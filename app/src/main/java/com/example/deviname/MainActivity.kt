package com.example.deviname.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deviname.data.GameRepository
import com.example.deviname.model.Category
import com.example.deviname.viewmodel.GameViewModel
import com.example.deviname.viewmodel.MAX_ERREURS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---- Palette cinéma : jaune et noir ----
private val CinemaYellow = Color(0xFFFFD60A)
private val CinemaBlack = Color(0xFF0A0A0A)
private val CinemaGrey = Color(0xFF1C1C1C)
private val CinemaWhite = Color(0xFFF5F5F5)
private val CinemaRed = Color(0xFFFF5252)

private val CinemaColorScheme = darkColorScheme(
    primary = CinemaYellow,
    onPrimary = Color.Black,
    secondary = CinemaYellow,
    onSecondary = Color.Black,
    background = CinemaBlack,
    onBackground = CinemaWhite,
    surface = CinemaBlack,
    onSurface = CinemaWhite,
    surfaceVariant = CinemaGrey,
    onSurfaceVariant = CinemaWhite,
    error = CinemaRed,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = CinemaColorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DevinameApp()
                }
            }
        }
    }
}

@Composable
fun DevinameApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var gameStarted by rememberSaveable { mutableStateOf(false) }

    if (!gameStarted || uiState.isGameOver || uiState.current == null) {
        HomeScreen(
            wasPlaying = uiState.isGameOver,
            lastScore = uiState.score,
            lastTotal = uiState.total,
            onStart = { category ->
                viewModel.startGame(category)
                gameStarted = true
            },
        )
    } else {
        GameScreen(
            viewModel = viewModel,
            onMenu = { gameStarted = false },
        )
    }
}

// ------------------------------------------------------------------
// Écran d'accueil
// ------------------------------------------------------------------

@Composable
fun HomeScreen(
    wasPlaying: Boolean,
    lastScore: Int,
    lastTotal: Int,
    onStart: (Category?) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "🎬 DEVINE !",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Reconnais-les en une seule image !",
            style = MaterialTheme.typography.titleMedium,
        )

        if (wasPlaying) {
            Spacer(Modifier.height(12.dp))
            Text(
                if (lastTotal == 0) {
                    "Aucune image dans cette catégorie pour le moment."
                } else {
                    "🏁 Partie terminée ! Score : $lastScore / $lastTotal"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(20.dp))
        IntroCard()

        Spacer(Modifier.height(24.dp))
        Text(
            "Catégorie",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryCard(
                label = "Tout",
                emoji = "🍿",
                selected = selectedCategory == null,
                modifier = Modifier.weight(1f),
            ) { selectedCategory = null }
            CategoryCard(
                label = Category.FILM.label,
                emoji = "🎬",
                selected = selectedCategory == Category.FILM,
                modifier = Modifier.weight(1f),
            ) { selectedCategory = Category.FILM }
            CategoryCard(
                label = Category.SERIE.label,
                emoji = "📺",
                selected = selectedCategory == Category.SERIE,
                modifier = Modifier.weight(1f),
            ) { selectedCategory = Category.SERIE }
            CategoryCard(
                label = Category.JEUX_VIDEO.label,
                emoji = "🎮",
                selected = selectedCategory == Category.JEUX_VIDEO,
                modifier = Modifier.weight(1f),
            ) { selectedCategory = Category.JEUX_VIDEO }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onStart(selectedCategory) },
            colors = ButtonDefaults.buttonColors(
                containerColor = CinemaYellow,
                contentColor = Color.Black,
            ),
        ) {
            Text("ACTION !", fontWeight = FontWeight.Bold)
        }
    }
}

/** Encadré qui explique le jeu */
@Composable
fun IntroCard() {
    val erreurs = if (MAX_ERREURS > 1) "erreurs" else "erreur"
    val coeurs = "❤️".repeat(MAX_ERREURS)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(CinemaGrey),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(CinemaYellow),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Des films, des séries et des jeux vidéo ont été réduits à une image minimaliste. À toi de retrouver leur titre !")
            Spacer(Modifier.height(8.dp))
            Text("Choisis une catégorie, puis tape ta réponse : les accents, les majuscules et la ponctuation n'ont pas d'importance.")
            Spacer(Modifier.height(8.dp))
            Text("Pour chaque image, tu as droit à $MAX_ERREURS $erreurs ($coeurs) : ensuite, la réponse t'est dévoilée. Bonne chance !")
        }
    }
}

