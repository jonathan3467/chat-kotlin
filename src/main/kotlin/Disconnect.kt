import kotlinx.serialization.Serializable

@Serializable
data class Disconnect(
    val type: String = "DISCONNECT"
)
