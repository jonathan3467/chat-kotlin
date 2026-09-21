package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class Disconnect(
    val type: String = "DISCONNECT"
)

@Serializable
data class Disconnected(
    val type: String = "DISCONNECTED",
    val username: String
)
