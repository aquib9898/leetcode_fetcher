package com.example.task5

import com.google.gson.annotations.SerializedName

data class RecentSubmission(
    @SerializedName("title") val title: String? = null,
    @SerializedName("titleSlug") val titleSlug: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("statusDisplay") val statusDisplay: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("lang") val lang: String? = null
)
