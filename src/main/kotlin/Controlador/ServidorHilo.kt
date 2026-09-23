package Controlador

import Modelo.Disconnected
import Modelo.Identify
import Modelo.Invitation
import Modelo.Invite
import Modelo.JoinRoom
import Modelo.JoinedRoom
import Modelo.JsonUtil
import Modelo.LeaveRoom
import Modelo.LeftRoom
import Modelo.NewRoom
import Modelo.NewStatus
import Modelo.NewUser
import Modelo.PublicText
import Modelo.PublicTextFrom
import Modelo.Response
import Modelo.RoomText
import Modelo.RoomTextFrom
import Modelo.RoomUserList
import Modelo.RoomUsers
import Modelo.Salas
import Modelo.Status
import Modelo.Text
import Modelo.TextFrom
import Modelo.UserList
import Modelo.UsuarioConectado
import Modelo.Verificar
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ServidorHilo(
    private val socket: Socket,
    private val IN: BufferedReader,
    private val OUT: BufferedWriter,
    private val verificar: Verificar,
    private val salas: Salas
) : Thread(), UsuarioConectado {

    private var nombreCliente = ""
    private var identificado = false
    private var estado = "ACTIVE"
    private val salasUnidas = mutableSetOf<String>()
    // esto es como el static en java, para evitar crear uno para cada
    companion object{
        const val TAMANO_MAXIMO = 1024 * 1024
    }

    override fun run() {
        try {
            val mensaje = IN.readLine()
            if (mensaje == null) {
                socket.close()
                return
            }
            println("<<<<< $mensaje")
            val identify = try{
                JsonUtil.json.decodeFromString<Identify>(mensaje)
            } catch (e: Exception) {null}

            //checamos si su nombre no se pasa de 8 caracteres
            if (identify == null || identify.username.length > 8){
                val invalido = Response(operation = "INVALID", result = "INVALID")
                val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                println(">>>>>>> $mensajeInvalido")
                enviarMensaje(mensajeInvalido)
                socket.close()
                return
            }
            val nombreSolicitado = identify.username
            try {
                verificar.agregarUsuario(nombreSolicitado, this)
            } catch (e: Exception) {
                val respuesta = Response(
                    operation = "IDENTIFY",
                    result = "USER_ALREADY_EXISTS",
                    extra = nombreSolicitado
                )
                val mensajeRespuesta = (JsonUtil.json.encodeToString(respuesta))
                println(">>>>>>> $mensajeRespuesta")
                enviarMensaje(mensajeRespuesta)
                socket.close()
                return
            }
            //usuario aceptado
            nombreCliente = nombreSolicitado
            identificado = true
            val respuesta = Response(
                operation = "IDENTIFY",
                result = "SUCCESS",
                extra = nombreCliente
            )
            val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
            println(">>>>>>> $mensajeRespuesta")
            enviarMensaje(mensajeRespuesta)

            val nuevoUsuario = NewUser(username = nombreCliente)
            val mensajeNuevoUsuario = JsonUtil.json.encodeToString(nuevoUsuario)
            println(">>>>>> $mensajeNuevoUsuario")
            verificar.enviarMensajeExcepto(mensajeNuevoUsuario,this)

            while (true) {
                val mensajeRecibido = IN.readLine() ?: break
                println("<<<<<< $mensajeRecibido")

                //checamos si el mensaje sobrepasa el tamaño maximo
                val tamanoBytes = mensajeRecibido.toByteArray(Charsets.UTF_8).size
                if(tamanoBytes > TAMANO_MAXIMO){
                    val invalido = Response(operation = "INVALID", result = "INVALID")
                    val mensaInvalido = JsonUtil.json.encodeToString(invalido)
                    println(">>>>>>> $mensaInvalido")
                    enviarMensaje(mensaInvalido)
                    break //desconectamos al cliente
                }

                val json = JsonUtil.json.parseToJsonElement(mensajeRecibido)
                val tipo = json.jsonObject["type"]?.jsonPrimitive?.content

                when(tipo){
                    "PUBLIC_TEXT" -> {
                        val textoPublico = JsonUtil.json.decodeFromString<PublicText>(mensajeRecibido)
                        val publicTextFrom = PublicTextFrom(username = nombreCliente, text = textoPublico.text)
                        val mensajeAEnviar = JsonUtil.json.encodeToString(publicTextFrom)
                        println(">>>>>> $mensajeAEnviar")
                        verificar.enviarMensajeExcepto(JsonUtil.json.encodeToString(publicTextFrom),this)
                    }

                    "DISCONNECT" -> {
                        break
                    }

                    "USERS" -> {
                        val listaUsuarios = verificar.obtenerListaUsuarios()
                        val userList = UserList(users = listaUsuarios)
                        val mensajeUserList = JsonUtil.json.encodeToString(userList)
                        println(">>>>>>> $mensajeUserList")
                        enviarMensaje(mensajeUserList)
                    }

                    "STATUS" -> {
                        val estadosValidos = setOf("ACTIVE", "AWAY", "BUSY")
                        val statusMsg = try {
                            JsonUtil.json.decodeFromString<Status>(mensajeRecibido)
                        } catch (e: Exception){
                            null
                        }
                        if(statusMsg == null || statusMsg.status !in estadosValidos){
                            //para mensaje incompleto o que no se reconocio el estado,desconectamos
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                            break //sale del while y desconect al cliente
                        } else if (statusMsg.status != estado){
                            estado = statusMsg.status
                            val newStatus = NewStatus(username = nombreCliente, status = estado)
                            val mensajeNewStatus = JsonUtil.json.encodeToString(newStatus)
                            println(">>>>>>>> $mensajeNewStatus")
                            verificar.enviarMensajeExcepto(mensajeNewStatus,this)
                        }
                    }

                    "TEXT" -> {
                        val textMsg = JsonUtil.json.decodeFromString<Text>(mensajeRecibido)
                        val textFrom = TextFrom(username = nombreCliente, text = textMsg.text)
                        val mensajeTextFrom = JsonUtil.json.encodeToString(textFrom)

                        val existe = verificar.enviarMensajeAUsuario(textMsg.username, mensajeTextFrom)
                        if(!existe){
                            val respuesta =
                                Response(operation = "TEXT", result = "NO_SUCH_USER", extra = textMsg.username)
                            val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                            println(">>>>>>> $mensajeRespuesta")
                            enviarMensaje(mensajeRespuesta)
                        } else {
                            println(">>>>>> $mensajeTextFrom")
                        }
                    }

                    "NEW_ROOM" -> {
                        val newRoom = try {
                            JsonUtil.json.decodeFromString<NewRoom>(mensajeRecibido)
                        } catch (e: Exception){null}

                        if (newRoom == null || newRoom.roomname.length > 16){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                        } else{
                            try {
                                salas.crearSala(newRoom.roomname, nombreCliente, this)
                                val respuesta = Response(operation = "NEW_ROOM", result = "SUCCESS", extra = newRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } catch (e: Exception){
                                val respuesta = Response(operation = "NEW_ROOM", result = "ROOM_ALREADY_EXISTS", extra = newRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            }
                        }
                    }

                    "INVITE" -> {
                        val invite = try {
                            JsonUtil.json.decodeFromString<Invite>(mensajeRecibido)
                        } catch (e: Exception) { null }
                        if (invite == null){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                        } else{
                            val sala = salas.obtenerSala(invite.roomname)
                            if (sala == null){
                                val respuesta = Response(operation = "INVITE", result = "NO_SUCH_ROOM", extra = invite.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else if (!sala.contieneUsuario(nombreCliente)){
                                val respuesta = Response(operation = "INVITE", result = "NOT_JOINED", extra = invite.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else{
                                //checamos si los usuarios si existen
                                val faltante = invite.usernames.firstOrNull{verificar.obtenerUsuarioConectado(it) == null}
                                if (faltante != null){
                                    val respuesta = Response(operation = "INVITE", result = "NO_SUCH_USER", extra = faltante)
                                    val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                    println(">>>>>>>> $mensajeRespuesta")
                                    enviarMensaje(mensajeRespuesta)
                                } else{
                                    //todos existen, entonces invitamos a los que no sean miembros o ya los hayamos invitado
                                    for (nombreInvitado in invite.usernames){
                                        if (!sala.yaEsMiembroOInvitado(nombreInvitado)){
                                            sala.agregarInvitado(nombreInvitado)
                                            val usuarioDestino = verificar.obtenerUsuarioConectado(nombreInvitado)
                                            val invitation = Invitation(username = nombreCliente, roomname = invite.roomname)
                                            val mensajeInvitation = JsonUtil.json.encodeToString(invitation)
                                            println(">>>>>>> $mensajeInvitation")
                                            usuarioDestino?.enviarMensaje(mensajeInvitation)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "JOIN_ROOM" -> {
                        val joinRoom = try {
                            JsonUtil.json.decodeFromString<JoinRoom>(mensajeRecibido)
                        } catch (e: Exception) {null}
                        if (joinRoom == null){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                        } else {
                            val sala = salas.obtenerSala(joinRoom.roomname)
                            if (sala == null){
                                val respuesta = Response(operation = "JOIN_ROOM", result = "NO_SUCH_ROOM", extra = joinRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else if (!sala.estaInvitado(nombreCliente)){
                                val respuesta = Response(operation = "JOIN_ROOM", result = "NOT_INVITED", extra = joinRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else{
                                sala.agregarUsuario(nombreCliente, this)
                                salasUnidas.add(joinRoom.roomname)
                                val respuesta = Response(operation = "JOIN_ROOM", result = "SUCCESS", extra = joinRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)

                                val joinedRoom = JoinedRoom(roomname = joinRoom.roomname, username = nombreCliente)
                                val mensajeJoinedRoom = JsonUtil.json.encodeToString(joinedRoom)
                                println(">>>>>>> $mensajeJoinedRoom")
                                sala.enviarMensajeExcepto(mensajeJoinedRoom, this)
                            }
                        }
                    }

                    "ROOM_USERS"-> {
                        val roomUsers = try {
                            JsonUtil.json.decodeFromString<RoomUsers>(mensajeRecibido)
                        } catch (e: Exception) {null}

                        if (roomUsers == null){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                        } else {
                            val sala = salas.obtenerSala(roomUsers.roomname)
                            if (sala == null){
                                val respuesta = Response(operation = "ROOM_USERS", result = "NO_SUCH_ROOM", extra = roomUsers.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else if (!sala.contieneUsuario(nombreCliente)){
                                val respuesta = Response(operation = "ROOM_USERS", result = "NOT_JOINED", extra = roomUsers.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else{
                                val roomUserList = RoomUserList(roomname = roomUsers.roomname, users = sala.obtenerUsuarios())
                                val mensajeRoomUserList = JsonUtil.json.encodeToString(roomUserList)
                                println(">>>>>>>> $mensajeRoomUserList")
                                enviarMensaje(mensajeRoomUserList)
                            }
                        }
                    }
                    "ROOM_TEXT" -> {
                        val roomText = try {
                            JsonUtil.json.decodeFromString<RoomText>(mensajeRecibido)
                        } catch (e: Exception) {null}

                        if(roomText == null){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                         } else {
                             val sala = salas.obtenerSala(roomText.roomname)
                            if (sala == null){
                                val respuesta = Response(operation = "ROOM_TEXT", result = "NO_SUCH_ROOM", extra = roomText.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else if (!sala.contieneUsuario(nombreCliente)){
                                val respuesta = Response(operation = "ROOM_TEXT", result = "NOT_JOINED", extra = roomText.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else{
                                val roomTextFrom = RoomTextFrom(roomname = roomText.roomname, username = nombreCliente, text = roomText.text)
                                val mensajeRoomTextFrom = JsonUtil.json.encodeToString(roomTextFrom)
                                println(">>>>>>>>> $mensajeRoomTextFrom")
                                sala.enviarMensajeExcepto(mensajeRoomTextFrom, this)
                            }
                        }
                    }

                    "LEAVE_ROOM" -> {
                        val leaveRoom = try {
                            JsonUtil.json.decodeFromString<LeaveRoom>(mensajeRecibido)
                        } catch (e: Exception) {null}

                        if (leaveRoom == null){
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                        } else{
                            val sala = salas.obtenerSala((leaveRoom.roomname))
                            if (sala == null){
                                val respuesta = Response(operation = "LEAVE_ROOM", result = "NO_SUCH_ROOM", extra = leaveRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else if (!sala.contieneUsuario(nombreCliente)){
                                val respuesta = Response(operation = "LEAVE_ROOM", result = "NOT_JOINED", extra = leaveRoom.roomname)
                                val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                                println(">>>>>>> $mensajeRespuesta")
                                enviarMensaje(mensajeRespuesta)
                            } else{
                                salirDeSala(leaveRoom.roomname)
                            }
                        }
                    }

                    else -> {
                        println("Mensaje desconocido: $tipo")
                    }
                }

            }
        } finally {
            if (identificado) {
                if (identificado){
                    for (nombreSala in salasUnidas.toList()){
                        salirDeSala(nombreSala)
                    }
                }
                verificar.eliminaUsuario(nombreCliente)
                val desconectado = Disconnected(username = nombreCliente)
                val mensajeAEnviar = JsonUtil.json.encodeToString(desconectado)
                println(">>>>> $mensajeAEnviar")
                verificar.enviarMensaje(mensajeAEnviar)
            }
            socket.close()
        }
    }

    override fun enviarMensaje(mensaje: String){
        try {
            OUT.write(mensaje)
            OUT.newLine()
            OUT.flush()
        } catch (e: Exception){
            // el socket del cliente ya no existe y
        // lo ignoramos silenciosamente sin ningun error de tuberia rota
        }
    }

    private fun salirDeSala(nombreSala: String){
        val sala = salas.obtenerSala(nombreSala) ?: return
        sala.eliminarUsuario(nombreCliente)
        val leftRoom = LeftRoom(roomname = nombreSala, username = nombreCliente)
        val mensajeLeftRoom = JsonUtil.json.encodeToString(leftRoom)
        println(">>>>>>>> $mensajeLeftRoom")
        sala.enviarMensajeExcepto(mensajeLeftRoom, this)
        salas.eliminarSalaSiVacia(nombreSala)
        salasUnidas.remove(nombreSala)
    }

    override fun obtenerEstado(): String {
        return estado
    }
}