package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val hidden: Boolean = false,
    @Embedded
    var attachment: Attachment?,
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        content,
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(published), ZoneId.systemDefault()),
        likedByMe,
        likes,
        hidden,
        attachment
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published.toEpochSecond(),
                dto.likedByMe,
                dto.likes,
                dto.hidden,
                dto.attachment,
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)


