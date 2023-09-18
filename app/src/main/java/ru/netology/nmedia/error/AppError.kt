package ru.netology.nmedia.error

sealed class AppError(private val code: Int, private val info: String) : RuntimeException()
class ApiException(code: Int, info: String) : AppError(code, info)
data object NetworkException : AppError(code = -1, info = "No_Network_Exception")
data object UnknownException : AppError(code = -2, info = "Unknown_Exception")

