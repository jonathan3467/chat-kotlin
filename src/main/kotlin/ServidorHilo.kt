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
}