package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0L,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0L,
    attachment = null,
//    shares = 0L,
//    views = 0L,
//    video = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> = _data
    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true, refreshing = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.value = FeedModel(posts = result, empty = result.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true, errorMessage = e.message.toString())
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(result: Post) {
                    _postCreated.value = Unit
                }

                override fun onError(e: Exception) {
                    _data.value = FeedModel(error = true, errorMessage = e.message.toString())
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEditing() {
        edited.value = empty
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content != text) {
            edited.value = edited.value?.copy(content = text)
        }
    }

    fun removeById(id: Long) {
        val old = _data.value
        _data.value = old?.copy(
            posts = old.posts.filter { it.id != id }
        )
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {}

            override fun onError(e: Exception) {
                _data.value = old
            }
        })
    }

    fun likeById(id: Long) {
        repository.likeByIdAsync(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                val updatePosts = _data.value?.posts?.map {
                    if (it.id == result.id) {
                        result
                    } else {
                        it
                    }
                }.orEmpty()

                _data.value = _data.value?.copy(posts = updatePosts)
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true, errorMessage = e.message.toString())
            }
        })
    }

    fun unlikeById(id: Long) {
        repository.unlikeByIdAsync(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                val updatePosts = _data.value?.posts?.map {
                    if (it.id == result.id) {
                        result
                    } else {
                        it
                    }
                }.orEmpty()

                _data.value = _data.value?.copy(posts = updatePosts)
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true, errorMessage = e.message.toString())
            }
        })
    }

    fun shareById(id: Long) {}
}