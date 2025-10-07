import android.content.Context
import com.example.lifeline.MoodItem
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object MoodStorage {

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    private const val PREFS = "mood_prefs"       // SharedPreferences file name
    private const val KEY = "mood_history"       // Key for stored JSON data

    // -------------------------------------------------------------------------
    // LOAD ALL MOODS
    // -------------------------------------------------------------------------

    /*
     * Loads all saved mood entries from SharedPreferences.
     */
    fun load(context: Context): MutableList<MoodItem> {
        // Read JSON string from SharedPreferences; default to "[]" if empty
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"

        val arr = JSONArray(json)
        val list = mutableListOf<MoodItem>()

        // Convert each JSON object back into a MoodItem
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

    // -------------------------------------------------------------------------
    // SAVE ALL MOODS
    // -------------------------------------------------------------------------

    /*
     * Converts a list of MoodItem objects into a JSON array and saves it
     * to SharedPreferences.
     */
    private fun saveAll(context: Context, list: List<MoodItem>) {
        val arr = JSONArray()

        // Convert each MoodItem to a JSONObject and add to array
        list.forEach { e ->
            val o = JSONObject().apply {
                put("id", e.id)
                put("emojiResId", e.emojiResId)
                put("name", e.name)
                put("description", e.description)
                put("timeStamp", e.timeStamp) // same key name used in load()
            }
            arr.put(o)
        }

        // Save JSON string representation into SharedPreferences
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                putString(KEY, arr.toString())
            }
    }

    // -------------------------------------------------------------------------
    // ADD A NEW MOOD
    // -------------------------------------------------------------------------

    /*
     * Adds a new MoodItem to the top of the saved list (newest first)
     * and persists the updated list.
     */
    fun add(context: Context, entry: MoodItem) {
        // Load existing list
        val list = load(context)
        // Add new mood entry at the top
        list.add(0, entry)
        // Save updated list
        saveAll(context, list)
    }

    // -------------------------------------------------------------------------
    // CLEAR ALL MOODS
    // -------------------------------------------------------------------------

    /*
     * Clears all saved mood history from SharedPreferences.
     */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                remove(KEY) // delete the stored JSON data
            }
    }
}
