package ru.netology.nmedia.module

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.Constants
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "${Constants.API_URL}/api/slow/"
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(
        appAuth: AppAuth
    ): Interceptor = Interceptor { chain ->
        val request = appAuth.authFlow.value?.token?.let {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", it)
                .build()
        } ?: chain.request()

        chain.proceed(request)
    }

    @Singleton
    @Provides
    fun provideClient(
        authInterceptor: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(
                        OffsetDateTime::class.java,
                        object : TypeAdapter<OffsetDateTime>() {
                            override fun write(out: JsonWriter, value: OffsetDateTime) {
                                out.value(value.toEpochSecond())
                            }

                            override fun read(`in`: JsonReader): OffsetDateTime =
                                OffsetDateTime.ofInstant(
                                    Instant.ofEpochSecond(`in`.nextLong()),
                                    ZoneId.systemDefault()
                                )

                        })
                    .create()
            )
        )
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    @Singleton
    @Provides
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create()
}

