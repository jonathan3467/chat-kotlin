import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val type: String = "STATUS",
    val status: String
)

@Serializable
data class NewStatus(
    val type: String = "NEW_STATUS",
    val username: String,
    val status: String
)

