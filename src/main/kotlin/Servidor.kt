
import Controlador.ServidorHilo
import Modelo.Verificar
import java.net.ServerSocket

fun main() {
    val puerto = 2300
    val sc = ServerSocket(puerto)
    val clientes = Verificar()
    while (true){
        val cliente = sc.accept()
        val IN = cliente.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT = cliente.getOutputStream().bufferedWriter(Charsets.UTF_8)
        var hilo = ServidorHilo(cliente, IN, OUT, clientes)
        hilo.start()
    }

}