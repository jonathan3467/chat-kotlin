import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

class ClienteHilo(
    private val IN: DataInputStream,
    private val OUT: DataOutputStream) : Thread() {

    override fun run() {
        try {
            while (true) {
                val mensaje = IN.readUTF()
                println(mensaje)
            }
        } catch (e: EOFException){
            println("Conexion cerrada por el servidor.")
        }
    }
}