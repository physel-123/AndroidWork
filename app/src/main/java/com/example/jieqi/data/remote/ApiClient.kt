package com.example.jieqi.data.remote

import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** 网络客户端：单例，供仓库层调用。 */
object ApiClient {

    private val gson = GsonBuilder().create()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // 多模态识别可能较慢
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: JieQiApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(JieQiApi::class.java)
    }

    /** 把字节数组转成 multipart 上传部件。 */
    fun imagePart(bytes: ByteArray, filename: String = "image.jpg"): MultipartBody.Part {
        val body = bytes.toRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData("file", filename, body)
    }
}
