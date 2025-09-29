package com.example.lifeline

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodHistoryAdapter(
    private val items: MutableList<MoodItem>
) : RecyclerView.Adapter<MoodHistoryAdapter.VH>() {

    private val fmt = SimpleDateFormat("yyyy-MM-dd • HH:mm", Locale.getDefault())

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivEmoji: ImageView = itemView.findViewById(R.id.ivEmoji)
        val tvMoodName: TextView = itemView.findViewById(R.id.textView24)
        val tvDescription: TextView = itemView.findViewById(R.id.textView25)
        val tvTimestamp: TextView = itemView.findViewById(R.id.textView26)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.ivEmoji.setImageResource(item.emojiResId)
        holder.tvMoodName.text = item.name
        holder.tvDescription.text = item.description.ifBlank { "No description" }
        holder.tvTimestamp.text = fmt.format(Date(item.timeStamp))
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<MoodItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
