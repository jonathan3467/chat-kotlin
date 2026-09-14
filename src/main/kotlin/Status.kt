import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val type: String = "STATUS",
    val status: String
)
