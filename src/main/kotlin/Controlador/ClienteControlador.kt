package Controlador

import Modelo.Disconnect
import Modelo.Identify
import Modelo.JsonUtil
import Modelo.PublicText
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
}