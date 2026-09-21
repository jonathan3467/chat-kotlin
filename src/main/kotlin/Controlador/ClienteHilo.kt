package Controlador

import Modelo.Disconnected
import Modelo.JsonUtil
import Modelo.NewStatus
import Modelo.NewUser
import Modelo.PublicTextFrom
import Modelo.Response
import Modelo.TextFrom
import Modelo.UserList
import Vista.VistaCliente
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader

class ClienteHilo(
    private val IN: BufferedReader,
    private val vista: VistaCliente
) : Thread() {

    override fun run(){
        while (true){
            val mensaje = try {
                IN.readLine()
            } catch (e: Exception){
                null
            }
            if (mensaje == null){
                vista.mostrarConexionCerrada()
                break
            }
            val json = JsonUtil.json.parseToJsonElement(mensaje)
            val tipo = json.jsonObject["type"]?.jsonPrimitive?.content

            when(tipo){
                "RESPONSE" -> {
                    val respuesta = JsonUtil.json.decodeFromString<Response>(mensaje)
                    when(respuesta.result){
                        "SUCCESS" -> vista.mostrarConexionExitosa()
                        "USER_ALREADY_EXISTS" -> vista.mostrarNombreEnUso(respuesta.extra ?: "")
                        "INVALID" -> vista.mostrarMensajeInvalido()
                        "NO_SUCH_USER" -> vista.mostrarUsuarioInexistente(respuesta.extra ?: "")
                        else -> vista.mostrarRespuestaGenerica(respuesta.operation, respuesta.result)
                    }
                }
                "NEW_USER" -> {
                    val nuevoUsuario = JsonUtil.json.decodeFromString<NewUser>(mensaje)
                    vista.mostrarUsuarioConectado(nuevoUsuario.username)
                }
                "DISCONNECTED" -> {
                    val usuario = JsonUtil.json.decodeFromString<Disconnected>(mensaje)
                    vista.mostrarUsuarioDesconectado(usuario.username)
                }
                "PUBLIC_TEXT_FROM" -> {
                    val msg = JsonUtil.json.decodeFromString<PublicTextFrom>(mensaje)
                    vista.mostrarMensajePublico(msg.username, msg.text)
                }
                "TEXT_FROM" -> {
                    val msg = JsonUtil.json.decodeFromString<TextFrom>(mensaje)
                    vista.mostrarMensajePrivado(msg.username, msg.text)
                }
                "NEW_STATUS" -> {
                    val msg = JsonUtil.json.decodeFromString<NewStatus>(mensaje)
                    vista.mostrarCambioEstado(msg.username, msg.status)
                }
                "USER_LIST" -> {
                    val userList = JsonUtil.json.decodeFromString<UserList>(mensaje)
                    vista.mostrarListaUsuarios(userList.users)
                }
                else -> {
                    vista.mostrarMensajeDesconocido()
                }
            }
        }
    }
}