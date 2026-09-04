import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

fun main() {
    val puerto = 2300
    val sc = ServerSocket(puerto)
    val clientes = Verificar()
    println("servidor iniciado")
    while (true){
        val cliente = sc.accept()
        val IN = DataInputStream(cliente.getInputStream())
        val OUT = DataOutputStream(cliente.getOutputStream())
        var hilo = ServidorHilo(cliente,IN,OUT,clientes)
        hilo.start()

        println("nueva conexion")
    }

}