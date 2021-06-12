package kr.ac.gachon.sw.mirrorassistant.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private var retrofitClient: Retrofit? = null

    fun getRetrofitClient(baseUrl: String) {
        retrofitClient = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}