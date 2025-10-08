package com.example.lifeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifeline.databinding.FragmentMoodHistoryBinding

class MoodHistory : Fragment() {

    // -------------------------------------------------------------------------
    // VARIABLES
    // -------------------------------------------------------------------------
    private var _binding: FragmentMoodHistoryBinding? = null
    private val binding get() = _binding!! // Safe access to view binding

    private lateinit var adapter: MoodHistoryAdapter // RecyclerView adapter

    // -------------------------------------------------------------------------
    // LIFECYCLE METHODS
    // -------------------------------------------------------------------------

    /*
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Initializes UI components and sets up event handlers.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = MoodHistoryAdapter(mutableListOf())
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = adapter

        // Navigate back to Mood fragment
        binding.moodBackBtn.setOnClickListener {
            (requireActivity() as Navbar).navigateToFragment(Mood(), R.id.home)
        }

        // Clear all mood history
        binding.button4.setOnClickListener {
            MoodStorage.clear(requireContext())
            refreshList()
            Toast.makeText(requireContext(), "All history cleared", Toast.LENGTH_SHORT).show()
        }

        // Initial data load
        refreshList()
    }

    /*
     * Refresh the list every time the fragment is resumed (ensures latest data).
     */
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    /*
     * Clears binding reference when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------------------------------------------------------------
    // HELPER FUNCTIONS
    // -------------------------------------------------------------------------

    /*
     * Loads saved moods from MoodStorage and updates the RecyclerView adapter.
     */
    private fun refreshList() {
        val data = MoodStorage.load(requireContext())
        adapter.submit(data)
    }
}
