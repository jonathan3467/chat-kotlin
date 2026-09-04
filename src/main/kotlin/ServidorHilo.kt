import java.io.DataInputStream
import java.io.DataOutputStream

class ServidorHilo(
    private val IN: DataInputStream,
    private val OUT: DataOutputStream,
    private val clientes: MutableList<ServidorHilo>) : Thread() {
    private var nombreCliente = ""
    override fun run() {
        OUT.writeUTF("indica tu nombre: ")
        nombreCliente = IN.readUTF()
        println("Cliente conectado: $nombreCliente")
        while (true){
            val mensaje = IN.readUTF()
            println("$nombreCliente: $mensaje")
            for(cliente in clientes){
                cliente.enviarMensaje("$nombreCliente: $mensaje")
            }
        }
    }

    fun enviarMensaje(mensaje: String){
        OUT.writeUTF(mensaje)
    }
}