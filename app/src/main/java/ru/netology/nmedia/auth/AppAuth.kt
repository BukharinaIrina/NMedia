package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val _authFlow = MutableStateFlow<Token?>(null)
    val authFlow = _authFlow.asStateFlow()

    init {
        val id = prefs.getLong(idKey, 0L)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            prefs.edit { clear() }
        } else {
            _authFlow.value = Token(id, token)
        }

        sendPushToken()
    }

    fun setAuth(token: Token) {
        prefs.edit {
            putLong(idKey, token.id)
            putString(tokenKey, token.token)
        }
        _authFlow.value = token

        sendPushToken()
    }

    fun removeAuth() {
        prefs.edit { clear() }
        _authFlow.value = null

        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().savePushToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}