package com.example.lifeline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isEmpty
import com.example.lifeline.databinding.FragmentHabitBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class Habit : Fragment() {

    // --------------------------------------------------
    // VARIABLES
    // --------------------------------------------------
    private lateinit var progressBar: CircularProgressBar
    private var _binding: FragmentHabitBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()
    private val prefs by lazy {
        requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    }
    private var habits: MutableList<HabitItem> = mutableListOf()

    // --------------------------------------------------
    // LIFECYCLE METHODS
    // --------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = binding.progressBar2
        loadHabits()

        // Create new habit
        binding.habitBtn.setOnClickListener { showCreateHabitDialog() }

        // Navigate back to home
        binding.habitBackBtn.setOnClickListener {
            (requireActivity() as Navbar).navigateToFragment(Home(), R.id.home)
        }

        // Reset all habits
        binding.resetBtn.setOnClickListener { showResetConfirmDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --------------------------------------------------
    // CREATE HABIT DIALOG
    // --------------------------------------------------
    private fun showCreateHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.create_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.textInputLayoutHabit)
        val etDesc = dialogView.findViewById<EditText>(R.id.textInputLayoutHabitDesc)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val habit = HabitItem(name, desc, false)
            habits.add(habit)
            saveHabits()
            addHabitCard(habit)
            dialog.dismiss()
        }

        dialog.show()
    }

    // --------------------------------------------------
    // EDIT HABIT DIALOG
    // --------------------------------------------------
    @SuppressLint("SetTextI18n")
    private fun showEditHabitDialog(
        habit: HabitItem,
        nameText: TextView,
        descText: TextView
    ) {
        // Inflate the new edit_habit layout
        val dialogView = layoutInflater.inflate(R.layout.edit_habit, null)

        // Get references to the input fields
        val etName = dialogView.findViewById<EditText>(R.id.textInputLayoutUpdateHabit)
        val etDesc = dialogView.findViewById<EditText>(R.id.textInputLayoutUpdateHabitDesc)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelEdit)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEdit)

        // Pre-fill the existing habit data
        etName.setText(habit.name)
        etDesc.setText(habit.description)

        // Initialize BottomSheetDialog
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        // Cancel button closes dialog
        btnCancel.setOnClickListener { dialog.dismiss() }

        // Save (Update) button logic
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newDesc = etDesc.text.toString().trim()

            if (newName.isEmpty() || newDesc.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the habit object & UI
            habit.name = newName
            habit.description = newDesc
            nameText.text = newName
            descText.text = newDesc

            // Persist changes
            saveHabits()

            Toast.makeText(requireContext(), "Habit updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // --------------------------------------------------
    // ADD HABIT CARD TO LAYOUT
    // --------------------------------------------------
    private fun addHabitCard(habit: HabitItem) {
        binding.emptyStateLayout.visibility = View.GONE

        val cardView = layoutInflater.inflate(R.layout.item_habit, binding.habitContainer, false)
        val nameText = cardView.findViewById<TextView>(R.id.cardHabitName)
        val descText = cardView.findViewById<TextView>(R.id.cardHabitDesc)
        val checkBox = cardView.findViewById<CheckBox>(R.id.cardCheck)
        val btnDelete = cardView.findViewById<ImageView>(R.id.delete)
        val btnEdit = cardView.findViewById<ImageView>(R.id.editHabit)

        nameText.text = habit.name
        descText.text = habit.description
        checkBox.isChecked = habit.isCompleted

        // Toggle completion
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            saveHabits()
            updateProgressBar()
        }

        // Delete habit
        btnDelete.setOnClickListener { showDeleteDialog(cardView, habit) }

        // Edit habit
        btnEdit.setOnClickListener { showEditHabitDialog(habit, nameText, descText) }

        binding.habitContainer.addView(cardView)
        updateProgressBar()
    }

    // --------------------------------------------------
    // DELETE HABIT CONFIRMATION
    // --------------------------------------------------
    private fun showDeleteDialog(cardView: View, habit: HabitItem) {
        val confirmView = layoutInflater.inflate(R.layout.custom_confirm_box, null)
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(confirmView)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnYes = confirmView.findViewById<Button>(R.id.btnYes)
        val btnNo = confirmView.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            binding.habitContainer.removeView(cardView)
            habits.remove(habit)
            saveHabits()
            updateProgressBar()

            if (binding.habitContainer.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
            }

            Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
            confirmDialog.dismiss()
        }

        btnNo.setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.show()
    }

    // --------------------------------------------------
    // RESET HABITS
    // --------------------------------------------------
    private fun showResetConfirmDialog() {
        val confirmView = layoutInflater.inflate(R.layout.custom_confirm_box2, null)
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(confirmView)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnYes = confirmView.findViewById<Button>(R.id.btnResetYes)
        val btnNo = confirmView.findViewById<Button>(R.id.btnResetNo)

        btnYes.setOnClickListener {
            resetHabits()
            confirmDialog.dismiss()
        }

        btnNo.setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.show()
    }

    private fun resetHabits() {
        habits.clear()
        prefs.edit { putString("task_completion", "0/0") }
        binding.habitContainer.removeAllViews()
        binding.emptyStateLayout.visibility = View.VISIBLE
        progressBar.setProgressWithAnimation(0f, 1000)
        saveHabits()
        Toast.makeText(requireContext(), "All habits have been reset", Toast.LENGTH_SHORT).show()
    }

    // --------------------------------------------------
    // PROGRESS BAR + STORAGE HELPERS
    // --------------------------------------------------
    private fun updateProgressBar() {
        if (habits.isEmpty()) {
            progressBar.setProgressWithAnimation(0f, 1000)
            prefs.edit { putString("task_completion", "0/0") }
            return
        }

        val completed = habits.count { it.isCompleted }
        val progressPercent = (completed.toFloat() / habits.size.toFloat()) * 100f
        progressBar.setProgressWithAnimation(progressPercent, 1000)

        prefs.edit { putString("task_completion", "$completed/${habits.size}") }
    }

    private fun saveHabits() {
        val json = gson.toJson(habits)
        prefs.edit { putString("habits_list", json) }
    }

    private fun loadHabits() {
        val json = prefs.getString("habits_list", null) ?: return
        val type = object : TypeToken<MutableList<HabitItem>>() {}.type
        habits = gson.fromJson(json, type)
        habits.forEach { addHabitCard(it) }
    }
}
