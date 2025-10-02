import android.content.Context
import com.example.lifeline.MoodItem
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object MoodStorage {
    private const val PREFS = "mood_prefs"
    private const val KEY = "mood_history"

    fun load(context: Context): MutableList<MoodItem> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"

        val arr = JSONArray(json)
        val list = mutableListOf<MoodItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                MoodItem(
                    id = o.getLong("id"),
                    emojiResId = o.getInt("emojiResId"),
                    name = o.getString("name"),
                    description = o.getString("description"),
                    timeStamp = o.getLong("timeStamp")
                )
            )
        }
        return list
    }

    private fun saveAll(context: Context, list: List<MoodItem>) {
        val arr = JSONArray()
        list.forEach { e ->
            val o = JSONObject().apply {
                put("id", e.id)
                put("emojiResId", e.emojiResId)
                put("name", e.name)
                put("description", e.description)
                put("timeStamp", e.timeStamp) // keep same key as load()
            }
            arr.put(o)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                putString(KEY, arr.toString())
            }
    }

    fun add(context: Context, entry: MoodItem) {
        val list = load(context)
        list.add(0, entry) // newest first
        saveAll(context, list)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                remove(KEY)
            }
    }
}
