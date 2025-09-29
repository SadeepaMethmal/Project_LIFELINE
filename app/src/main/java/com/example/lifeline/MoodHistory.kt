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

    private var _binding: FragmentMoodHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MoodHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMoodHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //MoodStorage.clear(requireContext())  //to clear Mood Storage (testing purposes only)

        adapter = MoodHistoryAdapter(mutableListOf())
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = adapter

        binding.button4.setOnClickListener {
            MoodStorage.clear(requireContext())
            refreshList()
            Toast.makeText(requireContext(), "All history cleared", Toast.LENGTH_SHORT).show()
        }

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val data = MoodStorage.load(requireContext())
        adapter.submit(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
