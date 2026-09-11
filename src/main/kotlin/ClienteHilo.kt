import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader

class ClienteHilo(
    private val IN: BufferedReader) : Thread() {

    override fun run() {
            while (true) {
                val mensaje = try {
                    IN.readLine()
                } catch (e: Exception){
                    null
                }
                if (mensaje == null){
                    println("Conexion cerrada por el servidor")
                    break
                }
                val json = JsonUtil.json.parseToJsonElement(mensaje)
                val tipo = json.jsonObject["type"]?.jsonPrimitive?.content
                when(tipo){
                    "RESPONSE" -> {
                        val respuesta = JsonUtil.json.decodeFromString<Response>(mensaje)
                        when(respuesta.result){
                            "SUCCESS" -> println("Conexion exitosa")
                            "USER_ALREADY_EXISTS" -> println("El nombre '${respuesta.extra}' ya esta en uso, intenta con otro")
                            else -> println("Respuesta del servidor: ${respuesta.operation} -> ${respuesta.result}")
                        }
                    }

                    "NEW_USER" -> {
                        val nuevoUsuario = JsonUtil.json.decodeFromString<NewUser>(mensaje)
                        println("${nuevoUsuario.username} se conecto")
                    }

                    "DISCONNECTED" -> {
                        val usuario = JsonUtil.json.decodeFromString<Disconnected>(mensaje)
                        println("${usuario.username} se desconecto")
                    }

                    "PUBLIC_TEXT_FROM" -> {
                        val msg = JsonUtil.json.decodeFromString<PublicTextFrom>(mensaje)
                        println("${msg.username}: ${msg.text}")
                    }
                    else -> {
                        println("Mensaje desconocido")
                    }
                }
            }
    }
}