package com.example.task5

import com.google.gson.annotations.SerializedName

data class SubmissionsResponse(
    @SerializedName("count") val count: Int? = null,
    // JSON key is "submission" (singular) — map it here
    @SerializedName("submission") val submission: List<RecentSubmission>? = emptyList()
)
