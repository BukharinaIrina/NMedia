package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {
    val state = AppAuth.getInstance().authFlow
        .asLiveData()

    val authenticated: Boolean
        get() = AppAuth.getInstance().authFlow.value?.token != null
}