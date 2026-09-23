import Modelo.Identify
import Modelo.Invitation
import Modelo.Invite
import Modelo.JoinRoom
import Modelo.JoinedRoom
import Modelo.JsonUtil
import Modelo.LeaveRoom
import Modelo.LeftRoom
import Modelo.NewRoom
import Modelo.PublicText
import Modelo.Response
import Modelo.RoomText
import Modelo.RoomTextFrom
import Modelo.RoomUserList
import Modelo.RoomUsers
import Modelo.Status
import Modelo.Text
import Modelo.TextFrom
import Modelo.UserList
import Modelo.Users
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
                nombre = "U${Random.nextInt(100000, 999999)}"
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
        val nombre = "NR${Random.nextInt(100000,999999)}"
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
        val nombre = "G${Random.nextInt(100000,999999)}"
        val cliente = Socket("127.0.0.1", 2300)
        val IN = cliente.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT = cliente.getOutputStream().bufferedWriter(Charsets.UTF_8)

        //identificarse first osea primero osease antes que el segundo osease....
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

    @Test
    fun listaDeUsuarios() {
        //que se conecten 3 usuarios de prueba
        val nombre1 = "L1${Random.nextInt(100000,999999)}"
        val nombre2 = "L2${Random.nextInt(100000,999999)}"
        val nombre3 = "L3${Random.nextInt(100000,999999)}"

        val cliente1 = Socket("127.0.0.1", 2300)
        val IN1 = cliente1.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT1 = cliente1.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT1.write(JsonUtil.json.encodeToString(Identify(username = nombre1)))
        OUT1.newLine(); OUT1.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(IN1.readLine()).result)

        val cliente2 = Socket("127.0.0.1", 2300)
        val IN2 = cliente2.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT2 = cliente2.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT2.write(JsonUtil.json.encodeToString(Identify(username = nombre2)))
        OUT2.newLine(); OUT2.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(IN2.readLine()).result)
        IN1.readLine() // el cliente 1 recibe el NEW_USER de cliente2 el cual lo ignoramos aqui

        val cliente3 = Socket("127.0.0.1", 2300)
        val IN3 = cliente3.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT3 = cliente3.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT3.write(JsonUtil.json.encodeToString(Identify(username = nombre3)))
        OUT3.newLine(); OUT3.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(IN3.readLine()).result)
        IN1.readLine() //cliente1 recibe el NEW_USER de cliente3
        IN2.readLine() //cliente 2 recibe NEW_USER de cliente3

        //cliente2 cambia su estado a AWAY
        OUT2.write(JsonUtil.json.encodeToString(Status(status = "AWAY")))
        OUT2.newLine(); OUT2.flush()
        // cliente1 y el 3 reciben el NEW_status del cliemte 2
        IN1.readLine()
        IN3.readLine()

        //cliemte 1 pide la lista de usuarios
        OUT1.write(JsonUtil.json.encodeToString(Users()))
        OUT1.newLine(); OUT1.flush()

        val respuesta = JsonUtil.json.decodeFromString<UserList>(IN1.readLine())
        println("Lista recibida: ${respuesta.users}")
        assertEquals("ACTIVE",respuesta.users[nombre1])
        assertEquals("AWAY",respuesta.users[nombre2])
        assertEquals("ACTIVE",respuesta.users[nombre3])

        cliente1.close()
        cliente2.close()
        cliente3.close()
    }

    @Test    //haremos dos uno para el caso que si exita el usuario, y el segundo en caso de que no
    fun mensajePrivadoExitoso(){
        val nombreA = "A${Random.nextInt(100000,999999)}"
        val nombreB = "B${Random.nextInt(100000,999999)}"

        val clienteA = Socket("127.0.0.1", 2300)
        val INa = clienteA.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUTa = clienteA.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUTa.write(JsonUtil.json.encodeToString(Identify(username = nombreA)))
        OUTa.newLine(); OUTa.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INa.readLine()).result)

        val clienteB = Socket("127.0.0.1", 2300)
        val INb = clienteB.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUTb = clienteB.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUTb.write(JsonUtil.json.encodeToString(Identify(username = nombreB)))
        OUTb.newLine(); OUTb.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INb.readLine()).result)
        INa.readLine() //A recibe el NEW_USER de B

        // A le manda un texto privado a B
        val texto = "Hola B, estes es un mensaje privado"
        val textMsg = Text(username = nombreB, text = texto)
        OUTa.write(JsonUtil.json.encodeToString(textMsg))
        OUTa.newLine(); OUTa.flush()

        // B debe de recibir el TEXT_FROM
        val recibido = JsonUtil.json.decodeFromString<TextFrom>(INb.readLine())
        println("B recibio: $recibido")
        assertEquals(nombreA, recibido.username)
        assertEquals(texto,recibido.text)
        clienteA.close()
        clienteB.close()
    }

    @Test
    fun mensajePrivadoAUsuarioInexistente(){
        val nombreA = "PC${Random.nextInt(100000,999999)}"
        val destinoFalso = "NE${Random.nextInt(100000,999999)}"

        val clienteA = Socket("127.0.0.1", 2300)
        val INa = clienteA.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUTa = clienteA.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUTa.write(JsonUtil.json.encodeToString(Identify(username = nombreA)))
        OUTa.newLine(); OUTa.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INa.readLine()).result)

        //A intenta mndarle un texto privado a alguien que no existe
        val textMsg = Text(username = destinoFalso, text = "hola?")
        OUTa.write(JsonUtil.json.encodeToString(textMsg))
        OUTa.newLine(); OUTa.flush()

        //El servidro debe de responer NO_SUCH_USER
        val respuesta = JsonUtil.json.decodeFromString<Response>(INa.readLine())
        println("Respuesta: $respuesta")
        assertEquals("TEXT",respuesta.operation)
        assertEquals("NO_SUCH_USER", respuesta.result)
        assertEquals(destinoFalso, respuesta.extra)
        clienteA.close()
    }

    @Test
    fun Salas(){
        val nombreA = "SA${Random.nextInt(1000,9999)}"
        val nombreB = "SA${Random.nextInt(1000,9999)}"
        val sala = "R${Random.nextInt(1000,9999)}"

        val clienteA = Socket("127.0.0.1", 2300)
        val INa = clienteA.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUTa = clienteA.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUTa.write(JsonUtil.json.encodeToString(Identify(username = nombreA))); OUTa.newLine(); OUTa.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INa.readLine()).result)

        val clienteB = Socket("127.0.0.1", 2300)
        val INb = clienteB.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUTb = clienteB.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUTb.write(JsonUtil.json.encodeToString(Identify(username = nombreB))); OUTb.newLine(); OUTb.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INb.readLine()).result)
        INa.readLine() // A recibe NEW_USER de B

        //A crea sala
        OUTa.write(JsonUtil.json.encodeToString(NewRoom(roomname = sala))); OUTa.newLine(); OUTa.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INa.readLine()).result)

        //A invita a B
        OUTa.write(JsonUtil.json.encodeToString(Invite(roomname = sala, usernames = listOf(nombreB)))); OUTa.newLine(); OUTa.flush()
        val invitacion = JsonUtil.json.decodeFromString<Invitation>(INb.readLine())
        assertEquals(nombreA, invitacion.username)

        //B se une
        OUTb.write(JsonUtil.json.encodeToString(JoinRoom(roomname = sala))); OUTb.newLine(); OUTb.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(INb.readLine()).result)
        val joined = JsonUtil.json.decodeFromString<JoinedRoom>(INa.readLine())
        assertEquals(nombreB, joined.username)

        //A pide la lista de usuarios de la salecion es como sala pero mejor
        OUTa.write(JsonUtil.json.encodeToString(RoomUsers(roomname = sala))); OUTa.newLine(); OUTa.flush()
        val lista = JsonUtil.json.decodeFromString<RoomUserList>(INa.readLine())
        assertEquals(2, lista.users.size)
        assertEquals("ACTIVE", lista.users[nombreB])

        //A manda texto a la sala
        OUTa.write(JsonUtil.json.encodeToString(RoomText(roomname = sala, text = "hola sala"))); OUTa.newLine(); OUTa.flush()
        val texto = JsonUtil.json.decodeFromString<RoomTextFrom>(INb.readLine())
        assertEquals("hola sala", texto.text)

        //B sale de la sala y A lo wacha
        OUTb.write(JsonUtil.json.encodeToString(LeaveRoom(roomname = sala))); OUTb.newLine(); OUTb.flush()
        val left = JsonUtil.json.decodeFromString<LeftRoom>(INa.readLine())
        assertEquals(nombreB, left.username)
        clienteA.close()
        clienteB.close()
    }

    @Test
    fun erroresDeSalas(){
        val nombre = "ER${Random.nextInt(1000,9999)}"
        val cliente = Socket("127.0.0.1", 2300)
        val IN = cliente.getInputStream().bufferedReader(Charsets.UTF_8)
        val OUT = cliente.getOutputStream().bufferedWriter(Charsets.UTF_8)
        OUT.write(JsonUtil.json.encodeToString(Identify(username = nombre))); OUT.newLine(); OUT.flush()
        assertEquals("SUCCESS", JsonUtil.json.decodeFromString<Response>(IN.readLine()).result)

        //unirse a una sala que no existe
        OUT.write(JsonUtil.json.encodeToString(JoinRoom(roomname = "NoExiste"))); OUT.newLine(); OUT.flush()
        val r1 = JsonUtil.json.decodeFromString<Response>(IN.readLine())
        assertEquals("NO_SUCH_ROOM", r1.result)

        //crear sala y unirse sin ser invitado
        OUT.write(JsonUtil.json.encodeToString(NewRoom(roomname = "SolaR"))); OUT.newLine(); OUT.flush()
        IN.readLine()

        OUT.write(JsonUtil.json.encodeToString(RoomUsers(roomname = "NoExiste"))); OUT.newLine(); OUT.flush()
        val r2 = JsonUtil.json.decodeFromString<Response>(IN.readLine())
        assertEquals("NO_SUCH_ROOM", r2.result)
        cliente.close()
    }
}