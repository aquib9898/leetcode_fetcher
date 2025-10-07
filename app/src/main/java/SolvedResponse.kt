package com.example.task5

import com.google.gson.annotations.SerializedName

data class SolvedResponse(
    @SerializedName("totalSolved") val totalSolved: Int? = null,
    @SerializedName("easy") val easy: Int? = null,
    @SerializedName("medium") val medium: Int? = null,
    @SerializedName("hard") val hard: Int? = null
)
