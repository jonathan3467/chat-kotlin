import kotlinx.serialization.Serializable

@Serializable
data class PublicText(
    val type: String = "PUBLIC_TEXT",
    val text: String
)
