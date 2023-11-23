package ru.netology.nmedia.repository

import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.IOException

class AuthRepository {

    suspend fun authUser(login: String, password: String): Token {
        try {
            val response = PostsApi.service.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    suspend fun registrationUser(login: String, password: String, name: String): Token {
        try {
            val response = PostsApi.service.registrationUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}