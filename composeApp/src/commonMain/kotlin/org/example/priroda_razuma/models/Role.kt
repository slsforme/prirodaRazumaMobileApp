package org.example.priroda_razuma.models

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val description: String?,
)