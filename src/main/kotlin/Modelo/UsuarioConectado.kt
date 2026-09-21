package Modelo

interface UsuarioConectado {
    fun enviarMensaje(mensaje: String)
    fun obtenerEstado(): String
}