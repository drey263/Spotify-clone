import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'models/playback_state.dart';
import 'models/track.dart';
import 'screens/about_screen.dart';
import 'screens/downloads_screen.dart';
import 'screens/full_player_screen.dart';
import 'screens/home_screen.dart';
import 'screens/library_screen.dart';
import 'screens/search_screen.dart';
import 'screens/splash_screen.dart';
import 'services/audio_platform_channel.dart';
import 'services/storage_service.dart';
import 'theme/spotific_theme.dart';
import 'widgets/loading_overlay.dart';
import 'widgets/mini_player.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      systemNavigationBarColor: SpotificTheme.surfaceDark,
      statusBarIconBrightness: Brightness.light,
      systemNavigationBarIconBrightness: Brightness.light,
    ),
  );

  final audioChannel = AudioPlatformChannel();
  // Critical Requirement: Re-sync with Kotlin foreground service BEFORE building UI
  await audioChannel.init();

  runApp(SpotificApp(audioChannel: audioChannel));
}

class SpotificApp extends StatelessWidget {
  final AudioPlatformChannel audioChannel;

  const SpotificApp({Key? key, required this.audioChannel}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Spotific',
      debugShowCheckedModeBanner: false,
      theme: SpotificTheme.themeData,
      home: SpotificRoot(audioChannel: audioChannel),
    );
  }
}

class SpotificRoot extends StatefulWidget {
  final AudioPlatformChannel audioChannel;

  const SpotificRoot({Key? key, required this.audioChannel}) : super(key: key);

  @override
  State<SpotificRoot> createState() => _SpotificRootState();
}

class _SpotificRootState extends State<SpotificRoot> {
  final StorageService _storageService = StorageService();

  bool _showSplash = true;
  int _currentTabIndex = 0;
  bool _isFullPlayerExpanded = false;

  PlaybackStateModel _playbackState = PlaybackStateModel();
  List<Track> _favorites = [];
  List<Track> _downloads = [];
  final Set<String> _downloadingIds = {};

  @override
  void initState() {
    super.initState();
    _playbackState = widget.audioChannel.currentState;
    // If audio was already active in background on startup, skip splash!
    if (_playbackState.currentTrack != null) {
      _showSplash = false;
    }

    // Subscribe to state stream
    widget.audioChannel.stateStream.listen((state) {
      if (mounted) {
        setState(() {
          _playbackState = state;
        });
      }
    });

    _loadLocalData();
  }

  Future<void> _loadLocalData() async {
    final favs = await _storageService.getFavorites();
    final dls = await _storageService.getDownloads();
    if (mounted) {
      setState(() {
        _favorites = favs;
        _downloads = dls;
      });
    }
  }

  void _playTrack(Track track, List<Track> queue) {
    widget.audioChannel.playTrack(track);
    setState(() {
      _isFullPlayerExpanded = true;
    });
  }

  Future<void> _toggleFavorite(Track track) async {
    await _storageService.toggleFavorite(track);
    await _loadLocalData();
  }

  Future<void> _downloadTrack(Track track) async {
    setState(() {
      _downloadingIds.add(track.id);
    });
    final success = await _storageService.downloadTrack(track);
    await _loadLocalData();
    setState(() {
      _downloadingIds.remove(track.id);
    });
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(success ? 'Downloaded ${track.title}' : 'Download failed'),
          backgroundColor: SpotificTheme.surfaceElevated,
          duration: const Duration(seconds: 2),
        ),
      );
    }
  }

  Future<void> _deleteDownload(Track track) async {
    await _storageService.deleteDownload(track.id);
    await _loadLocalData();
  }

  @override
  Widget build(BuildContext context) {
    if (_showSplash) {
      return SplashScreenWidget(
        onFinish: () {
          setState(() => _showSplash = false);
        },
      );
    }

    final favoriteIds = _favorites.map((t) => t.id).toSet();
    final downloadedIds = _downloads.map((t) => t.id).toSet();
    final currentTrack = _playbackState.currentTrack;
    final isCurrentTrackFavorite = currentTrack != null && favoriteIds.contains(currentTrack.id);
    final isCurrentTrackDownloaded = currentTrack != null && downloadedIds.contains(currentTrack.id);
    final isCurrentTrackDownloading = currentTrack != null && _downloadingIds.contains(currentTrack.id);

    return Scaffold(
      backgroundColor: SpotificTheme.backgroundDark,
      body: Stack(
        children: [
          // Current Tab Page
          IndexedStack(
            index: _currentTabIndex,
            children: [
              HomeScreenWidget(
                onTrackSelect: _playTrack,
                onNavigateToSearch: () => setState(() => _currentTabIndex = 1),
              ),
              SearchScreenWidget(
                currentTrackId: currentTrack?.id,
                favoriteIds: favoriteIds,
                downloadedIds: downloadedIds,
                downloadingIds: _downloadingIds,
                onTrackSelect: _playTrack,
                onToggleFavorite: _toggleFavorite,
                onDownloadTrack: _downloadTrack,
              ),
              LibraryScreenWidget(
                favorites: _favorites,
                currentTrackId: currentTrack?.id,
                downloadedIds: downloadedIds,
                downloadingIds: _downloadingIds,
                onTrackSelect: _playTrack,
                onToggleFavorite: _toggleFavorite,
                onDownloadTrack: _downloadTrack,
              ),
              DownloadsScreenWidget(
                downloads: _downloads,
                currentTrackId: currentTrack?.id,
                onTrackSelect: _playTrack,
                onDeleteDownload: _deleteDownload,
              ),
              const AboutScreenWidget(),
            ],
          ),

          // Docked MiniPlayer above Bottom Navigation
          if (currentTrack != null)
            Positioned(
              left: 0,
              right: 0,
              bottom: kBottomNavigationBarHeight,
              child: MiniPlayerWidget(
                playbackState: _playbackState,
                isFavorite: isCurrentTrackFavorite,
                onExpand: () => setState(() => _isFullPlayerExpanded = true),
                onPlayPause: () => widget.audioChannel.togglePlayPause(),
                onToggleFavorite: () => _toggleFavorite(currentTrack),
              ),
            ),

          // Full Player Modal Screen
          if (_isFullPlayerExpanded && currentTrack != null)
            Positioned.fill(
              child: FullPlayerScreen(
                playbackState: _playbackState,
                isFavorite: isCurrentTrackFavorite,
                isDownloaded: isCurrentTrackDownloaded,
                isDownloading: isCurrentTrackDownloading,
                onCollapse: () => setState(() => _isFullPlayerExpanded = false),
                onPlayPause: () => widget.audioChannel.togglePlayPause(),
                onPrevious: () => widget.audioChannel.skipPrevious(),
                onNext: () => widget.audioChannel.skipNext(),
                onSeek: (posMs) => widget.audioChannel.seek(posMs),
                onToggleFavorite: () => _toggleFavorite(currentTrack),
                onDownload: () => _downloadTrack(currentTrack),
              ),
            ),

          // Loading Buffering Overlay
          LoadingOverlayWidget(isBuffering: _playbackState.isBuffering),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentTabIndex,
        onTap: (index) => setState(() => _currentTabIndex = index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.search), label: 'Search'),
          BottomNavigationBarItem(icon: Icon(Icons.favorite), label: 'Library'),
          BottomNavigationBarItem(icon: Icon(Icons.download_done), label: 'Downloads'),
          BottomNavigationBarItem(icon: Icon(Icons.info), label: 'About'),
        ],
      ),
    );
  }
}
