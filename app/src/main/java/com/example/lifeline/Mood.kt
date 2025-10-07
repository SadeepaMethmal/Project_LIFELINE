package com.example.lifeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifeline.databinding.FragmentMoodBinding

class Mood : Fragment() {

    // -------------------------------------------------------------------------
    // VARIABLES
    // -------------------------------------------------------------------------

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!! // Safe access to binding

    private var selectedEmojiResId: Int? = null // Currently selected mood icon

    // List of mood drawable resources
    private val moodList = listOf(
        R.drawable.happy,
        R.drawable.sad,
        R.drawable.love,
        R.drawable.angry,

    )

    // -------------------------------------------------------------------------
    // LIFECYCLE METHODS
    // -------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using ViewBinding
        _binding = FragmentMoodBinding.inflate(inflater, container, false)

        // Navigate to Mood History when "History" button is clicked
        binding.historyBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MoodHistory())
                .addToBackStack(null)
                .commit()
        }

        // Handle "Save Mood" button click
        binding.button3.setOnClickListener { handleSaveMood() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---------------------------------------------------------------------
        // MOOD RECYCLER VIEW SETUP
        // ---------------------------------------------------------------------

        val adapter = MoodRecyclerAdapter(moodList) { moodResId ->
            // When a mood is clicked, show it in the preview and store the selection
            selectedEmojiResId = moodResId
            binding.imageView17.setImageResource(moodResId)
        }

        // Set up horizontal scrolling list of moods
        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.moodRecyclerView.layoutManager = layoutManager
        binding.moodRecyclerView.adapter = adapter

        // Start at the middle to create the illusion of infinite scrolling
        binding.moodRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------

    /*
     * Handles saving a selected mood with an optional description.
     */
    private fun handleSaveMood() {
        val emoji = selectedEmojiResId ?: return // Exit if no mood is selected
        val desc = binding.moodDesc.text?.toString()?.trim().orEmpty()

        // Determine mood name from emoji resource
        val moodName = when (emoji) {
            R.drawable.happy -> "Happy"
            R.drawable.sad -> "Sad"
            R.drawable.love -> "Love"
            R.drawable.angry -> "Angry"
            else -> "Mood"
        }

        // Create mood entry
        val entry = MoodItem(
            emojiResId = emoji,
            name = moodName,
            description = desc
        )

        // Save to local storage
        MoodStorage.add(requireContext(), entry)

        // Reset input and confirm save
        binding.moodDesc.setText("")
        Toast.makeText(requireContext(), "Mood saved", Toast.LENGTH_SHORT).show()
    }
}
