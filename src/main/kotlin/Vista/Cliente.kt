package Vista

import Controlador.ClienteControlador
import java.util.Scanner

fun main(){
    val vista = VistaCliente()
    val nombre = vista.pedirNombre()

    val controlador = ClienteControlador(ip = "127.0.0.1", puerto = 2300, vista = vista)
    controlador.identificar(nombre)
    val sn = Scanner(System. `in`)
    sn.useDelimiter("\n")

    while (controlador.estaActivo()){
        val texto = sn.next()
        when{
            texto.equals("<desconectar>", ignoreCase = true) -> {
                controlador.desconectar()
            }
            texto.equals("<usuarios>", ignoreCase = true) -> {
                controlador.pedirListaUsuarios()
            }
            texto.startsWith("<estado>", ignoreCase = true) -> {
                val nuevoEstado = texto.substringAfter("<estado>").trim().uppercase()
                controlador.cambiarEstado(nuevoEstado)
            }
            texto.startsWith("<privado>" , ignoreCase = true) -> {
                val resto = texto.substringAfter("<privado>").trim()
                val partes = resto.split("", limit = 2)
                if (partes.size == 2){
                    controlador.enviarPrivado(partes[0], partes[1])
                } else {
                    println("Uso: <privado> usuario mensaje")
                }
            }
            else -> {
                controlador.enviarTextoPublico(texto)
            }
        }
    }
}