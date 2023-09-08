package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Unit>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun likeByIdAsync(id: Long, callback: Callback<Unit>)
    fun unlikeByIdAsync(id: Long, callback: Callback<Unit>)

    //fun shareByIdAsync(id: Long, callback: Callback<Unit>)

    interface Callback<T> {
        fun onSuccess(posts: T)
        fun onError(e: Exception)
    }
}