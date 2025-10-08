package com.example.task5

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("username") val username: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("ranking") val ranking: Int? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("profileImage") val profileImage: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("contributionPoint") val contributionPoint: Int? = null,
    @SerializedName("totalSolved") val totalSolved: Int? = null,
    @SerializedName("easySolved") val easySolved: Int? = null,
    @SerializedName("mediumSolved") val mediumSolved: Int? = null,
    @SerializedName("hardSolved") val hardSolved: Int? = null,
    @SerializedName("matchedUserStats") val matchedUserStats: MatchedUserStats? = null,
    @SerializedName("totalSubmissions") val totalSubmissions: List<SubmissionCount>? = null,
    @SerializedName("recentSubmissions") val recentSubmissions: List<RecentSubmission>? = null
)

data class MatchedUserStats(
    @SerializedName("acSubmissionNum") val acSubmissionNum: List<SubmissionCount>? = null
)

data class SubmissionCount(
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("submissions") val submissions: Int? = null
)
