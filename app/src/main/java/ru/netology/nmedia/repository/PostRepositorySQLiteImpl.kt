package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post

class PostRepositorySQLiteImpl(
    private val dao: PostDao
) : PostRepository {
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)

    init {
        posts = dao.getAll()
        data.value = posts
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun save(post: Post) {
        dao.save(post)
        posts = dao.getAll()
        data.value = posts
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
        posts = dao.getAll()
        data.value = posts
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
        posts = dao.getAll()
        data.value = posts
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
        posts = dao.getAll()
        data.value = posts
    }
}