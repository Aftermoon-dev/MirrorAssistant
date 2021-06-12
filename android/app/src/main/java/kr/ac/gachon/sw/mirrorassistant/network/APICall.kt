package kr.ac.gachon.sw.mirrorassistant.network

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface APICall {
    @GET("androidCheck/")
    fun validCheck(): Response<JsonElement>

    @GET("getfacelist/")
    fun getFaceList(): Response<JsonElement>

    @Multipart
    @FormUrlEncoded
    @POST("newface/")
    fun addNewFace(
        @Part photoFile: MultipartBody.Part
    ): Response<JsonElement>

    @POST("setfacelayout/")
    @FormUrlEncoded
    fun setFaceLayout(
        @Field("clock") clock: Int,
        @Field("news") news: Int,
        @Field("weather") weather: Int,
        @Field("noti") noti: Int
    ): Response<JsonElement>

    @Multipart
    @FormUrlEncoded
    @POST("newnoti/")
    fun sendNewNotification(
        @Field("title") title: String,
        @Field("msg") msg: String
    ): Response<JsonElement>
}