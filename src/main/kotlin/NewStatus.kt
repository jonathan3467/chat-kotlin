import kotlinx.serialization.Serializable

@Serializable
data class NewStatus(
    val type: String = "NEW_STATUS",
    val username: String,
    val status: String
)
