package com.example.lifeline

data class HabitItem(
    val name: String,
    val description: String,
    var isCompleted: Boolean = false
)
