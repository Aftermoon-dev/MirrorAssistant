package kr.ac.gachon.sw.mirrorassistant.network

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

@JvmSuppressWildcards
interface APICall {
    @GET("androidCheck")
    fun validCheck(): Call<BaseResponse>

    @GET("getfacelist")
    fun getFaceList(): Call<FaceListResponse>

    @Multipart
    @POST("newface")
    fun addNewFace(
        //@PartMap params: Map<String, RequestBody>
        @Part photoFile: MultipartBody.Part,
        @Part name: MultipartBody.Part
    ): Call<BaseResponse>

    @POST("removeface")
    @FormUrlEncoded
    fun removeFace(
        @Field("id") id: Int,
    ): Call<BaseResponse>

    @POST("setfacelayout")
    @FormUrlEncoded
    fun setFaceLayout(
        @Field("id") id: Int,
        @Field("clock") clock: Int,
        @Field("news") news: Int,
        @Field("weather") weather: Int,
        @Field("noti") noti: Int
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST("setNews")
    fun setNews(
        @Field("id") id: Int,
        @Field("newsid") newsid: Int,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST("newnoti")
    fun sendNewNotification(
        @Field("appName") appName: String,
        @Field("title") title: String,
        @Field("msg") msg: String,
        @Field("time") time: String
    ): Call<BaseResponse>
}