package Vista

class VistaCliente {
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
}