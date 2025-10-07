package com.example.task5

import com.google.gson.annotations.SerializedName

data class Submission(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("timestamp") val timestamp: Long? = null
)
