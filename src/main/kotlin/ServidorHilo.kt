import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class ServidorHilo(
    private val socket: Socket,
    private val IN: DataInputStream,
    private val OUT: DataOutputStream,
    private val verificar: Verificar) : Thread() {
    private var nombreCliente = ""
    override fun run() {
        OUT.writeUTF("indica tu nombre: ")
        nombreCliente = IN.readUTF()
        try {
            verificar.agregarUsuario(nombreCliente, this)
        }catch (e: Exception){
            OUT.writeUTF(e.message?: "Error")
            socket.close()
            return
        }
        println("Cliente conectado: $nombreCliente")
        while (true){
            val mensaje = IN.readUTF()
            println("$nombreCliente: $mensaje")
            verificar.enviarMensaje("$nombreCliente: $mensaje")
        }
    }

    fun enviarMensaje(mensaje: String){
        OUT.writeUTF(mensaje)
    }
}