package Modelo

class Salas {
    private val salas = mutableMapOf<String, Sala>()

    @Synchronized
    fun crearSala(nombre: String, creador: String, usuarioConectado: UsuarioConectado){
        if (salas.containsKey(nombre)){
            throw Exception("La sala `$nombre` ya existe")
        }
        val sala = Sala(nombre)
        sala.agregarUsuario(creador, usuarioConectado)
        salas[nombre] = sala
    }

    @Synchronized
    fun obtenerSala(nombre: String): Sala? = salas[nombre]

    @Synchronized
    fun eliminarSalaSiVacia(nombre: String){
        if (salas[nombre]?. estaVacia() == true){
            salas.remove(nombre)
        }
    }
}