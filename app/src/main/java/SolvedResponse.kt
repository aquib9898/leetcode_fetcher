package com.example.task5

import com.google.gson.annotations.SerializedName

data class SolvedResponse(
    @SerializedName("solvedProblem") val solvedProblem: Int? = null,
    @SerializedName("easySolved") val easySolved: Int? = null,
    @SerializedName("mediumSolved") val mediumSolved: Int? = null,
    @SerializedName("hardSolved") val hardSolved: Int? = null,
    @SerializedName("totalSubmissionNum") val totalSubmissionNum: List<SubmissionCount>? = null,
    @SerializedName("acSubmissionNum") val acSubmissionNum: List<SubmissionCount>? = null
)
