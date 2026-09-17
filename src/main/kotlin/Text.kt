import kotlinx.serialization.Serializable

@Serializable
data class Text(
    val type: String = "TEXT",
    val username: String,
    val text: String
)
