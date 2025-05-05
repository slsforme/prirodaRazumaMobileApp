package org.example.priroda_razuma.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val fio: String,
    val login: String,
    val role_id: Int,
    val email: String?,
    val photo_url: String?,
    val created_at: String,
    val updated_at: String,
    val active: Boolean
)