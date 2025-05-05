package org.example.priroda_razuma.models

import kotlinx.serialization.Serializable

@Serializable
enum class SubDirectories(val value: String) {
    DIAGNOSTICS("Диагностика"),
    ANAMNESIS("Анамнез"),
    WORK_PLAN("План работы"),
    COMMENTS("Комментарии специалистов"),
    PHOTOS_AND_VIDEOS("Фотографии и Видео");

    companion object {
        fun fromValue(value: String): SubDirectories? {
            return entries.find { it.value == value }
        }
    }
}