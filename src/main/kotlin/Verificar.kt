class Verificar {
    private val usuarios = mutableMapOf<String, ServidorHilo>()

    @Synchronized
    fun agregarUsuario(nombre: String, hilo: ServidorHilo){
        if(usuarios.containsKey(nombre)){
            throw Exception("El nombre '$nombre' ya esta en uso.")
        }
        usuarios[nombre] = hilo
    }

    @Synchronized
    fun enviarMensaje(mensaje: String){
        for(usuario in usuarios.values){
            usuario.enviarMensaje(mensaje)
        }
    }

    @Synchronized
    fun enviarMensajeExcepto(mensaje: String, excepto:ServidorHilo){
        for(usuario in usuarios.values){
            if(usuario != excepto){
                usuario.enviarMensaje(mensaje)
            }
        }
    }

    @Synchronized
    fun eliminaUsuario(nombre: String){
        usuarios.remove(nombre)
    }

    @Synchronized
    fun obtenerListaUsuarios(): Map<String, String>{
        val lista = mutableMapOf<String, String>()
        for((nombre, hilo) in usuarios){
            lista[nombre] = hilo.getEstado()
        }
        return lista
    }

    @Synchronized
    fun enviarMnesajeAUsuario(nombreDestino: String, mensaje: String): Boolean{
        val hiloDestino = usuarios[nombreDestino] ?: return false
        hiloDestino.enviarMensaje(mensaje)
        return true
    }
}