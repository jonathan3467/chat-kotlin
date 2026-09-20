import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ServidorHilo(
    private val socket: Socket,
    private val IN: BufferedReader,
    private val OUT: BufferedWriter,
    private val verificar: Verificar) : Thread() {
    private var nombreCliente = ""
    private var identificado = false
    private var estado = "ACTIVE"
    // esto es como el static en java, para evitar crear uno para cada
    companion object{
        const val TAMANO_MAXIMO = 1024 * 1024
    }

    override fun run() {
        try {
            val mensaje = IN.readLine()
            if (mensaje == null) {
                socket.close()
                return
            }
            println("<<<<< $mensaje")
            val identify = JsonUtil.json.decodeFromString<Identify>(mensaje)
            val nombreSolicitado = identify.username

            //checamos si su nombre no se pasa de 8 caracteres
            if (nombreSolicitado.length > 8){
                val invalido = Response(operation = "INVALID", result = "INVALID")
                val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                println(">>>>>>> $mensajeInvalido")
                enviarMensaje(mensajeInvalido)
                socket.close()
                return
            }

            try {
                verificar.agregarUsuario(nombreSolicitado, this)
            } catch (e: Exception) {
                val respuesta = Response(
                    operation = "IDENTIFY",
                    result = "USER_ALREADY_EXISTS",
                    extra = nombreSolicitado
                )
                val mensajeRespuesta = (JsonUtil.json.encodeToString(respuesta))
                println(">>>>>>> $mensajeRespuesta")
                enviarMensaje(mensajeRespuesta)
                socket.close()
                return
            }
            //usuario aceptado
            nombreCliente = nombreSolicitado
            identificado = true
            val respuesta = Response(
                operation = "IDENTIFY",
                result = "SUCCESS",
                extra = nombreCliente
            )
            val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
            println(">>>>>>> $mensajeRespuesta")
            enviarMensaje(mensajeRespuesta)

            val nuevoUsuario = NewUser(username = nombreCliente)
            val mensajeNuevoUsuario = JsonUtil.json.encodeToString(nuevoUsuario)
            println(">>>>>> $mensajeNuevoUsuario")
            verificar.enviarMensajeExcepto(mensajeNuevoUsuario,this)

            while (true) {
                val mensajeRecibido = IN.readLine() ?: break
                println("<<<<<< $mensajeRecibido")

                //checamos si el mensaje sobrepasa el tamaño maximo
                val tamanoBytes = mensajeRecibido.toByteArray(Charsets.UTF_8).size
                if(tamanoBytes > TAMANO_MAXIMO){
                    val invalido = Response(operation = "INVALID", result = "INVALID")
                    val mensaInvalido = JsonUtil.json.encodeToString(invalido)
                    println(">>>>>>> $mensaInvalido")
                    enviarMensaje(mensaInvalido)
                    break //desconectamos al cliente
                }

                val json = JsonUtil.json.parseToJsonElement(mensajeRecibido)
                val tipo = json.jsonObject["type"]?.jsonPrimitive?.content

                when(tipo){
                    "PUBLIC_TEXT" -> {
                        val textoPublico = JsonUtil.json.decodeFromString<PublicText>(mensajeRecibido)
                        val publicTextFrom = PublicTextFrom(username = nombreCliente, text = textoPublico.text)
                        val mensajeAEnviar = JsonUtil.json.encodeToString(publicTextFrom)
                        println(">>>>>> $mensajeAEnviar")
                        verificar.enviarMensajeExcepto(JsonUtil.json.encodeToString(publicTextFrom),this)
                    }

                    "DISCONNECT" -> {
                        break
                    }

                    "USERS" -> {
                        val listaUsuarios = verificar.obtenerListaUsuarios()
                        val userList = UserList(users = listaUsuarios)
                        val mensajeUserList = JsonUtil.json.encodeToString(userList)
                        println(">>>>>>> $mensajeUserList")
                        enviarMensaje(mensajeUserList)
                    }

                    "STATUS" -> {
                        val estadosValidos = setOf("ACTIVE", "AWAY", "BUSY")
                        val statusMsg = try {
                            JsonUtil.json.decodeFromString<Status>(mensajeRecibido)
                        } catch (e: Exception){
                            null
                        }
                        if(statusMsg == null || statusMsg.status !in estadosValidos){
                            //para mensaje incompleto o que no se reconocio el estado,desconectamos
                            val invalido = Response(operation = "INVALID", result = "INVALID")
                            val mensajeInvalido = JsonUtil.json.encodeToString(invalido)
                            println(">>>>>>> $mensajeInvalido")
                            enviarMensaje(mensajeInvalido)
                            break //sale del while y desconect al cliente
                        } else if (statusMsg.status != estado){
                            estado = statusMsg.status
                            val newStatus = NewStatus(username = nombreCliente, status = estado)
                            val mensajeNewStatus = JsonUtil.json.encodeToString(newStatus)
                            println(">>>>>>>> $mensajeNewStatus")
                            verificar.enviarMensajeExcepto(mensajeNewStatus,this)
                        }
                    }

                    "TEXT" -> {
                        val textMsg = JsonUtil.json.decodeFromString<Text>(mensajeRecibido)
                        val textFrom = TextFrom(username = nombreCliente, text = textMsg.text)
                        val mensajeTextFrom = JsonUtil.json.encodeToString(textFrom)

                        val existe = verificar.enviarMnesajeAUsuario(textMsg.username, mensajeTextFrom)
                        if(!existe){
                            val respuesta = Response(operation = "TEXT", result = "NO_SUCH_USER", extra = textMsg.username)
                            val mensajeRespuesta = JsonUtil.json.encodeToString(respuesta)
                            println(">>>>>>> $mensajeRespuesta")
                            enviarMensaje(mensajeRespuesta)
                        } else {
                            println(">>>>>> $mensajeTextFrom")
                        }
                    }
                    else -> {
                        println("Mensaje desconocido: $tipo")
                    }
                }

            }
        } finally {
            if (identificado) {
                verificar.eliminaUsuario(nombreCliente)
                val desconectado = Disconnected(username = nombreCliente)
                val mensajeAEnviar = JsonUtil.json.encodeToString(desconectado)
                println(">>>>> $mensajeAEnviar")
                verificar.enviarMensaje(mensajeAEnviar)
            }
            socket.close()
        }
    }

    fun enviarMensaje(mensaje: String){
        try {
            OUT.write(mensaje)
            OUT.newLine()
            OUT.flush()
        } catch (e: Exception){
            // el socket del cliente ya no existe y
        // lo ignoramos silenciosamente sin ningun error de tuberia rota
        }
    }

    fun getEstado(): String {
        return estado
    }
}