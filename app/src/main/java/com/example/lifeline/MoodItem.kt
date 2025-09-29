package com.example.lifeline

data class MoodItem (
    val id: Long = System.currentTimeMillis(), //unique
    val emojiResId: Int,
    val name: String,
    val description: String,
    val timeStamp: Long = System.currentTimeMillis()
)