package com.example.lifeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.droidsonroids.gif.GifImageView

class MoodRecyclerAdapter(
    private val moods: List<Int>,
    private val onMoodClick: (Int) -> Unit // Callback for mood click events
) : RecyclerView.Adapter<MoodRecyclerAdapter.MoodViewHolder>() {

    // -------------------------------------------------------------------------
    // VIEW HOLDER CLASS
    // -------------------------------------------------------------------------

    /*
     * ViewHolder representing a single mood GIF item.
     */
    inner class MoodViewHolder(private val gifView: GifImageView) :
        RecyclerView.ViewHolder(gifView) {

        /*
         * Binds a mood resource to the GIF view and sets up the click listener.
         */
        fun bind(mood: Int) {
            gifView.setImageResource(mood) // Display the mood GIF
            gifView.setOnClickListener {
                onMoodClick(mood) // Notify the fragment when a mood is selected
            }
        }
    }

    // -------------------------------------------------------------------------
    // ADAPTER METHODS
    // -------------------------------------------------------------------------

    /*
     * Inflates the layout for a single mood item (item_mood.xml)
     * and creates a new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val gifView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false) as GifImageView
        return MoodViewHolder(gifView)
    }

    /*
     * Binds a mood GIF to the current position in the list.
     * The position value is wrapped using modulus (%) to loop through the moods list,
     * creating an infinite scrolling effect.
     */
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position % moods.size] // Repeat moods cyclically
        holder.bind(mood)
    }

    /*
     * Returns a very large number to simulate infinite scrolling.
     */
    override fun getItemCount(): Int = Int.MAX_VALUE
}
