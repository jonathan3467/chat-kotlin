import kotlinx.serialization.Serializable

@Serializable
data class TextFrom(
    val type: String = "TEXT_FROM",
    val username: String,
    val text: String
)
