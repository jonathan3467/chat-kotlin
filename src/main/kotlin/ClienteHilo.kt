import java.io.DataInputStream
import java.io.DataOutputStream

class ClienteHilo(
    private val IN: DataInputStream,
    private val OUT: DataOutputStream) : Thread() {

    override fun run() {
    while(true){
     val mensaje = IN.readUTF()
     println(mensaje)
        }
    }
}