package org.example.priroda_razuma.models

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: Int,
    val name: String,
    val patient_id: Int,
    val subdirectory_type: String,
    val author_id: Int? = null,
    val file_path: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)


