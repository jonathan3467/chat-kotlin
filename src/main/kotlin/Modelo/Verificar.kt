package Modelo

import kotlin.collections.iterator

class Verificar {
    private val usuarios = mutableMapOf<String, UsuarioConectado>()

    @Synchronized
    fun agregarUsuario(nombre: String, usuario: UsuarioConectado){
        if(usuarios.containsKey(nombre)){
            throw Exception("El nombre '$nombre' ya esta en uso.")
        }
        usuarios[nombre] = usuario
    }

    @Synchronized
    fun enviarMensaje(mensaje: String){
        for(usuario in usuarios.values){
            usuario.enviarMensaje(mensaje)
        }
    }

    @Synchronized
    fun enviarMensajeExcepto(mensaje: String, excepto: UsuarioConectado){
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
        for((nombre, usuario) in usuarios){
            lista[nombre] = usuario.obtenerEstado()
        }
        return lista
    }

    @Synchronized
    fun enviarMensajeAUsuario(nombreDestino: String, mensaje: String): Boolean{
        val usuarioDestino = usuarios[nombreDestino] ?: return false
        usuarioDestino.enviarMensaje(mensaje)
        return true
    }

    @Synchronized
    fun obtenerUsuarioConectado(nombre: String): UsuarioConectado? = usuarios[nombre]
}