package com.example.task5

import com.google.gson.annotations.SerializedName

data class Badge(
    @SerializedName("id") val id: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("creationDate") val creationDate: String? = null
)

data class BadgesResponse(
    @SerializedName("badgesCount") val badgesCount: Int? = null,
    @SerializedName("badges") val badges: List<Badge> = emptyList(),
    @SerializedName("upcomingBadges") val upcomingBadges: List<Badge> = emptyList(),
    @SerializedName("activeBadge") val activeBadge: Badge? = null
)
