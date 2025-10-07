package com.example.task5

import com.google.gson.annotations.SerializedName

data class BadgesResponse(
    @SerializedName("badges") val badges: List<String> = emptyList()
)
