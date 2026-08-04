package com.example.logicgermanchunks

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logicgermanchunks.data.model.ContextSentence
import com.example.logicgermanchunks.data.model.GermanChunk
import com.example.logicgermanchunks.ui.theme.LogicGermanChunksTheme
import org.json.JSONArray
import java.io.IOException
import java.util.Locale

// Navigation states
sealed class ScreenState {
    object Home : ScreenState()
    data class Session(val category: String, val filteredChunks: List<GermanChunk>, val initialIndex: Int = 0) : ScreenState()
}

// Local persistence helper using SharedPreferences
class SessionPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("logic_german_chunks_prefs", Context.MODE_PRIVATE)

    fun saveSession(category: String, chunkIds: List<String>, currentIndex: Int) {
        prefs.edit().apply {
            putString("session_category", category)
            putString("session_chunk_ids", chunkIds.joinToString(","))
            putInt("session_current_index", currentIndex)
            apply()
        }
    }

    fun getSessionCategory(): String? {
        return prefs.getString("session_category", null)
    }

    fun getSessionChunkIds(): List<String> {
        val idsString = prefs.getString("session_chunk_ids", null) ?: return emptyList()
        if (idsString.isEmpty()) return emptyList()
        return idsString.split(",")
    }

    fun getSessionCurrentIndex(): Int {
        return prefs.getInt("session_current_index", 0)
    }

    fun clearSession() {
        prefs.edit().apply {
            remove("session_category")
            remove("session_chunk_ids")
            remove("session_current_index")
            apply()
        }
    }

    fun incrementCompletionCount() {
        val count = getCompletionCount()
        prefs.edit().putInt("completion_count", count + 1).apply()
    }

    fun getCompletionCount(): Int {
        return prefs.getInt("completion_count", 0)
    }
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var sessionPrefs: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize utilities
        tts = TextToSpeech(this, this)
        sessionPrefs = SessionPreferences(this)

        setContent {
            LogicGermanChunksTheme {
                var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Home) }

                val context = this@MainActivity

                // Dynamically discover all category asset files at runtime
                val categories = remember {
                    discoverCategories(context)
                }

                // Global loading of all chunks for real-time searching on the home screen
                val allChunks = remember(categories) {
                    categories.flatMap { cat -> loadGermanChunksFromAssets(context, cat) }
                }

                // SharedPreferences live state triggers
                var totalCompletedCount by remember { mutableStateOf(sessionPrefs.getCompletionCount()) }
                var resumeCategory by remember { mutableStateOf(sessionPrefs.getSessionCategory()) }
                var resumeChunkIds by remember { mutableStateOf(sessionPrefs.getSessionChunkIds()) }
                var resumeIndex by remember { mutableStateOf(sessionPrefs.getSessionCurrentIndex()) }

                val resumeProgressText = if (resumeCategory != null && resumeChunkIds.isNotEmpty()) {
                    "Progress: ${resumeIndex + 1} of ${resumeChunkIds.size}"
                } else null

                when (val screen = currentScreen) {
                    is ScreenState.Home -> {
                        HomeScreen(
                            categories = categories,
                            totalCompletedSessions = totalCompletedCount,
                            resumeCategory = resumeCategory,
                            resumeProgressText = resumeProgressText,
                            allChunks = allChunks,
                            onResumeSession = {
                                val cat = resumeCategory
                                val ids = resumeChunkIds
                                val idx = resumeIndex
                                if (cat != null && ids.isNotEmpty()) {
                                    val loadedChunks = loadGermanChunksFromAssets(context, cat)
                                    val reconstituted = ids.mapNotNull { id -> loadedChunks.find { it.id == id } }
                                    if (reconstituted.isNotEmpty()) {
                                        currentScreen = ScreenState.Session(cat, reconstituted, idx)
                                    }
                                }
                            },
                            onClearResumeSession = {
                                sessionPrefs.clearSession()
                                resumeCategory = null
                                resumeChunkIds = emptyList()
                                resumeIndex = 0
                            },
                            onCategorySelect = { selectedCategory ->
                                val loadedChunks = loadGermanChunksFromAssets(context, selectedCategory)
                                val sessionList = loadedChunks.shuffled().take(5)
                                if (sessionList.isNotEmpty()) {
                                    sessionPrefs.saveSession(selectedCategory, sessionList.map { it.id }, 0)
                                    
                                    // Update local indicators
                                    resumeCategory = selectedCategory
                                    resumeChunkIds = sessionList.map { it.id }
                                    resumeIndex = 0
                                    
                                    currentScreen = ScreenState.Session(selectedCategory, sessionList, 0)
                                }
                            },
                            onChunkSelect = { chunk ->
                                // Custom 1-chunk search focus session
                                currentScreen = ScreenState.Session("Search Highlight", listOf(chunk), 0)
                            }
                        )
                    }
                    is ScreenState.Session -> {
                        ChunkScreen(
                            category = screen.category,
                            chunks = screen.filteredChunks,
                            initialIndex = screen.initialIndex,
                            onSpeak = { text -> speakGerman(text) },
                            onProgressUpdate = { index ->
                                sessionPrefs.saveSession(screen.category, screen.filteredChunks.map { it.id }, index)
                                resumeIndex = index
                            },
                            onSessionFinished = {
                                sessionPrefs.clearSession()
                                sessionPrefs.incrementCompletionCount()
                                
                                // Update local state
                                resumeCategory = null
                                resumeChunkIds = emptyList()
                                resumeIndex = 0
                                totalCompletedCount = sessionPrefs.getCompletionCount()

                                currentScreen = ScreenState.Home
                            },
                            onBackToHome = {
                                currentScreen = ScreenState.Home
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.GERMAN)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun speakGerman(text: String) {
        if (isTtsReady) {
            val cleanText = text.replace(Regex("""^\d+\.\s*"""), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun discoverCategories(context: Context): List<String> {
        return try {
            context.assets.list("")
                ?.filter { it.endsWith(".json", ignoreCase = true) && !it.equals("german_chunks.json", ignoreCase = true) }
                ?.map { fileName ->
                    val baseName = fileName.removeSuffix(".json")
                    baseName.lowercase(Locale.ROOT).replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() 
                    }
                }
                ?.sorted()
                ?: listOf("Home", "Work", "Personal")
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("Home", "Work", "Personal")
        }
    }

    private fun loadGermanChunksFromAssets(context: Context, categoryName: String): List<GermanChunk> {
        val chunks = mutableListOf<GermanChunk>()
        try {
            val fileName = "${categoryName.lowercase(Locale.ROOT)}.json"
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getString("id")
                val category = jsonObject.getString("category")
                val text = jsonObject.getString("germanChunk")
                val meaning = jsonObject.getString("translation")
                val literalTranslation = jsonObject.getString("literalBreakdown")
                val logicalExplanation = jsonObject.getString("logicalExplanation")

                val sentencesArray = jsonObject.getJSONArray("sentences")
                val contextSentences = mutableListOf<ContextSentence>()
                for (j in 0 until sentencesArray.length()) {
                    val sentenceObj = sentencesArray.getJSONObject(j)
                    contextSentences.add(
                        ContextSentence(
                            german = sentenceObj.getString("german"),
                            english = sentenceObj.getString("english")
                        )
                    )
                }

                chunks.add(
                    GermanChunk(
                        id = id,
                        text = text,
                        meaning = meaning,
                        literalTranslation = literalTranslation,
                        category = category,
                        logicalExplanation = logicalExplanation,
                        contextSentences = contextSentences
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chunks
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    categories: List<String>,
    totalCompletedSessions: Int,
    resumeCategory: String?,
    resumeProgressText: String?,
    allChunks: List<GermanChunk>,
    onResumeSession: () -> Unit,
    onClearResumeSession: () -> Unit,
    onCategorySelect: (String) -> Unit,
    onChunkSelect: (GermanChunk) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Logic German Chunks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dashboard & Search",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Dashboard Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Your Learning Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Master German vocabulary logically",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🏆 $totalCompletedSessions",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Real-time Global Search Input Field
            var searchQuery by remember { mutableStateOf("") }
            val filteredChunks = remember(searchQuery, allChunks) {
                if (searchQuery.isBlank()) emptyList()
                else {
                    allChunks.filter { chunk ->
                        chunk.text.contains(searchQuery, ignoreCase = true) ||
                        chunk.meaning.contains(searchQuery, ignoreCase = true) ||
                        chunk.category.contains(searchQuery, ignoreCase = true) ||
                        chunk.contextSentences.any {
                            it.german.contains(searchQuery, ignoreCase = true) ||
                            it.english.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Chunks...") },
                placeholder = { Text("Search German, English, or endings...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text("✕", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )

            // Conditional view: Standard dashboard or Filtered search results
            if (searchQuery.isEmpty()) {
                // Resume Session Banner
                if (resumeCategory != null && resumeProgressText != null) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Keep Going!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = resumeProgressText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = "You have an unfinished session in '$resumeCategory'.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onResumeSession,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Resume Study")
                                }
                                OutlinedButton(
                                    onClick = onClearResumeSession,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Discard Progress")
                                }
                            }
                        }
                    }
                }

                // Categories list header
                Text(
                    text = "Select Study Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Dynamic categories selector cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        OutlinedCard(
                            onClick = { onCategorySelect(category) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "➔",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Search Results header
                Text(
                    text = "Search Results (${filteredChunks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Search Results cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredChunks.isEmpty()) {
                        Text(
                            text = "No matching chunks found. Try another search!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        filteredChunks.forEach { chunk ->
                            OutlinedCard(
                                onClick = { onChunkSelect(chunk) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = chunk.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = chunk.meaning,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChunkScreen(
    modifier: Modifier = Modifier,
    category: String,
    chunks: List<GermanChunk>,
    initialIndex: Int,
    onSpeak: (String) -> Unit,
    onProgressUpdate: (Int) -> Unit,
    onSessionFinished: () -> Unit,
    onBackToHome: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }

    val isCompleted = currentIndex >= chunks.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Practice Session",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Text(
                            text = "◀",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isCompleted) {
                val currentChunk = chunks[currentIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Progress Indicator
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session Progress",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Chunk ${currentIndex + 1} of ${chunks.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = (currentIndex + 1).toFloat() / chunks.size
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }

                    // Center Card containing the study block
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentChunk,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                            },
                            label = "CardTransition"
                        ) { targetChunk ->
                            ChunkCard(
                                chunk = targetChunk,
                                onSpeak = onSpeak
                            )
                        }
                    }

                    // Navigation Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (currentIndex < chunks.size - 1) {
                                    val nextIdx = currentIndex + 1
                                    currentIndex = nextIdx
                                    onProgressUpdate(nextIdx)
                                } else {
                                    currentIndex = chunks.size
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            val buttonText = if (currentIndex == chunks.size - 1) "Finish Session" else "Next Chunk"
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            } else {
                CelebrationScreen(
                    onReset = onSessionFinished
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChunkCard(
    chunk: GermanChunk,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGrammarHelper by remember { mutableStateOf(false) }

    // Parse out optional case requirements in parentheses (e.g. "+ Dative", "+ Accusative")
    val text = chunk.text
    val regex = Regex("""\(([^)]+)\)""")
    val matchResult = regex.find(text)
    val requirement = matchResult?.groupValues?.get(1)
    val cleanText = if (requirement != null) text.substringBefore("(").trim() else text

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // German Chunk Text (without raw case text if badge is active)
            Text(
                text = cleanText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )

            // Clickable Grammar Requirement Badge
            if (requirement != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = { showGrammarHelper = !showGrammarHelper },
                        label = {
                            Text(
                                text = requirement,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Translation Meaning
            Text(
                text = chunk.meaning,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permanently visible study details block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = chunk.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Literal breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Literal: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "\"${chunk.literalTranslation}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Logical Explanation
                Text(
                    text = "Why it works logically:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = chunk.logicalExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Interactive Pronoun Selector Row
                Text(
                    text = "Pronoun / Subject Matrix:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var selectedPronounIndex by remember { mutableStateOf(0) }
                val pronouns = listOf("Ich", "Wir", "Er/Sie")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pronouns.forEachIndexed { index, pronoun ->
                        val isSelected = selectedPronounIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPronounIndex = index },
                            label = {
                                Text(
                                    text = pronoun,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Dynamic Pre-vetted Sentence Pair Content Container with TTS Button
                val sentencePair = chunk.contextSentences.getOrNull(selectedPronounIndex)
                if (sentencePair != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Sentence Context (${pronouns[selectedPronounIndex]}):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sentencePair.german,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onSpeak(sentencePair.german) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Text(
                                        text = "🔊",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Text(
                                text = sentencePair.english,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Grammar overlay helper dialog
    if (showGrammarHelper && requirement != null) {
        val explanation = when {
            requirement.contains("Dative", ignoreCase = true) -> {
                "The Dative case indicates the indirect object (recipient) or static location where an action happens.\n\n" +
                        "Definite Article changes:\n" +
                        "• Masculine: der ➔ dem\n" +
                        "• Feminine: die ➔ der\n" +
                        "• Neuter: das ➔ dem\n" +
                        "• Plural: die ➔ den (+ 'n' suffix to dative plural nouns)"
            }
            requirement.contains("Accusative", ignoreCase = true) -> {
                "The Accusative case indicates the direct object (thing directly acted upon) or direction/motion toward a place.\n\n" +
                        "Definite Article changes:\n" +
                        "• Masculine: der ➔ den\n" +
                        "• Feminine: die ➔ die (no change)\n" +
                        "• Neuter: das ➔ das (no change)\n" +
                        "• Plural: die ➔ die (no change)"
            }
            else -> {
                "This specifies the case or grammatical requirement expected by this specific preposition or verb structure. Correct case agreement is crucial for correct German sentences."
            }
        }

        AlertDialog(
            onDismissRequest = { showGrammarHelper = false },
            title = {
                Text(
                    text = "Grammar Guide: $requirement",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showGrammarHelper = false }) {
                    Text("Got it")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CelebrationScreen(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 56.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Session Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fantastic job! You've successfully finished this round of 5 logical German chunks.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Understanding the logical connections under the hood will help these chunks stick in your memory long term.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "🔄 Start New Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
