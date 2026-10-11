package com.example.deviname.data

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.deviname.model.Category
import com.example.deviname.model.GameItem
import org.json.JSONArray
import java.text.Normalizer

/**
 * Charge la liste des items depuis assets/data.json et les images depuis
 * assets/images/. Aucune dépendance externe : on utilise org.json (inclus
 * dans Android).
 */
object GameRepository {

    fun loadItems(context: Context): List<GameItem> {
        val jsonText = context.assets.open("data.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(jsonText)
        val items = mutableListOf<GameItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val file = obj.getString("file")
            items.add(
                GameItem(
                    fileName = file,
                    title = obj.getString("title"),
                    category = parseCategory(obj.optString("category"), file),
                )
            )
        }
        return items
    }

    /**
     * Tolérant : "Jeux vidéo", "jeux-video" ou "JEUX_VIDÉO" donnent tous JEUX_VIDEO.
     * Une catégorie inconnue est rangée dans Film, avec un avertissement dans Logcat.
     */
    private fun parseCategory(raw: String, file: String): Category {
        val key = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "") // retire les accents
            .lowercase()
            .trim()
            .replace(Regex("[\\s-]+"), "_")

        val found = Category.values().firstOrNull { it.name.lowercase() == key }
        if (found == null) {
            Log.w("Devine", "Catégorie inconnue \"$raw\" pour $file : rangée dans Film")
        }
        return found ?: Category.FILM
    }

    /** Charge une image depuis assets/images/<fileName> en ImageBitmap Compose */
    fun loadImage(context: Context, fileName: String): ImageBitmap? {
        return runCatching {
            context.assets.open("images/$fileName").use { stream ->
                BitmapFactory.decodeStream(stream).asImageBitmap()
            }
        }.getOrNull()
    }
}