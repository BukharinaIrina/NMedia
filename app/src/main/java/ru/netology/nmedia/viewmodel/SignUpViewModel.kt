package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.UnknownException
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val _data = MutableLiveData<Token>()
    val data: LiveData<Token>
        get() = _data

    fun registrationUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                val user = repository.registrationUser(login, password, name)
                _data.value = user
            } catch (e: Exception) {
                throw UnknownException
            }
        }
    }
}