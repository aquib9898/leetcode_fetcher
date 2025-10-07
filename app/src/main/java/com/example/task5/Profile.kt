package com.example.task5

import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("username") val username: String?,
    @SerializedName("ranking") val ranking: Int? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("about") val about: String? = null
)
