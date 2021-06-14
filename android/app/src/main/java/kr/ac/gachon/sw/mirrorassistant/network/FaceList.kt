package kr.ac.gachon.sw.mirrorassistant.network

import com.google.gson.annotations.SerializedName

data class FaceList (
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("create") val create: String,
    @SerializedName("clock") val clock: Int,
    @SerializedName("weather") val weather: Int,
    @SerializedName("news") val news: Int,
    @SerializedName("newsid") val newsid: Int,
    @SerializedName("noti") val noti: Int
)
