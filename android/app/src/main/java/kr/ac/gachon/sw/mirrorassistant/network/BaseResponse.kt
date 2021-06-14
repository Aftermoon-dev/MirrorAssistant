package kr.ac.gachon.sw.mirrorassistant.network

import com.google.gson.annotations.SerializedName

data class BaseResponse (
    @SerializedName("code") val code: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val message: String
)

