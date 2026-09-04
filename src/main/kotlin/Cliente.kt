import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.Scanner

fun main() {
    val sn = Scanner(System.`in`)
    sn.useDelimiter("\n")
    val cliente = Socket("127.0.0.1", 2300)
    val IN = DataInputStream(cliente.getInputStream())
    val OUT = DataOutputStream(cliente.getOutputStream())

    var mensaje = IN.readUTF()
    print(mensaje)
    val nombre = sn.next()
    OUT.writeUTF(nombre)



    val hilo = ClienteHilo(IN,OUT)
    hilo.start()
    while (hilo.isAlive){
        val mensajeEnviar = sn.next()
        OUT.writeUTF(mensajeEnviar)
    }
}