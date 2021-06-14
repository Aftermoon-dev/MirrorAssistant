package kr.ac.gachon.sw.mirrorassistant.network

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class FaceListResponse (
    @SerializedName("code") val code: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val message: String,
    @SerializedName("facelist") val faceList: List<FaceList>
)