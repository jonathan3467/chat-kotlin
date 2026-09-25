import Controlador.ClienteControlador
import Vista.VistaCliente
import java.util.Scanner


fun main(){
    val vista = VistaCliente()
    val ip = vista.pedirIp()
    val puerto = vista.pedirPuerto()
    val nombre = vista.pedirNombre()

    val controlador = ClienteControlador(ip = ip, puerto = puerto, vista = vista)
    controlador.identificar(nombre)

    val sn = Scanner(System.`in`)
    sn.useDelimiter("\n")

    while (controlador.estaActivo()){
        val texto = sn.next()
        controlador.procesarComando(texto)
    }
}