package org.example.priroda_razuma.models

import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: Int,
    val fio: String,
    val date_of_birth: String,
    val created_at: String,
    val updated_at: String
)