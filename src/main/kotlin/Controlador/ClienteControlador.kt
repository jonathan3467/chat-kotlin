package Controlador

import Modelo.Disconnect
import Modelo.Identify
import Modelo.Invite
import Modelo.JoinRoom
import Modelo.JsonUtil
import Modelo.LeaveRoom
import Modelo.NewRoom
import Modelo.PublicText
import Modelo.RoomText
import Modelo.RoomUsers
import Modelo.Status
import Modelo.Text
import Modelo.Users
import Vista.VistaCliente
import java.net.Socket

class ClienteControlador(
    private val ip: String,
    private val puerto: Int,
    private val vista: VistaCliente
){
    private val socket = Socket(ip, puerto)
    private val IN = socket.getInputStream().bufferedReader(Charsets.UTF_8)
    private val OUT = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
    private val hiloEscucha = ClienteHilo(IN, vista)

    fun estaActivo(): Boolean = hiloEscucha.isAlive

    fun identificar(nombre: String){
        val identify = Identify(username =  nombre)
        enviarAlServidor(JsonUtil.json.encodeToString(identify))
        hiloEscucha.start()
    }

    fun enviarTextoPublico(texto: String){
        val publicText = PublicText(text = texto)
        enviarAlServidor(JsonUtil.json.encodeToString(publicText))
    }

    fun enviarPrivado(destinatario: String, texto: String){
        val textMsg = Text(username = destinatario, text = texto)
        enviarAlServidor(JsonUtil.json.encodeToString(textMsg))
    }

    fun cambiarEstado(nuevoEstado: String){
        val status = Status(status = nuevoEstado)
        enviarAlServidor(JsonUtil.json.encodeToString(status))
    }

    fun pedirListaUsuarios(){
        val users = Users()
        enviarAlServidor(JsonUtil.json.encodeToString(users))
    }

    fun desconectar(){
        val disconnect = Disconnect()
        enviarAlServidor(JsonUtil.json.encodeToString(disconnect))
        socket.close()
    }

    private fun enviarAlServidor(mensajeJson: String){
        OUT.write(mensajeJson)
        OUT.newLine()
        OUT.flush()
    }

    fun crearSala(nombreSala: String){
        val newRoom = NewRoom(roomname = nombreSala)
        enviarAlServidor(JsonUtil.json.encodeToString(newRoom))
    }

    fun invitar(nombreSala: String, usuarios: List<String>){
        val invite = Invite(roomname = nombreSala, usernames = usuarios)
        enviarAlServidor(JsonUtil.json.encodeToString(invite))
    }

    fun unirseASala(nombreSala: String){
        val joinRoom = JoinRoom(roomname = nombreSala)
        enviarAlServidor(JsonUtil.json.encodeToString(joinRoom))
    }

    fun pedirUsuarioSala(nombreSala: String){
        val roomUsers = RoomUsers(roomname = nombreSala)
        enviarAlServidor(JsonUtil.json.encodeToString(roomUsers))
    }

    fun enviarTextoSala(nombreSala: String, texto: String){
        val roomText = RoomText(roomname = nombreSala, text = texto)
        enviarAlServidor(JsonUtil.json.encodeToString(roomText))
    }

    fun salirDeSala(nombreSala: String){
        val leaveRoom = LeaveRoom(roomname = nombreSala)
        enviarAlServidor(JsonUtil.json.encodeToString(leaveRoom))
    }

    fun procesarComando(texto: String){
        when{
            texto.equals("<desconectar>", ignoreCase = true) -> {
                desconectar()
            }
            texto.equals("<usuarios>", ignoreCase = true) -> {
                pedirListaUsuarios()
            }
            texto.startsWith("<estado>", ignoreCase = true) -> {
                val nuevoEstado = texto.substringAfter("<estado>").trim().uppercase()
                cambiarEstado(nuevoEstado)
            }
            texto.startsWith("<privado>" , ignoreCase = true) -> {
                val resto = texto.substringAfter("<privado>").trim()
                val partes = resto.split(" ", limit = 2)
                if (partes.size == 2){
                    enviarPrivado(partes[0], partes[1])
                } else {
                    vista.mostrarUsoPrivado()
                }
            }
            texto.startsWith("<crear_sala>", ignoreCase = true) -> {
                val nombreSala = texto.substringAfter("<crear_sala>").trim()
                crearSala(nombreSala)
            }
            texto.startsWith("<invitar>", ignoreCase = true) -> {
                val resto = texto.substringAfter("<invitar>").trim()
                val partes = resto.split(" ")
                if (partes.size >= 2){
                    val nombreSala = partes[0]
                    val usuarios = partes.drop(1)
                    invitar(nombreSala, usuarios)
                } else{
                    vista.mostrarUsoInvitar()
                }
            }
            texto.startsWith("<unirse>", ignoreCase = true) -> {
                val nombreSala = texto.substringAfter("<unirse>").trim()
                unirseASala(nombreSala)
            }
            texto.startsWith("<usuarios_sala>", ignoreCase = true) -> {
                val nombreSala = texto.substringAfter("<usuarios_sala>").trim()
                pedirUsuarioSala(nombreSala)
            }
            texto.startsWith("<sala_texto>", ignoreCase = true) -> {
                val resto = texto.substringAfter("<sala_texto>").trim()
                val partes = resto.split(" ", limit = 2)
                if (partes.size == 2){
                    enviarTextoSala(partes[0], partes[1])
                } else {
                    vista.mostrarUsoSalaTexto()
                }
            }
            texto.startsWith("<salir_sala>", ignoreCase = true) -> {
                val nombreSala = texto.substringAfter("<salir_sala>").trim()
                salirDeSala(nombreSala)
            }
            else -> {
                enviarTextoPublico(texto)
            }
        }
    }
}