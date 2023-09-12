package ru.netology.nmedia.util

enum class TypeAttachment {
    IMAGE
}

data class Attachment(
    val url: String,
    val description: String,
    val type: TypeAttachment = TypeAttachment.IMAGE
)

