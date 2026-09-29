package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TrackRepository
import com.example.model.Track
import com.example.ui.components.TrackCardRow
import com.example.ui.theme.DarkBackgroundGradient
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GenreCategory(
    val name: String,
    val gradientColors: List<Color>
)

val GENRE_CATEGORIES = listOf(
    GenreCategory("Pop", listOf(Color(0xFF1E90FF), Color(0xFF00C6FF))),
    GenreCategory("Hip Hop", listOf(Color(0xFF8A2387), Color(0xFFE94057))),
    GenreCategory("Rock", listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
    GenreCategory("R&B", listOf(Color(0xFF654ea3), Color(0xFFeaafc8))),
    GenreCategory("EDM", listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
    GenreCategory("Indie", listOf(Color(0xFFF37335), Color(0xFFFDC830))),
    GenreCategory("Latin", listOf(Color(0xFFe52d27), Color(0xFFb31217))),
    GenreCategory("Chill", listOf(Color(0xFF4776E6), Color(0xFF8E54E9)))
)

@Composable
fun SearchScreen(
    repository: TrackRepository,
    currentTrackId: String?,
    favoriteIds: Set<String>,
    downloadedIds: Set<String>,
    downloadingIds: Set<String>,
    onTrackSelect: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            searchResults = emptyList()
            hasSearched = false
            isSearching = false
            return
        }

        coroutineScope.launch {
            isSearching = true
            hasSearched = true
            val result = repository.search(trimmed)
            searchResults = result.getOrDefault(emptyList())
            isSearching = false
        }
    }

    // Debounce search as user types
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(400)
            performSearch(searchQuery)
        } else {
            searchResults = emptyList()
            hasSearched = false
            isSearching = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackgroundGradient)
            .testTag("search_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Pill-shaped search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "What do you want to listen to?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = TextGray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextGray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    performSearch(searchQuery)
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("search_text_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Empty State Genre Grid, Loading, Results List, or No Results
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Searching...", color = TextGray)
                        }
                    }
                }

                searchQuery.isEmpty() -> {
                    // Empty state: Genre Category Grid
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Browse all",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(GENRE_CATEGORIES) { genre ->
                                Box(
                                    modifier = Modifier
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(genre.gradientColors))
                                        .clickable {
                                            searchQuery = genre.name
                                            performSearch(genre.name)
                                        }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                hasSearched && searchResults.isEmpty() -> {
                    // Empty results state: icon + "No results found"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results found for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching for another song, artist, or album",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }

                else -> {
                    // Vertical results list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        items(searchResults, key = { it.id }) { track ->
                            TrackCardRow(
                                track = track,
                                isPlaying = track.id == currentTrackId,
                                isFavorite = favoriteIds.contains(track.id),
                                isDownloaded = downloadedIds.contains(track.id),
                                isDownloading = downloadingIds.contains(track.id),
                                onTrackClick = { onTrackSelect(track, searchResults) },
                                onFavoriteClick = { onToggleFavorite(track) },
                                onDownloadClick = { onDownloadTrack(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}
