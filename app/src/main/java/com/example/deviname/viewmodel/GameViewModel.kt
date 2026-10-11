package com.example.deviname.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deviname.data.GameRepository
import com.example.deviname.model.Category
import com.example.deviname.model.GameItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

/**
 * Nombre d'erreurs permises PAR IMAGE : une fois atteint, la réponse est
 * dévoilée et on passe à l'image suivante (2 = deux essais par image).
 */
const val MAX_ERREURS = 2

data class GameUiState(
    val remaining: List<GameItem> = emptyList(), // images pas encore jouées
    val current: GameItem? = null,               // image affichée
    val total: Int = 0,                          // nombre d'images de la partie
    val score: Int = 0,                          // images trouvées
    val errors: Int = 0,                         // erreurs sur l'image en cours
    val lastTry: String = "",                    // dernière réponse proposée
    val answered: Boolean = false,               // image terminée (trouvée ou dévoilée)
    val correct: Boolean = false,                // trouvée ?
    val isGameOver: Boolean = false,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /** Démarre (ou relance) une partie. category = null : toutes les catégories. */
    fun startGame(category: Category?) {
        viewModelScope.launch {
            val all = withContext(Dispatchers.IO) {
                runCatching { GameRepository.loadItems(getApplication<Application>()) }
                    .getOrDefault(emptyList())
            }
            val pool = (if (category == null) all else all.filter { it.category == category })
                .shuffled()

            _uiState.value = GameUiState(
                remaining = pool.drop(1),
                current = pool.firstOrNull(),
                total = pool.size,
                isGameOver = pool.isEmpty(),
            )
        }
    }

    /** Le joueur valide une réponse (texte libre). */
    fun submitAnswer(input: String) {
        val s = _uiState.value
        val item = s.current ?: return
        if (s.answered || input.isBlank()) return

        if (normalize(input) == normalize(item.title)) {
            _uiState.value = s.copy(
                score = s.score + 1,
                lastTry = input,
                answered = true,
                correct = true,
            )
        } else {
            val errors = s.errors + 1
            _uiState.value = s.copy(
                errors = errors,
                lastTry = input,
                answered = errors >= MAX_ERREURS, // trop d'erreurs : on dévoile la réponse
                correct = false,
            )
        }
    }

    /** Image suivante, ou fin de partie s'il n'y en a plus. */
    fun nextQuestion() {
        val s = _uiState.value
        val next = s.remaining.firstOrNull()
        if (next == null) {
            _uiState.value = s.copy(isGameOver = true)
            return
        }
        _uiState.value = s.copy(
            remaining = s.remaining.drop(1),
            current = next,
            errors = 0, // les erreurs sont comptées image par image
            lastTry = "",
            answered = false,
            correct = false,
        )
    }

    /** Ignore accents, majuscules, ponctuation et espaces ("X-Men" = "x men" = "xmen"). */
    private fun normalize(text: String): String {
        val noAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")
        return noAccents.lowercase().replace(Regex("[^\\p{L}\\p{N}]"), "")
    }
}