package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.UnknownException
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

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