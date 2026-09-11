import kotlinx.serialization.Serializable

@Serializable
data class Disconnected(
    val type: String = "DISCONNECTED",
    val username: String
)
