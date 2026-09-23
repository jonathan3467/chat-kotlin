import Controlador.ClienteControlador
import Vista.VistaCliente
import java.util.Scanner


fun main(){
    val vista = VistaCliente()
    val nombre = vista.pedirNombre()

    val controlador = ClienteControlador(ip = "127.0.0.1", puerto = 2300, vista = vista)
    controlador.identificar(nombre)

    val sn = Scanner(System.`in`)
    sn.useDelimiter("\n")

    while (controlador.estaActivo()){
        val texto = sn.next()
        controlador.procesarComando(texto)
    }
}