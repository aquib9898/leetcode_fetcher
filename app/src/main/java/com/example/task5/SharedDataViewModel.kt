package com.example.task5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ConcurrentHashMap

class SharedDataViewModel : ViewModel() {

    private val _profile = MutableLiveData<UserProfileResponse?>()
    val profile: LiveData<UserProfileResponse?> = _profile

    private val _badges = MutableLiveData<BadgesResponse?>()
    val badges: LiveData<BadgesResponse?> = _badges

    private val _solved = MutableLiveData<UserProfileResponse?>()
    val solved: LiveData<UserProfileResponse?> = _solved

    private val _submissions = MutableLiveData<List<Submission>>()
    val submissions: LiveData<List<Submission>> = _submissions

    private val submissionsCache = ConcurrentHashMap<String, List<Submission>>()

    private var profileCall: Call<UserProfileResponse>? = null
    private var badgesCall: Call<BadgesResponse>? = null
    private var solvedCall: Call<UserProfileResponse>? = null
    private var submissionsCall: Call<SubmissionsResponse>? = null

    fun loadProfile(username: String) {
        val current = _profile.value
        if (current != null && (current.username?.equals(username, true) == true)) return

        profileCall?.cancel()
        profileCall = RetrofitClient.apiService.getUserProfile(username)
        profileCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) _profile.postValue(response.body()) else _profile.postValue(null)
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                if (!call.isCanceled) _profile.postValue(null)
            }
        })
    }

    fun loadBadges(username: String) {
        val current = _badges.value
        if (current != null) return

        badgesCall?.cancel()
        badgesCall = RetrofitClient.apiService.getBadges(username)
        badgesCall?.enqueue(object : Callback<BadgesResponse> {
            override fun onResponse(call: Call<BadgesResponse>, response: Response<BadgesResponse>) {
                if (response.isSuccessful) _badges.postValue(response.body()) else _badges.postValue(null)
            }

            override fun onFailure(call: Call<BadgesResponse>, t: Throwable) {
                if (!call.isCanceled) _badges.postValue(null)
            }
        })
    }

    fun loadSolved(username: String) {
        solvedCall?.cancel()
        solvedCall = RetrofitClient.apiService.getUserProfile(username)
        solvedCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) _solved.postValue(response.body()) else _solved.postValue(null)
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                if (!call.isCanceled) _solved.postValue(null)
            }
        })
    }

    fun loadSubmissionsWrapped(username: String, limit: Int) {
        val key = "$username|$limit"
        val cached = submissionsCache[key]
        if (cached != null) {
            _submissions.postValue(cached)
            return
        }

        submissionsCall?.cancel()
        submissionsCall = RetrofitClient.apiService.getSubmissionsWrapped(username, limit)
        submissionsCall?.enqueue(object : Callback<SubmissionsResponse> {
            override fun onResponse(call: Call<SubmissionsResponse>, response: Response<SubmissionsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val list = body.submission?.map {
                        Submission(
                            id = null,
                            title = it.title,
                            status = it.statusDisplay ?: it.status ?: "-",
                            lang = it.lang,
                            timestamp = it.timestamp?.toLongOrNull()
                        )
                    } ?: emptyList()
                    submissionsCache[key] = list
                    _submissions.postValue(list)
                } else {
                    _submissions.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<SubmissionsResponse>, t: Throwable) {
                if (!call.isCanceled) _submissions.postValue(emptyList())
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        profileCall?.cancel()
        badgesCall?.cancel()
        solvedCall?.cancel()
        submissionsCall?.cancel()
    }
}
