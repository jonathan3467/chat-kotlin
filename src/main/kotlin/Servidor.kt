import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

fun main() {
    val puerto = 2300
    val sc = ServerSocket(puerto)
    val clientes = mutableListOf<ServidorHilo>()
    println("servidor iniciado")
    while (true){
        val cliente = sc.accept()
        val IN = DataInputStream(cliente.getInputStream())
        val OUT = DataOutputStream(cliente.getOutputStream())
        var hilo = ServidorHilo(IN,OUT,clientes)
        clientes.add(hilo)
        hilo.start()

        println("nueva conexion")
    }

}