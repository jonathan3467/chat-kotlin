package Modelo

import kotlinx.serialization.json.Json

object JsonUtil {
    val json = Json{
        encodeDefaults = true
    }
}