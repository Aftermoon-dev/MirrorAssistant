package kr.ac.gachon.sw.mirrorassistant.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofitClient: Retrofit? = null

    fun getNewRetrofitClient(baseUrl: String): Retrofit? {
        retrofitClient = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofitClient
    }

    fun getCurrentRetrofitClient(): Retrofit? {
        return retrofitClient
    }

}