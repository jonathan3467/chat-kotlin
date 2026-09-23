package Controlador

import Modelo.Disconnected
import Modelo.Invitation
import Modelo.JoinedRoom
import Modelo.JsonUtil
import Modelo.LeftRoom
import Modelo.NewStatus
import Modelo.NewUser
import Modelo.PublicTextFrom
import Modelo.Response
import Modelo.RoomTextFrom
import Modelo.RoomUserList
import Modelo.TextFrom
import Modelo.UserList
import Vista.VistaCliente
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader

class ClienteHilo(
    private val IN: BufferedReader,
    private val vista: VistaCliente
) : Thread() {

    override fun run(){
        while (true){
            val mensaje = try {
                IN.readLine()
            } catch (e: Exception){
                null
            }
            if (mensaje == null){
                vista.mostrarConexionCerrada()
                break
            }
            val json = JsonUtil.json.parseToJsonElement(mensaje)
            val tipo = json.jsonObject["type"]?.jsonPrimitive?.content

            when(tipo){
                "RESPONSE" -> {
                    val respuesta = JsonUtil.json.decodeFromString<Response>(mensaje)
                    when(respuesta.operation){
                        "IDENTIFY" -> when (respuesta.result){
                            "SUCCESS" -> vista.mostrarConexionExitosa()
                            "USER_ALREADY_EXISTS" -> vista.mostrarNombreEnUso(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                    }
                        "NEW_ROOM" -> when (respuesta.result){
                            "SUCCESS" -> vista.mostrarSalaCreada(respuesta.extra ?: "")
                            "ROOM_ALREADY_EXISTS" -> vista.mostrarSalaEnUso(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "TEXT" -> when (respuesta.result){
                            "NO_SUCH_USER" -> vista.mostrarUsuarioInexistente(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "INVITE" -> when(respuesta.result){
                            "NO_SUCH_ROOM" -> vista.mostrarSalaInexistente(respuesta.extra ?: "")
                            "NOT_JOINED" -> vista.mostrarNoPerteneceSala(respuesta.extra ?: "")
                            "NO_SUCH_USER" -> vista.mostrarUsuarioInexistente(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "JOIN_ROOM" -> when(respuesta.result){
                            "SUCCESS" -> vista.mostrarUnionExitosaSala(respuesta.extra ?: "")
                            "NO_SUCH_ROOM" -> vista.mostrarSalaInexistente(respuesta.extra ?: "")
                            "NOT_INVITED" -> vista.mostrarNoInvitadoSala(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "ROOM_USERS" -> when (respuesta.result){
                            "NO_SUCH_ROOM" -> vista.mostrarSalaInexistente(respuesta.extra ?: "")
                            "NOT_JOINED" -> vista.mostrarNoPerteneceSala(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "ROOM_TEXT" -> when (respuesta.result){
                            "NO_SUCH_ROOM" -> vista.mostrarSalaInexistente(respuesta.extra ?: "")
                            "NOT_JOINED" -> vista.mostrarNoPerteneceSala(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "LEAVE_ROOM" -> when (respuesta.result) {
                            "NO_SUCH_ROOM" -> vista.mostrarSalaInexistente(respuesta.extra ?: "")
                            "NOT_JOINED" -> vista.mostrarNoPerteneceSala(respuesta.extra ?: "")
                            else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                        }
                        "INVALID" -> vista.mostrarMensajeInvalido()
                        else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                    }
                }
                "NEW_USER" -> {
                    val nuevoUsuario = JsonUtil.json.decodeFromString<NewUser>(mensaje)
                    vista.mostrarUsuarioConectado(nuevoUsuario.username)
                }
                "DISCONNECTED" -> {
                    val usuario = JsonUtil.json.decodeFromString<Disconnected>(mensaje)
                    vista.mostrarUsuarioDesconectado(usuario.username)
                }
                "PUBLIC_TEXT_FROM" -> {
                    val msg = JsonUtil.json.decodeFromString<PublicTextFrom>(mensaje)
                    vista.mostrarMensajePublico(msg.username, msg.text)
                }
                "TEXT_FROM" -> {
                    val msg = JsonUtil.json.decodeFromString<TextFrom>(mensaje)
                    vista.mostrarMensajePrivado(msg.username, msg.text)
                }
                "NEW_STATUS" -> {
                    val msg = JsonUtil.json.decodeFromString<NewStatus>(mensaje)
                    vista.mostrarCambioEstado(msg.username, msg.status)
                }
                "USER_LIST" -> {
                    val userList = JsonUtil.json.decodeFromString<UserList>(mensaje)
                    vista.mostrarListaUsuarios(userList.users)
                }
                "INVITATION" -> {
                    val msg = JsonUtil.json.decodeFromString<Invitation>(mensaje)
                    vista.mostrarInvitacion(msg.username, msg.roomname)
                }
                "JOINED_ROOM" -> {
                    val msg = JsonUtil.json.decodeFromString<JoinedRoom>(mensaje)
                    vista.mostrarUsuarioUnidoASala(msg.roomname, msg.username)
                }
                "ROOM_USER_LIST" -> {
                    val msg = JsonUtil.json.decodeFromString<RoomUserList>(mensaje)
                    vista.mostrarUsuariosSala(msg.roomname, msg.users)
                }
                "ROOM_TEXT_FROM" -> {
                    val msg = JsonUtil.json.decodeFromString<RoomTextFrom>(mensaje)
                    vista.mostrarMensajeSala(msg.roomname, msg.username, msg.text)
                }
                "LEFT_ROOM" -> {
                    val msg = JsonUtil.json.decodeFromString<LeftRoom>(mensaje)
                    vista.mostrarUsuarioSalioSala(msg.roomname, msg.username)
                }
                else -> {
                    vista.mostrarMensajeDesconocido()
                }
            }
        }
    }
}