package org.example.priroda_razuma.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse (
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user_id: Int
)