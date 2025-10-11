package com.example.task5

import android.util.Log
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

    private val _profileLoading = MutableLiveData<Boolean>(false)
    val profileLoading: LiveData<Boolean> = _profileLoading

    private val _profileError = MutableLiveData<String?>(null)
    val profileError: LiveData<String?> = _profileError

    private val _badges = MutableLiveData<BadgesResponse?>()
    val badges: LiveData<BadgesResponse?> = _badges

    private val _solved = MutableLiveData<SolvedResponse?>()
    val solved: LiveData<SolvedResponse?> = _solved

    private val _submissions = MutableLiveData<List<Submission>>(emptyList())
    val submissions: LiveData<List<Submission>> = _submissions

    private val submissionsCache = ConcurrentHashMap<String, List<Submission>>()

    private var lastUsername: String? = null

    private var profileCall: Call<UserProfileResponse>? = null
    private var badgesCall: Call<BadgesResponse>? = null
    private var solvedCall: Call<SolvedResponse>? = null
    private var submissionsCall: Call<SubmissionsResponse>? = null

    fun loadProfile(username: String) {
        if (username.isBlank()) return
        if (_profile.value != null && _profile.value?.username?.equals(username, true) == true) {
            _profileError.postValue(null)
            _profileLoading.postValue(false)
            lastUsername = username
            return
        }
        lastUsername = username
        profileCall?.cancel()
        _profileLoading.postValue(true)
        _profileError.postValue(null)
        profileCall = RetrofitClient.apiService.getUserProfile(username)
        profileCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                _profileLoading.postValue(false)
                if (response.isSuccessful) {
                    _profile.postValue(response.body())
                    _profileError.postValue(null)
                    Log.d("SharedDataVM", "profile loaded for $username: ${response.body()}")
                } else {
                    _profile.postValue(null)
                    _profileError.postValue("Profile load failed (${response.code()})")
                    Log.w("SharedDataVM", "profile load failed ${response.code()} for $username")
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                _profileLoading.postValue(false)
                if (!call.isCanceled) {
                    _profile.postValue(null)
                    _profileError.postValue(t.localizedMessage ?: "Network error")
                    Log.w("SharedDataVM", "profile load error for $username: ${t.localizedMessage}")
                }
            }
        })
    }

    fun loadBadges(username: String) {
        if (username.isBlank()) return
        badgesCall?.cancel()
        badgesCall = RetrofitClient.apiService.getBadges(username)
        badgesCall?.enqueue(object : Callback<BadgesResponse> {
            override fun onResponse(call: Call<BadgesResponse>, response: Response<BadgesResponse>) {
                if (response.isSuccessful) {
                    _badges.postValue(response.body())
                    Log.d("SharedDataVM", "badges loaded for $username")
                } else {
                    _badges.postValue(null)
                    Log.w("SharedDataVM", "badges load failed ${response.code()} for $username")
                }
            }
            override fun onFailure(call: Call<BadgesResponse>, t: Throwable) {
                if (!call.isCanceled) {
                    _badges.postValue(null)
                    Log.w("SharedDataVM", "badges load error for $username: ${t.localizedMessage}")
                }
            }
        })
    }

    fun loadSolved(username: String) {
        if (username.isBlank()) return
        lastUsername = username
        solvedCall?.cancel()
        solvedCall = RetrofitClient.apiService.getSolved(username)
        solvedCall?.enqueue(object : Callback<SolvedResponse> {
            override fun onResponse(call: Call<SolvedResponse>, response: Response<SolvedResponse>) {
                if (response.isSuccessful) {
                    _solved.postValue(response.body())
                    Log.d("SharedDataVM", "solved loaded for $username: ${response.body()}")
                } else {
                    _solved.postValue(null)
                    Log.w("SharedDataVM", "solved load failed ${response.code()} for $username")
                }
            }
            override fun onFailure(call: Call<SolvedResponse>, t: Throwable) {
                if (!call.isCanceled) {
                    _solved.postValue(null)
                    Log.w("SharedDataVM", "solved load error for $username: ${t.localizedMessage}")
                }
            }
        })
    }

    fun getLastUsername(): String? = lastUsername

    fun loadSubmissionsWrapped(username: String, limit: Int) {
        if (username.isBlank()) return
        val key = "$username|$limit"
        val cached = submissionsCache[key]
        if (cached != null) {
            _submissions.postValue(cached ?: emptyList())
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
                    Log.d("SharedDataVM", "submissions loaded for $username (limit=$limit) count=${list.size}")
                } else {
                    _submissions.postValue(emptyList())
                    Log.w("SharedDataVM", "submissions load failed ${response.code()} for $username")
                }
            }
            override fun onFailure(call: Call<SubmissionsResponse>, t: Throwable) {
                if (!call.isCanceled) {
                    _submissions.postValue(emptyList())
                    Log.w("SharedDataVM", "submissions load error for $username: ${t.localizedMessage}")
                }
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
