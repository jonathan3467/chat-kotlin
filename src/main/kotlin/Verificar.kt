class Verificar {
    private val usuarios = mutableMapOf<String, ServidorHilo>()

    fun agregarUsuario(nombre: String, hilo: ServidorHilo){
        if(usuarios.containsKey(nombre)){
            throw Exception("El nombre '$nombre' ya esta en uso.")
        }
        usuarios[nombre] = hilo
    }

    fun enviarMensaje(mensaje: String){
        for(usuario in usuarios.values){
            usuario.enviarMensaje(mensaje)
        }
    }
}