package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class NewUser(
    val type: String = "NEW_USER",
    val username: String
)
