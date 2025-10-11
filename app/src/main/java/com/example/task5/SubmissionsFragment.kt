package com.example.task5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class SubmissionsFragment : Fragment() {

    companion object {
        const val ARG_USERNAME = "arg_username"
        const val ARG_LIMIT = "arg_limit"
        const val ARG_RECENT_JSON = "recent_submissions_json"

        fun newInstance(username: String, limit: Int): SubmissionsFragment {
            val f = SubmissionsFragment()
            val b = Bundle().apply {
                putString(ARG_USERNAME, username)
                putInt(ARG_LIMIT, limit)
            }
            f.arguments = b
            return f
        }
    }

    private var username: String = ""
    private var limit: Int = 5

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SubmissionsAdapter
    private lateinit var tvEmpty: TextView

    private val gson = Gson()
    private lateinit var viewModel: SharedDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString(ARG_USERNAME) ?: ""
        limit = arguments?.getInt(ARG_LIMIT) ?: 5
        if (limit <= 0) limit = 5
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_submissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rv_submissions)
        tvEmpty = view.findViewById(R.id.tv_empty)
        adapter = SubmissionsAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        tvEmpty.text = "Loading submissions..."
        tvEmpty.visibility = View.VISIBLE

        viewModel = ViewModelProvider(requireActivity()).get(SharedDataViewModel::class.java)

        val recentJson = arguments?.getString(ARG_RECENT_JSON)
        if (!recentJson.isNullOrBlank()) {
            try {
                val arr = gson.fromJson(recentJson, Array<RecentSubmission>::class.java)
                val list = arr.map {
                    Submission(
                        id = null,
                        title = it.title,
                        status = it.statusDisplay ?: it.status ?: "-",
                        lang = it.lang,
                        timestamp = it.timestamp?.toLongOrNull()
                    )
                }
                val limited = list.take(limit)
                if (limited.isNotEmpty()) {
                    showList(limited)
                    return
                } else {
                    showEmpty("No submissions found")
                    return
                }
            } catch (e: Exception) {}
        }

        viewModel.submissions.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) showEmpty("No submissions found") else showList(list)
        }

        if (username.isNotBlank()) {
            viewModel.loadSubmissionsWrapped(username, limit)
        } else {
            showEmpty("Invalid username")
        }
    }

    private fun showList(list: List<Submission>) {
        tvEmpty.visibility = View.GONE
        adapter.submitList(list)
    }

    private fun showEmpty(message: String) {
        adapter.submitList(emptyList())
        tvEmpty.text = message
        tvEmpty.visibility = View.VISIBLE
    }
}
