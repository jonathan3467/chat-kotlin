package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class PublicText(
    val type: String = "PUBLIC_TEXT",
    val text: String
)

@Serializable
data class PublicTextFrom(
    val type: String = "PUBLIC_TEXT_FROM",
    val username: String,
    val text: String
)