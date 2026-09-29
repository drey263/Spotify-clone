package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SpotificApplication
import com.example.model.Track
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.Default.Favorite),
    DOWNLOADS("Downloads", Icons.Default.DownloadDone),
    ABOUT("About", Icons.Default.Info)
}

@Composable
fun MainScreen(
    app: SpotificApplication,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var isFullPlayerExpanded by remember { mutableStateOf(false) }

    // Persistent state collected from single source of truth (SpotificPlayerManager)
    val playbackState by app.playerManager.playbackState.collectAsState()

    // Room DB State
    val favorites by app.trackRepository.favorites.collectAsState(initial = emptyList())
    val downloads by app.trackRepository.downloads.collectAsState(initial = emptyList())
    val downloadingIds by app.downloadManager.downloadingTrackIds.collectAsState()

    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }
    val downloadedIds = remember(downloads) { downloads.map { it.id }.toSet() }

    val currentTrack = playbackState.currentTrack
    val isCurrentTrackFavorite = currentTrack?.let { favoriteIds.contains(it.id) } ?: false
    val isCurrentTrackDownloaded = currentTrack?.let { downloadedIds.contains(it.id) } ?: false
    val isCurrentTrackDownloading = currentTrack?.let { downloadingIds.contains(it.id) } ?: false

    // Back handling: If full player is open, close it; if not on home, go home
    BackHandler(enabled = isFullPlayerExpanded || currentTab != ScreenTab.HOME) {
        if (isFullPlayerExpanded) {
            isFullPlayerExpanded = false
        } else if (currentTab != ScreenTab.HOME) {
            currentTab = ScreenTab.HOME
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
            ) {
                // Docked MiniPlayer above bottom navigation
                if (currentTrack != null) {
                    MiniPlayer(
                        playbackState = playbackState,
                        isFavorite = isCurrentTrackFavorite,
                        onExpandClick = { isFullPlayerExpanded = true },
                        onPlayPauseClick = { app.playerManager.togglePlayPause(context) },
                        onFavoriteClick = {
                            coroutineScope.launch {
                                app.trackRepository.toggleFavorite(currentTrack)
                            }
                        }
                    )
                }

                // Flush bottom navigation bar
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar")
                ) {
                    ScreenTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    color = if (selected) ElectricBlue else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricBlue,
                                unselectedIconColor = TextGray,
                                selectedTextColor = ElectricBlue,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
        ) {
            // Main content depending on active tab
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(
                    repository = app.trackRepository,
                    onTrackSelect = { track, queue ->
                        app.playerManager.playTrack(context, track, queue)
                        isFullPlayerExpanded = true
                    },
                    onNavigateToSearch = { currentTab = ScreenTab.SEARCH }
                )
                ScreenTab.SEARCH -> SearchScreen(
                    repository = app.trackRepository,
                    currentTrackId = currentTrack?.id,
                    favoriteIds = favoriteIds,
                    downloadedIds = downloadedIds,
                    downloadingIds = downloadingIds,
                    onTrackSelect = { track, queue ->
                        app.playerManager.playTrack(context, track, queue)
                        isFullPlayerExpanded = true
                    },
                    onToggleFavorite = { track ->
                        coroutineScope.launch {
                            app.trackRepository.toggleFavorite(track)
                        }
                    },
                    onDownloadTrack = { track ->
                        coroutineScope.launch {
                            Toast.makeText(context, "Downloading ${track.title}...", Toast.LENGTH_SHORT).show()
                            val success = app.downloadManager.downloadTrack(track)
                            if (success) {
                                Toast.makeText(context, "Downloaded ${track.title}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                ScreenTab.LIBRARY -> LibraryScreen(
                    favorites = favorites,
                    currentTrackId = currentTrack?.id,
                    downloadedIds = downloadedIds,
                    downloadingIds = downloadingIds,
                    onTrackSelect = { track, queue ->
                        app.playerManager.playTrack(context, track, queue)
                        isFullPlayerExpanded = true
                    },
                    onToggleFavorite = { track ->
                        coroutineScope.launch {
                            app.trackRepository.toggleFavorite(track)
                        }
                    },
                    onDownloadTrack = { track ->
                        coroutineScope.launch {
                            Toast.makeText(context, "Downloading ${track.title}...", Toast.LENGTH_SHORT).show()
                            val success = app.downloadManager.downloadTrack(track)
                            if (success) {
                                Toast.makeText(context, "Downloaded ${track.title}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                ScreenTab.DOWNLOADS -> DownloadsScreen(
                    downloadedTracks = downloads,
                    currentTrackId = currentTrack?.id,
                    onTrackSelect = { track, queue ->
                        app.playerManager.playTrack(context, track, queue)
                        isFullPlayerExpanded = true
                    },
                    onDeleteDownload = { track ->
                        coroutineScope.launch {
                            app.downloadManager.deleteDownload(track.id)
                            Toast.makeText(context, "Removed ${track.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                ScreenTab.ABOUT -> AboutScreen()
            }

            // Full Player Screen (Overlaid animated sheet)
            AnimatedVisibility(
                visible = isFullPlayerExpanded && currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FullPlayerSheet(
                    playbackState = playbackState,
                    isFavorite = isCurrentTrackFavorite,
                    isDownloaded = isCurrentTrackDownloaded,
                    isDownloading = isCurrentTrackDownloading,
                    onCollapse = { isFullPlayerExpanded = false },
                    onPlayPauseClick = { app.playerManager.togglePlayPause(context) },
                    onPreviousClick = { app.playerManager.skipPrevious(context) },
                    onNextClick = { app.playerManager.skipNext(context) },
                    onSeekTo = { posMs -> app.playerManager.seekTo(posMs) },
                    onFavoriteClick = {
                        currentTrack?.let { track ->
                            coroutineScope.launch {
                                app.trackRepository.toggleFavorite(track)
                            }
                        }
                    },
                    onDownloadClick = {
                        currentTrack?.let { track ->
                            coroutineScope.launch {
                                Toast.makeText(context, "Downloading ${track.title}...", Toast.LENGTH_SHORT).show()
                                val success = app.downloadManager.downloadTrack(track)
                                if (success) {
                                    Toast.makeText(context, "Downloaded ${track.title}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            // Buffering Loading Overlay
            LoadingOverlay(isBuffering = playbackState.isBuffering)
        }
    }
}
