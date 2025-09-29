package com.example.lifeline

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pl.droidsonroids.gif.GifImageView

class MoodRecyclerAdapter(
        private val moods: List<Int>,
        private val onMoodClick: (Int) -> Unit //callback when clicked
    ) : RecyclerView.Adapter<MoodRecyclerAdapter.MoodViewHolder>() {

    // ViewHolder for GifImageView
    inner class MoodViewHolder(val gifView: GifImageView) : RecyclerView.ViewHolder(gifView){
        fun bind(mood: Int){
            gifView.setImageResource(mood)  //set gif
            gifView.setOnClickListener {
                onMoodClick(mood) //call back to fragment
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        // Inflate item_mood.xml
        val gifView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false) as GifImageView
        return MoodViewHolder(gifView)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position % moods.size] // pick GIF cyclically
        holder.bind(mood) // set GIF resource
    }

    override fun getItemCount(): Int = Int.MAX_VALUE // infinite scroll
}
