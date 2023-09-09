package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
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
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(context = application).postDao()
    )
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
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Unit> {
                override fun onSuccess(posts: Unit) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        edited.postValue(empty)
    }

    fun edit(post: Post) {
        edited.postValue(post)
    }

    fun cancelEditing() {
        edited.postValue(empty)
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content != text) {
            edited.value = edited.value?.copy(content = text)
        }
    }

    fun removeById(id: Long) {
        val old = _data.value
        _data.postValue(
            old?.copy(
                posts = old.posts.filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(posts: Unit) {}

            override fun onError(e: Exception) {
                _data.postValue(old)
            }
        })
    }

    fun likeById(id: Long) {
        repository.likeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(posts: Unit) {
                _postCreated.postValue(Unit)
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun unlikeById(id: Long) {
        repository.unlikeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(posts: Unit) {
                _postCreated.postValue(Unit)
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun shareById(id: Long) {}
}