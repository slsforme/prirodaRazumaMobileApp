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
    val active: Boolean,
)

@Serializable
data class CreateUserRequest(
    val id: Int,
    val fio: String,
    val login: String,
    val role_id: Int,
    val email: String?,
    val active: Boolean,
    val photo_url: String?,
    val password: String,
)

@Serializable
data class UpdateUserRequest(
    val fio: String,
    val login: String,
    val email: String?,
    val active: Boolean,
    val role_id: Int,
    val photo_url: String?,
    val password: String? = null
)
