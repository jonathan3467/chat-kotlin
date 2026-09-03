import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

fun main() {
    val puerto = 2300
    val sc = ServerSocket(puerto)
    print("servidor iniciado \n")
    while (true){
        val cliente = sc.accept()
        val IN = DataInputStream(cliente.getInputStream())
        val OUT = DataOutputStream(cliente.getOutputStream())

        OUT.writeUTF("Indica tu nombre: ")
        var nombreCliente = IN.readUTF()
        var hilo = ServidorHilo(IN,OUT,nombreCliente)
        hilo.start()

        print("creada la conexion con el cliente" + nombreCliente)
    }

}