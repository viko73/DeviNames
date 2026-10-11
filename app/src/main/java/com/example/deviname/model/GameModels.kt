package com.example.deviname.model

/** Catégorie d'un item à deviner */
enum class Category(val label: String) {
    FILM("Film"),
    SERIE("Série"),
    JEUX_VIDEO("Jeux vidéo"),
}

/**
 * Un item du jeu.
 * fileName = nom du fichier dans assets/images/ (ex: "inception.png")
 * title    = réponse attendue (ex: "Inception")
 */
data class GameItem(
    val fileName: String,
    val title: String,
    val category: Category,
)