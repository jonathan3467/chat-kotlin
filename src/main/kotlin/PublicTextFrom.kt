import kotlinx.serialization.Serializable

@Serializable
data class PublicTextFrom(
    val type: String = "PUBLIC_TEXT_FROM",
    val username: String,
    val text: String
)
