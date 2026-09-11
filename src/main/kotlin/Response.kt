import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val type: String = "RESPONSE",
    val operation: String,
    val result: String,
    val extra: String? = null
)
