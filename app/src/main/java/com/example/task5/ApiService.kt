package com.example.task5

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("userProfile/{username}")
    fun getUserProfile(@Path("username") username: String): Call<UserProfileResponse>

    @GET("{username}/badges")
    fun getBadges(@Path("username") username: String): Call<BadgesResponse>

    @GET("{username}/solved")
    fun getSolved(@Path("username") username: String): Call<SolvedResponse>

    @GET("{username}/submission")
    fun getSubmissionsWrapped(
        @Path("username") username: String,
        @Query("limit") limit: Int
    ): Call<SubmissionsResponse>
}
