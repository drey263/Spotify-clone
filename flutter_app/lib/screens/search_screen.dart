import 'dart:async';
import 'package:flutter/material.dart';
import '../models/track.dart';
import '../services/api_service.dart';
import '../theme/spotific_theme.dart';
import '../widgets/track_card.dart';

class SearchScreenWidget extends StatefulWidget {
  final String? currentTrackId;
  final Set<String> favoriteIds;
  final Set<String> downloadedIds;
  final Set<String> downloadingIds;
  final Function(Track, List<Track>) onTrackSelect;
  final Function(Track) onToggleFavorite;
  final Function(Track) onDownloadTrack;

  const SearchScreenWidget({
    Key? key,
    this.currentTrackId,
    required this.favoriteIds,
    required this.downloadedIds,
    required this.downloadingIds,
    required this.onTrackSelect,
    required this.onToggleFavorite,
    required this.onDownloadTrack,
  }) : super(key: key);

  @override
  State<SearchScreenWidget> createState() => _SearchScreenWidgetState();
}

class _SearchScreenWidgetState extends State<SearchScreenWidget> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  List<Track> _searchResults = [];
  bool _isSearching = false;
  bool _hasSearched = false;
  Timer? _debounce;

  final List<Map<String, dynamic>> _genreCategories = [
    {'name': 'Pop', 'colors': [const Color(0xFF1E90FF), const Color(0xFF00C6FF)]},
    {'name': 'Hip Hop', 'colors': [const Color(0xFF8A2387), const Color(0xFFE94057)]},
    {'name': 'Rock', 'colors': [const Color(0xFFFF416C), const Color(0xFFFF4B2B)]},
    {'name': 'R&B', 'colors': [const Color(0xFF654ea3), const Color(0xFFeaafc8)]},
    {'name': 'EDM', 'colors': [const Color(0xFF11998e), const Color(0xFF38ef7d)]},
    {'name': 'Indie', 'colors': [const Color(0xFFF37335), const Color(0xFFFDC830)]},
    {'name': 'Latin', 'colors': [const Color(0xFFe52d27), const Color(0xFFb31217)]},
    {'name': 'Chill', 'colors': [const Color(0xFF4776E6), const Color(0xFF8E54E9)]},
  ];

  void _onSearchChanged(String query) {
    _debounce?.cancel();
    if (query.trim().isEmpty) {
      setState(() {
        _searchResults = [];
        _hasSearched = false;
        _isSearching = false;
      });
      return;
    }

    _debounce = Timer(const Duration(milliseconds: 400), () async {
      setState(() {
        _isSearching = true;
        _hasSearched = true;
      });
      final results = await _apiService.search(query.trim());
      if (mounted) {
        setState(() {
          _searchResults = results;
          _isSearching = false;
        });
      }
    });
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: SpotificTheme.backgroundDark,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: Text(
                'Search',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: SpotificTheme.textWhite,
                ),
              ),
            ),

            // Pill-shaped search bar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 8.0),
              child: Container(
                decoration: BoxDecoration(
                  color: SpotificTheme.surfaceElevated,
                  borderRadius: BorderRadius.circular(30),
                  border: Border.all(color: Colors.white10),
                ),
                child: TextField(
                  controller: _searchController,
                  onChanged: _onSearchChanged,
                  style: const TextStyle(color: SpotificTheme.textWhite),
                  decoration: InputDecoration(
                    hintText: 'What do you want to listen to?',
                    hintStyle: const TextStyle(color: SpotificTheme.textMuted),
                    prefixIcon: const Icon(Icons.search, color: SpotificTheme.textGray),
                    suffixIcon: _searchController.text.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear, color: SpotificTheme.textGray),
                            onPressed: () {
                              _searchController.clear();
                              _onSearchChanged('');
                            },
                          )
                        : null,
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  ),
                ),
              ),
            ),

            // Body
            Expanded(
              child: _buildBody(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBody() {
    if (_isSearching) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            CircularProgressIndicator(color: SpotificTheme.electricBlue),
            SizedBox(height: 14),
            Text('Searching...', style: TextStyle(color: SpotificTheme.textGray)),
          ],
        ),
      );
    }

    if (_searchController.text.isEmpty) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(20, 16, 20, 12),
            child: Text(
              'Browse all',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: SpotificTheme.textWhite,
              ),
            ),
          ),
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 120),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
                childAspectRatio: 1.6,
              ),
              itemCount: _genreCategories.length,
              itemBuilder: (context, index) {
                final genre = _genreCategories[index];
                final colors = genre['colors'] as List<Color>;
                return GestureDetector(
                  onTap: () {
                    _searchController.text = genre['name'] as String;
                    _onSearchChanged(genre['name'] as String);
                  },
                  child: Container(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(12),
                      gradient: LinearGradient(colors: colors),
                    ),
                    padding: const EdgeInsets.all(14),
                    child: Text(
                      genre['name'] as String,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      );
    }

    if (_hasSearched && _searchResults.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.search_off, size: 56, color: SpotificTheme.textMuted),
            const SizedBox(height: 12),
            Text(
              'No results found for "${_searchController.text}"',
              style: const TextStyle(color: SpotificTheme.textGray, fontSize: 16),
            ),
            const SizedBox(height: 6),
            const Text(
              'Try searching for another song, artist, or album',
              style: TextStyle(color: SpotificTheme.textMuted, fontSize: 13),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.only(bottom = 120),
      itemCount: _searchResults.length,
      itemBuilder: (context, index) {
        final track = _searchResults[index];
        return TrackCardRowWidget(
          track: track,
          isPlaying: track.id == widget.currentTrackId,
          isFavorite: widget.favoriteIds.contains(track.id),
          isDownloaded: widget.downloadedIds.contains(track.id),
          isDownloading: widget.downloadingIds.contains(track.id),
          onTap: () => widget.onTrackSelect(track, _searchResults),
          onToggleFavorite: () => widget.onToggleFavorite(track),
          onDownload: () => widget.onDownloadTrack(track),
        );
      },
    );
  }
}
