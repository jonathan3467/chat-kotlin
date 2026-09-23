package Modelo

class Sala (val nombre: String) {
    private val usuarios = mutableMapOf<String, UsuarioConectado>()
    private val invitados = mutableSetOf<String>()

    @Synchronized
    fun agregarUsuario(nombre: String, usuario: UsuarioConectado){
        usuarios[nombre] = usuario
        invitados.remove(nombre)
    }

    @Synchronized
    fun eliminarUsuario(nombre: String){
        usuarios.remove(nombre)
    }

    @Synchronized
    fun estaVacia(): Boolean = usuarios.isEmpty()

    @Synchronized
    fun contieneUsuario(nombre: String) : Boolean = usuarios.containsKey(nombre)

    @Synchronized
    fun estaInvitado(nombre: String): Boolean = invitados.contains(nombre)

    @Synchronized
    fun yaEsMiembroOInvitado(nombre: String): Boolean =
        usuarios.containsKey(nombre) || invitados.contains(nombre)

    @Synchronized
    fun agregarInvitado(nombre: String){
        invitados.add(nombre)
    }

    @Synchronized
    fun obtenerUsuarios(): Map<String, String>{
        return usuarios.mapValues {
            it.value.obtenerEstado()
        }
    }

    @Synchronized
    fun enviarMensajeExcepto(mensaje: String, excepto: UsuarioConectado){
        for (usuario in usuarios.values){
            if (usuario != excepto) usuario.enviarMensaje(mensaje)
        }
    }
}