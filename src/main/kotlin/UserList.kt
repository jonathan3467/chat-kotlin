import kotlinx.serialization.Serializable

@Serializable
data class UserList(
    val type: String = "USER_LIST",
    val users: Map<String, String>
)
