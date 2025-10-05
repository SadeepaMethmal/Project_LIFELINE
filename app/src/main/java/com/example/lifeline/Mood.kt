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

    private var _binding: FragmentMoodBinding? = null  // view binding reference
    private val binding get() = _binding!!             // safe getter for binding
    private var selectedEmojiResId: Int? = null

    // list of mood emojis
    private val moodList = listOf(
        R.drawable.happy,
        R.drawable.sad,
        R.drawable.love,
        R.drawable.angry
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false) // inflate layout

        binding.historyBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MoodHistory())
                .addToBackStack(null)
                .commit()
        }

        binding.button3.setOnClickListener {
            val emoji = selectedEmojiResId ?: return@setOnClickListener
            val desc = binding.moodDesc.text?.toString()?.trim().orEmpty()
            val moodName = when (emoji) {
                R.drawable.happy -> "Happy"
                R.drawable.sad -> "Sad"
                R.drawable.love -> "Love"
                R.drawable.angry -> "Angry"
                else -> "Mood"
            }
            val entry = MoodItem(
                emojiResId = emoji,
                name = moodName,
                description = desc
            )

            MoodStorage.add(requireContext(), entry)
            binding.moodDesc.setText("")
            Toast.makeText(requireContext(), "Mood saved", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MoodRecyclerAdapter(moodList){ moodResId ->  // set adapter for RecyclerView
            selectedEmojiResId = moodResId
            binding.imageView17.setImageResource(moodResId) // When a mood is clicked, show it in imageView17
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false) // horizontal list

        binding.moodRecyclerView.layoutManager = layoutManager // attach layout manager
        binding.moodRecyclerView.adapter = adapter             // attach adapter

        // start at the middle for infinite scroll effect
        binding.moodRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // clear binding to avoid memory leaks
    }
}
