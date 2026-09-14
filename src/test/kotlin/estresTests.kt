import org.junit.jupiter.api.Test
import java.net.Socket
import kotlin.random.Random
import kotlin.test.assertEquals

class `estresTests` {
    @Test
    fun muchosUsuarios(){
        val cantidadUsuarios = 100
        val clientes = mutableListOf<Socket>()
        val nombres = mutableSetOf<String>()
        repeat(cantidadUsuarios){
            var nombre: String
            do {
                nombre = "Usuario_${Random.nextInt(100000, 999999)}"
            } while(!nombres.add(nombre))

            val socket = Socket("127.0.0.1", 2300)
            val IN = socket.getInputStream().bufferedReader(Charsets.UTF_8)
            val OUT = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)

            //enviar IDENTIFY osea identificacion osease su nombre osease su nickname
            val identify = Identify(username = nombre)
            OUT.write(JsonUtil.json.encodeToString(identify))
            OUT.newLine()
            OUT.flush()

            // recibir el RESPONSE
            val mensaje = IN.readLine()
            val respuesta = JsonUtil.json.decodeFromString<Response>(mensaje)
            assertEquals("SUCCESS",respuesta.result)
            clientes.add(socket)
            println("Conectado: $nombre")
        }
        assertEquals(cantidadUsuarios,clientes.size)
        assertEquals(cantidadUsuarios,nombres.size)
        println("================================")
        println("$cantidadUsuarios usuarios conectados")
        println("================================")
        clientes.forEach {
            it.close()
        }
    }
    @Test
    fun nombreRepetido() {
        val nombre = "UsuarioPrueba_${Random.nextInt(100000,999999)}"
        //primer cliente
        val cliente1 = Socket("127.0.0.1", 2300)
        val IN1 =  cliente1.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT1 = cliente1.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT1.write(JsonUtil.json.encodeToString(Identify(username = nombre)))
        OUT1.newLine()
        OUT1.flush()
        val respuesta1 = JsonUtil.json.decodeFromString<Response>(IN1.readLine())
        assertEquals("SUCCESS",respuesta1.result)

        //segundo cliente con el mismo nombre
        val cliente2 = Socket("127.0.0.1", 2300)
        val IN2 = cliente2.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT2 = cliente2.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT2.write(JsonUtil.json.encodeToString(Identify(username = nombre)))
        OUT2.newLine()
        OUT2.flush()

        //el segundo recibe el error
        val respuesta2 = JsonUtil.json.decodeFromString<Response>(IN2.readLine())
        println("Respuesta del servidor: $respuesta2")
        assertEquals("USER_ALREADY_EXISTS", respuesta2.result)
        assertEquals(nombre,respuesta2.extra)
        cliente1.close()
        cliente2.close()
    }

    @Test
    fun mensajeDemasiadoGrande(){
        val nombre = "Grandote_${Random.nextInt(100000,999999)}"
        val cliente = Socket("127.0.0.1", 2300)
        val IN = cliente.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT = cliente.getOutputStream().bufferedWriter(Charsets.UTF_8)

        //identificarse first
        OUT.write(JsonUtil.json.encodeToString(Identify(username = nombre)))
        OUT.newLine()
        OUT.flush()
        val respuestaIdentify = JsonUtil.json.decodeFromString<Response>(IN.readLine())
        assertEquals("SUCCESS", respuestaIdentify.result)
        // construit un texto de mas de 1 MB

        val textoGigante = "A".repeat(1_100_000)
        val mensajeGrande = PublicText(text = textoGigante)
        val mensajeJson = JsonUtil.json.encodeToString(mensajeGrande)
        println("Tamaño del mensaje a enviar: ${mensajeJson.toByteArray(Charsets.UTF_8).size} bytes")
        OUT.write(mensajeJson)
        OUT.newLine()
        OUT.flush()

        //El servidor debe de responder INVALID
        val respuesta = JsonUtil.json.decodeFromString<Response>(IN.readLine())
        println("Respuesta del servidor: $respuesta")
        assertEquals("INVALID", respuesta.result)
        assertEquals("INVALID", respuesta.operation)
        cliente.close()
    }
}