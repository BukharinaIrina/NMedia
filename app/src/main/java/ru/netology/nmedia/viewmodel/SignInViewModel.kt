package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val _data = MutableLiveData<Token>()
    val data: LiveData<Token>
        get() = _data

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun authorizationUser(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.authUser(login, password)
                _data.value = user
            } catch (e: Exception) {
                _state.postValue(FeedModelState(authError = true))
            }
        }
    }
}