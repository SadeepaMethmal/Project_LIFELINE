package com.example.lifeline

data class Reminder(
    val hour: Int,
    val minute: Int,
    val days: String = "Reminder Set At"
)
