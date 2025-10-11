package com.example.task5

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide

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
    private lateinit var viewModel: SharedDataViewModel

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

        viewModel = ViewModelProvider(requireActivity()).get(SharedDataViewModel::class.java)
        tvUsernameTitle.text = "User: $username"

        viewModel.profile.observe(viewLifecycleOwner) { profileResp ->
            if (profileResp == null) {
                tvProfile.text = "Profile: error or not available"
                imgProfile.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                tvProfile.text = buildString {
                    append("Profile:\n")
                    append(profileResp.username ?: profileResp.name ?: "-")
                    append("\nRank: ${profileResp.ranking ?: "-"}\n")
                    append(profileResp.contributionPoint?.let { "Contribution: $it\n" } ?: "")
                }

                val avatarCandidate = profileResp.avatar ?: profileResp.profileImage ?: profileResp.avatarUrl
                val avatarNormalized = normalizeUrl(avatarCandidate)
                if (!avatarNormalized.isNullOrBlank()) {
                    try {
                        Glide.with(this)
                            .load(avatarNormalized)
                            .circleCrop()
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(imgProfile)
                    } catch (e: Exception) {
                        Log.w(TAG, "Glide load failed: ${e.localizedMessage}")
                        imgProfile.setImageResource(R.mipmap.ic_launcher_round)
                    }
                } else {
                    imgProfile.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
        }

        viewModel.badges.observe(viewLifecycleOwner) { badgesResp ->
            if (badgesResp == null) {
                tvBadges.text = "Badges:\n-"
            } else {
                val items = badgesResp.badges ?: emptyList()
                val names = if (items.isEmpty()) listOf("-") else items.mapNotNull { it.displayName }
                tvBadges.text = "Badges:\n" + names.joinToString(", ")
            }
        }

        viewModel.solved.observe(viewLifecycleOwner) { solvedResp ->
            if (solvedResp == null) {
                tvSolved.text = "Solved: -"
            } else {
                val easy = solvedResp.easySolved ?: 0
                val medium = solvedResp.mediumSolved ?: 0
                val hard = solvedResp.hardSolved ?: 0
                val total = solvedResp.totalSolved ?: (easy + medium + hard)
                tvSolved.text = "Solved:\nTotal: $total\nEasy: $easy  Medium: $medium  Hard: $hard"
            }
        }

        if (username.isBlank()) {
            Toast.makeText(requireContext(), "Invalid username", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.loadProfile(username)
        viewModel.loadSolved(username)
        viewModel.loadBadges(username)

        btnViewSubs.setOnClickListener {
            val raw = etLimit.text.toString().trim()
            val limit = raw.toIntOrNull() ?: DEFAULT_LIMIT
            val frag = SubmissionsFragment.newInstance(username, limit)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }
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


