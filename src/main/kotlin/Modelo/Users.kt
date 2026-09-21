package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class Users(
    val type: String = "USERS"
)

@Serializable
data class UserList(
    val type: String = "USER_LIST",
    val users: Map<String, String>
)
