package Vista

class VistaCliente {
    private val coloresDisponibles = listOf(
        "\u001B[31m",  //rojo
        "\u001B[32m",  // verde
        "\u001B[33m",  //amarrillo
        "\u001B[34m",  // azul
        "\u001B[35m",  // magenta
        "\u001B[36m",  // cian
    )
    private val RESET = "\u001B[0m"

    private fun colorParaSala(sala : String): String{
        val indice = Math.abs(sala.hashCode()) % coloresDisponibles.size
        return coloresDisponibles[indice]
    }
    fun mostrarConexionExitosa() = println("Conexion exitosa")
    fun mostrarNombreEnUso(nombre: String) = println("El nombre '$nombre' ya esta en uso, intenta con otro")
    fun mostrarMensajeInvalido() = println("Se paso del tamaño limite del mensaje, o el mensaje era invalido por lo que seras desconectado")
    fun mostrarUsuarioInexistente(nombre: String) = println("El usuario '$nombre' no existe")
    fun mostrarRespuestaGenerica(operacion: String, resultado: String) = println("Respuesta del servidor: $operacion -> $resultado")
    fun mostrarUsuarioConectado(nombre: String) = println("$nombre se conecto")
    fun mostrarUsuarioDesconectado(nombre: String) = println("$nombre se desconecto")
    fun mostrarMensajePublico(usuario: String, texto: String) = println("$usuario: $texto")
    fun mostrarMensajePrivado(usuario: String, texto: String) = println("[privado] $usuario: $texto")
    fun mostrarCambioEstado(usuario: String, estado: String) = println("$usuario cambio su estado a $estado")
    fun mostrarListaUsuarios(usuarios: Map<String, String>){
        println("Usuarios conectados:")
        for ((nombre, estado) in usuarios){
            println("  $nombre: $estado")
        }
    }
    fun mostrarConexionCerrada() = println("Conexion cerrada por el servidor")
    fun mostrarMensajeDesconocido() = println("Mensaje desconocido")
    fun pedirNombre(): String{
        print("Indica tu nombre: ")
        return readLine()?.trim() ?: ""
    }
    fun pedirIp(): String{
        print("IP del servidor (Enter para 127.0.0.1): ")
        val entrada = readLine()?.trim()
        return if (entrada.isNullOrEmpty()) "127.0.0.1" else entrada
    }
    fun pedirPuerto(): Int {
        print("Puero del servidor (Eneter para 2300): ")
        val entrada = readLine()?.trim()
        if (entrada.isNullOrEmpty()) return 2300
        return entrada.toIntOrNull() ?: run {
            println("Puerto invalido, usando 2300 por defecto")
            2300
        }
    }
    fun mostrarSalaCreada(nombreSala: String) {
        val color = colorParaSala(nombreSala)
        println("${color}Sala `$nombreSala` creada exitosamente$RESET")
    }
    fun mostrarSalaEnUso(nombreSala: String) = println("Este nombre de sala `$nombreSala` ya esta en uso")
    fun mostrarInvitacion(usuario: String, sala: String) = println("$usuario te invito a la sala `$sala` (usa <unirse> $sala para entrar)")
    fun mostrarUsuarioUnidoASala(sala: String, usuario: String) {
        val color = colorParaSala(sala)
        println("${color}$usuario se unio a la sala `$sala`$RESET")
    }
    fun mostrarSalaInexistente(sala: String) = println("La sala `$sala` no existe")
    fun mostrarNoPerteneceSala(sala: String) = println("No estas en la sala `$sala`")
    fun mostrarNoInvitadoSala(sala: String) = println("No has sido invitado a la sala `$sala`")
    fun mostrarUnionExitosaSala(sala: String) {
        val color = colorParaSala(sala)
        println("${color}Te uniste a la sala `$sala`$RESET")
    }
    fun mostrarUsuariosSala(sala: String, usuarios: Map<String, String>){
        val color = colorParaSala(sala)
        println("${color}Usuarios en la sala `$sala`: $RESET")
        for ((nombre, estado) in usuarios){
            println("${color} $nombre: $estado$RESET")
        }
    }
    fun mostrarMensajeSala(sala: String, usuario: String, texto: String) {
        val color = colorParaSala(sala)
        println("${color}[$sala] $usuario: $texto$RESET")
    }
    fun mostrarUsuarioSalioSala(sala: String, usuario: String) {
        val color = colorParaSala(sala)
        println("${color}$usuario salio de la sala `$sala`$RESET")
    }
    fun mostrarUsoPrivado() = println("Uso: <privado> usuario mensaje")
    fun mostrarUsoInvitar() = println("Uso: <invitar> sala usuario1 usuario2 ...")
    fun mostrarUsoSalaTexto() = println("Uso: <sala_texto> sala mensaje")
}