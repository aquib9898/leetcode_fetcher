package com.example.task5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultsFragment : Fragment() {

    companion object {
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

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var username: String = ""

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

        fetchAndShow(RetrofitClient.apiService.getProfile(username)) { je ->
            tvProfile.text = "Profile:\n${gson.toJson(je)}"
        }
        fetchAndShow(RetrofitClient.apiService.getBadges(username)) { je ->
            tvBadges.text = "Badges:\n${gson.toJson(je)}"
        }
        fetchAndShow(RetrofitClient.apiService.getSolved(username)) { je ->
            tvSolved.text = "Solved:\n${gson.toJson(je)}"
        }

        btnViewSubs.setOnClickListener {
            val raw = etLimit.text.toString().trim()
            val limit = if (raw.isEmpty()) {
                DEFAULT_LIMIT
            } else {
                try {
                    val parsed = raw.toInt()
                    if (parsed <= 0) {
                        Toast.makeText(requireContext(), "Enter a positive number", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    parsed
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            (activity as? MainActivity)?.openSubmissions(username, limit)
        }
    }

    private fun fetchAndShow(call: Call<JsonElement>, onSuccess: (JsonElement) -> Unit) {
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onSuccess(gson.toJsonTree(mapOf("error" to "code:${response.code()}")))
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                onSuccess(gson.toJsonTree(mapOf("failure" to t.message)))
            }
        })
    }
}
