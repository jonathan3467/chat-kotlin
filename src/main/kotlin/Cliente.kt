import java.net.Socket
import java.util.Scanner

fun main() {
    val sn = Scanner(System.`in`)
    sn.useDelimiter("\n")
    val cliente = Socket("127.0.0.1", 2300)
    val IN = cliente.getInputStream().bufferedReader(Charsets.UTF_8)
    val OUT = cliente.getOutputStream().bufferedWriter(Charsets.UTF_8)

    print("Indica tu nombre: ")
    val nombre = sn.next()
    val identify = Identify(username = nombre)
    val mensajeJson = JsonUtil.json.encodeToString(identify)
    OUT.write(mensajeJson)
    OUT.newLine()
    OUT.flush()

    val hilo = ClienteHilo(IN)
    hilo.start()
    while (hilo.isAlive){
        val texto = sn.next()
        if (texto.equals("<desconectar>", ignoreCase = true)){
            val disconnect = Disconnect()
            val mensajeJson = JsonUtil.json.encodeToString(disconnect)
            OUT.write(mensajeJson)
            OUT.newLine()
            OUT.flush()
            break //lo hace salir del while para ya no mandar mensajes
        }

        if (texto.startsWith("<estado>", ignoreCase = true)){
            val nuevoEstado = texto.substringAfter("<estado>").trim().uppercase()
            val status = Status(status = nuevoEstado)
            val mensajeJson = JsonUtil.json.encodeToString(status)
            OUT.write(mensajeJson)
            OUT.newLine()
            OUT.flush()
            continue //para no mandarlo como texto publico
        }

        if (texto.equals("<usuarios>", ignoreCase = true)){
            val users = Users()
            val mensajeJson = JsonUtil.json.encodeToString(users)
            OUT.write(mensajeJson)
            OUT.newLine()
            OUT.flush()
            continue
        }

        if (texto.startsWith("<privado>", ignoreCase = true)){
            val resto = texto.substringAfter("<privado>").trim()
            val partes = resto.split(" ", limit = 2)
            if(partes.size == 2){
                val destinatario = partes[0]
                val textoPrivado = partes[1]
                val textMsg = Text(username = destinatario, text = textoPrivado)
                val mensajeJson = JsonUtil.json.encodeToString(textMsg)
                OUT.write(mensajeJson)
                OUT.newLine()
                OUT.flush()
            } else{
                println("Uso: <privado> usuario mensaje")
            }
            continue
        }

        val textoPublico = PublicText(text = texto)
        val mensajeJson = JsonUtil.json.encodeToString(textoPublico)
        OUT.write(mensajeJson)
        OUT.newLine()
        OUT.flush()
    }
    cliente.close()
}