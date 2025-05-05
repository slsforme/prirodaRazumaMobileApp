package org.example.priroda_razuma.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse (
    val detail: String
)