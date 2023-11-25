package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authFlow = MutableStateFlow<Token?>(null)
    val authFlow = _authFlow.asStateFlow()

    init {
        val id = prefs.getLong(ID_KEY, 0L)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            prefs.edit { clear() }
        } else {
            _authFlow.value = Token(id, token)
        }

        sendPushToken()
    }

    fun setAuth(token: Token) {
        prefs.edit {
            putLong(ID_KEY, token.id)
            putString(TOKEN_KEY, token.token)
        }
        _authFlow.value = token

        sendPushToken()
    }

    fun removeAuth() {
        prefs.edit { clear() }
        _authFlow.value = null

        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                Api.service.savePushToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        @Volatile
        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE)

        fun initAuth(context: Context) {
            INSTANCE = AppAuth(context.applicationContext)
        }
    }
}