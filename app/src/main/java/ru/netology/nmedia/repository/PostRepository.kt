package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<Post>>
    fun getNewerCount(postId: Long): Flow<Int>
    suspend fun getNewPosts()
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun saveWithAttachment(post: Post, model: PhotoModel)
    suspend fun upload(photoModel: PhotoModel): Media
    suspend fun authUser(login: String, password: String): Token
    suspend fun registrationUser(login: String, password: String, name: String): Token

    //suspend fun shareById(id: Long)
}