/** Carte de catégorie affichant un emoji */
@Composable
fun CategoryCard(
    label: String,
    emoji: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(CinemaGrey)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) CinemaYellow else CinemaWhite.copy(alpha = 0.3f),
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 32.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 12.sp,
            maxLines = 1,
            color = if (selected) CinemaYellow else CinemaWhite,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ------------------------------------------------------------------
// Écran de jeu
// ------------------------------------------------------------------

@Composable
fun GameScreen(viewModel: GameViewModel, onMenu: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val item = uiState.current ?: return

    // Essais restants pour l'image en cours
    val left = (MAX_ERREURS - uiState.errors).coerceAtLeast(0)

    // Chargement de l'image
    var image by remember(item.fileName) { mutableStateOf<ImageBitmap?>(null) }
    var imageFailed by remember(item.fileName) { mutableStateOf(false) }
    LaunchedEffect(item.fileName) {
        val bmp = withContext(Dispatchers.IO) { GameRepository.loadImage(context, item.fileName) }
        if (bmp != null) image = bmp else imageFailed = true
    }

    // Champ de réponse : après une erreur, la dernière réponse reste affichée et sélectionnée
    var field by remember(item.fileName, uiState.errors) {
        mutableStateOf(TextFieldValue(uiState.lastTry, TextRange(0, uiState.lastTry.length)))
    }

    // Garde le clavier ouvert d'une image à l'autre
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(item.fileName, uiState.errors) {
        runCatching { focusRequester.requestFocus() }
    }

    val submit: () -> Unit = {
        if (field.text.isNotBlank()) viewModel.submitAnswer(field.text)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Barre du haut : menu, score, essais restants
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onMenu) { Text("← Menu", color = CinemaWhite) }
            Text(
                "Score : ${uiState.score} / ${uiState.total}",
                color = CinemaYellow,
                fontWeight = FontWeight.Bold,
            )
            Text("❤️".repeat(left) + "🖤".repeat(MAX_ERREURS - left))
        }

        Spacer(Modifier.height(12.dp))

        // Image à deviner, sur un cadre blanc
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(3.dp, CinemaYellow, RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            val bmp = image
            when {
                bmp != null -> Image(
                    bitmap = bmp,
                    contentDescription = "Image à deviner",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                imageFailed -> Text("Image introuvable : ${item.fileName}", color = Color.DarkGray)
                else -> CircularProgressIndicator(color = CinemaYellow)
            }
        }

        // Retour sur la réponse
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.answered && uiState.correct -> Text(
                    "✅ Bonne réponse !",
                    color = CinemaYellow,
                    fontWeight = FontWeight.Bold,
                )
                uiState.answered -> Text(
                    "❌ Raté ! C'était : ${item.title}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                uiState.errors > 0 -> Text(
                    "❌ Raté ! Il te reste $left " + if (left > 1) "essais." else "essai.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        OutlinedTextField(
            value = field,
            onValueChange = { if (!uiState.answered) field = it },
            label = { Text("Ta réponse") },
            singleLine = true,
            readOnly = uiState.answered,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (uiState.answered) viewModel.nextQuestion() else submit() },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CinemaYellow,
                unfocusedBorderColor = CinemaWhite.copy(alpha = 0.4f),
                focusedLabelColor = CinemaYellow,
                cursorColor = CinemaYellow,
            ),
        )

        Spacer(Modifier.height(12.dp))

        if (uiState.answered) {
            Button(
                onClick = { viewModel.nextQuestion() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaYellow,
                    contentColor = Color.Black,
                ),
            ) {
                Text(
                    if (uiState.remaining.isEmpty()) "Voir le score" else "Suivant",
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Button(
                onClick = { submit() },
                enabled = field.text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaYellow,
                    contentColor = Color.Black,
                ),
            ) {
                Text("Valider", fontWeight = FontWeight.Bold)
            }
        }
    }
}