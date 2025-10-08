package com.example.task5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ResultsFragment : Fragment() {

    companion object {
        private const val TAG = "ResultsFragment"
        private const val ARG_USERNAME = "arg_username"
        private const val DEFAULT_LIMIT = 5

        fun newInstance(username: String): ResultsFragment {
            val f = ResultsFragment()
            val b = Bundle()
            b.putString(ARG_USERNAME, username)
            f.arguments = b
            return f
        }
    }

    private lateinit var tvProfile: TextView
    private lateinit var tvBadges: TextView
    private lateinit var tvSolved: TextView
    private lateinit var btnViewSubs: Button
    private lateinit var etLimit: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var tvUsernameTitle: TextView

    private var username: String = ""
    private var profileCall: Call<UserProfileResponse>? = null
    private var badgesCall: Call<BadgesResponse>? = null
    private var solvedCall: Call<SolvedResponse>? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString(ARG_USERNAME) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvProfile = view.findViewById(R.id.tv_profile)
        tvBadges = view.findViewById(R.id.tv_badges)
        tvSolved = view.findViewById(R.id.tv_solved)
        btnViewSubs = view.findViewById(R.id.btn_view_submissions)
        etLimit = view.findViewById(R.id.et_submissions_limit)
        imgProfile = view.findViewById(R.id.img_profile)
        tvUsernameTitle = view.findViewById(R.id.tv_username_title)

        tvUsernameTitle.text = "User: $username"
        tvProfile.text = "Loading profile..."
        tvBadges.text = "Loading badges..."
        tvSolved.text = "Loading solved..."
        imgProfile.setImageResource(R.mipmap.ic_launcher_round)

        if (username.isBlank()) {
            Toast.makeText(requireContext(), "Invalid username", Toast.LENGTH_SHORT).show()
            return
        }

        fetchUserProfile()
        fetchSolved()
        fetchBadges()

        btnViewSubs.setOnClickListener {
            val raw = etLimit.text.toString().trim()
            val limit = raw.toIntOrNull() ?: DEFAULT_LIMIT
            val args = Bundle()
            args.putString("username", username)
            args.putInt("limit", limit)
            val frag = SubmissionsFragment.newInstance(username, limit)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchUserProfile() {
        profileCall = RetrofitClient.apiService.getUserProfile(username)
        profileCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (!isAdded) return
                if (!response.isSuccessful) {
                    tvProfile.text = "Profile: error ${response.code()}"
                    return
                }
                val body = response.body()
                tvProfile.text = buildString {
                    append("Profile:\n")
                    append(body?.username ?: body?.name ?: "-")
                    append("\nRank: ${body?.ranking ?: "-"}\n")
                    append(body?.contributionPoint?.let { "Contribution: $it\n" } ?: "")
                }
                val avatarCandidate = body?.avatar ?: body?.profileImage ?: body?.avatarUrl
                val avatarNormalized = normalizeUrl(avatarCandidate)
                if (!avatarNormalized.isNullOrBlank()) {
                    Thread {
                        val bmp = downloadBitmapBlocking(avatarNormalized)
                        if (bmp != null && isAdded) {
                            val circ = getCircularBitmap(bmp)
                            activity?.runOnUiThread {
                                try { imgProfile.setImageBitmap(circ) } catch (_: Exception) {}
                            }
                        } else {
                            Log.w(TAG, "avatar download returned null for url=$avatarNormalized")
                        }
                    }.start()
                } else {
                    Log.d(TAG, "no avatar found in profile response")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                if (!isAdded) return
                tvProfile.text = "Profile: network error"
                Log.w(TAG, "profile onFailure: ${t.localizedMessage}")
            }
        })
    }

    private fun fetchSolved() {
        solvedCall = RetrofitClient.apiService.getSolved(username)
        solvedCall?.enqueue(object : Callback<SolvedResponse> {
            override fun onResponse(call: Call<SolvedResponse>, response: Response<SolvedResponse>) {
                if (!isAdded) return
                if (!response.isSuccessful) {
                    tvSolved.text = "Solved: error ${response.code()}"
                    return
                }
                val body = response.body()
                val easy = body?.easySolved ?: body?.acSubmissionNum?.find { it.difficulty.equals("Easy", true) }?.count ?: 0
                val medium = body?.mediumSolved ?: body?.acSubmissionNum?.find { it.difficulty.equals("Medium", true) }?.count ?: 0
                val hard = body?.hardSolved ?: body?.acSubmissionNum?.find { it.difficulty.equals("Hard", true) }?.count ?: 0
                val total = body?.solvedProblem ?: body?.acSubmissionNum?.find { it.difficulty.equals("All", true) }?.count ?: (easy + medium + hard)
                tvSolved.text = "Solved:\nTotal: $total\nEasy: $easy  Medium: $medium  Hard: $hard"
            }

            override fun onFailure(call: Call<SolvedResponse>, t: Throwable) {
                if (!isAdded) return
                tvSolved.text = "Solved: network error"
                Log.w(TAG, "solved onFailure: ${t.localizedMessage}")
            }
        })
    }

    private fun fetchBadges() {
        badgesCall = RetrofitClient.apiService.getBadges(username)
        badgesCall?.enqueue(object : Callback<BadgesResponse> {
            override fun onResponse(call: Call<BadgesResponse>, response: Response<BadgesResponse>) {
                if (!isAdded) return
                if (!response.isSuccessful) {
                    tvBadges.text = "Badges:\n-"
                    return
                }
                val body = response.body()
                val items = body?.badges ?: emptyList()
                val names = if (items.isEmpty()) listOf("-") else items.mapNotNull { it.displayName }
                tvBadges.text = "Badges:\n" + names.joinToString(", ")
            }

            override fun onFailure(call: Call<BadgesResponse>, t: Throwable) {
                if (!isAdded) return
                tvBadges.text = "Badges:\n-"
                Log.w(TAG, "badges onFailure: ${t.localizedMessage}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileCall?.cancel()
        badgesCall?.cancel()
        solvedCall?.cancel()
    }

    private fun downloadBitmapBlocking(urlStr: String): Bitmap? {
        var conn: HttpURLConnection? = null
        var input: InputStream? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as? HttpURLConnection)?.apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
                doInput = true
            }
            conn?.connect()
            if (conn?.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "download failed code=${conn?.responseCode}")
                return null
            }
            input = conn.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.w(TAG, "downloadBitmapBlocking error: ${e.localizedMessage}")
            null
        } finally {
            try { input?.close() } catch (_: Exception) {}
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    private fun getCircularBitmap(srcBitmap: Bitmap): Bitmap {
        val size = minOf(srcBitmap.width, srcBitmap.height)
        val x = (srcBitmap.width - size) / 2
        val y = (srcBitmap.height - size) / 2
        val squared = Bitmap.createBitmap(srcBitmap, x, y, size, size)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        return output
    }

    private fun normalizeUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        return when {
            t.startsWith("http", true) -> t
            t.startsWith("//") -> "https:$t"
            t.startsWith("/") -> "https://leetcode.com$t"
            else -> "https://leetcode.com/$t"
        }
    }
}
