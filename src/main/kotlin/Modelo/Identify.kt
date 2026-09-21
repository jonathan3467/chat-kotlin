package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class Identify(
    val type: String = "IDENTIFY",
    val username: String
)
