package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.dto.TypeAttachment
import ru.netology.nmedia.model.PhotoModel

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {

    override val data = dao.getAll()
        .map { it.toDto() }
        .flowOn(Dispatchers.Default)

    override fun getNewerCount(postId: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000L)
                val response = Api.service.getNewer(postId)
                if (!response.isSuccessful) {
                    throw ApiException(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiException(response.code(), response.message())
                dao.insert(body.toEntity())
                emit(body.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                throw e
            } catch (e: IOException) {
                throw NetworkException
            } catch (e: Exception) {
                throw UnknownException
            }
        }
    }
        .flowOn(Dispatchers.Default)

    override suspend fun getNewPosts() {
        try {
            dao.hiddenPosts()
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun getAll() {
        try {
            val response = Api.service.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(body.toEntity()
                .map {
                    it.copy(hidden = true)
                })
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = Api.service.savePost(post)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = Api.service.deletePost(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            dao.likeById(id)
            val response = Api.service.likePost(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(
                PostEntity.fromDto(body)
                    .copy(hidden = true)
            )
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            dao.likeById(id)
            val response = Api.service.unlikePost(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(
                PostEntity.fromDto(body)
                    .copy(hidden = true)
            )
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun saveWithAttachment(post: Post, model: PhotoModel) {
        try {
            val media = upload(model)
            val response = Api.service.savePost(
                post.copy(
                    attachment = Attachment(
                        url = media.id,
                        type = TypeAttachment.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun upload(photoModel: PhotoModel): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", photoModel.file!!.name, photoModel.file.asRequestBody()
            )
            val response = Api.service.saveMedia(media)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun authUser(login: String, password: String): Token {
        try {
            val response = Api.service.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun registrationUser(login: String, password: String, name: String): Token {
        try {
            val response = Api.service.registrationUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    //override suspend fun shareById(id: Long) {}
}