import 'package:flutter/material.dart';
import '../models/track.dart';
import '../theme/spotific_theme.dart';
import '../widgets/track_card.dart';

class LibraryScreenWidget extends StatelessWidget {
  final List<Track> favorites;
  final String? currentTrackId;
  final Set<String> downloadedIds;
  final Set<String> downloadingIds;
  final Function(Track, List<Track>) onTrackSelect;
  final Function(Track) onToggleFavorite;
  final Function(Track) onDownloadTrack;

  const LibraryScreenWidget({
    Key? key,
    required this.favorites,
    this.currentTrackId,
    required this.downloadedIds,
    required this.downloadingIds,
    required this.onTrackSelect,
    required this.onToggleFavorite,
    required this.onDownloadTrack,
  }) : super(key: key);

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
                'Your Library',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: SpotificTheme.textWhite,
                ),
              ),
            ),
            Expanded(
              child: favorites.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: const [
                          Icon(Icons.favorite_border, size: 64, color: SpotificTheme.textMuted),
                          SizedBox(height: 16),
                          Text(
                            'Your Library is empty',
                            style: TextStyle(fontSize: 18, color: SpotificTheme.textGray, fontWeight: FontWeight.bold),
                          ),
                          SizedBox(height: 6),
                          Text(
                            'Tracks you like will appear here',
                            style: TextStyle(fontSize: 13, color: SpotificTheme.textMuted),
                          ),
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.only(bottom = 120),
                      itemCount: favorites.length,
                      itemBuilder: (context, index) {
                        final track = favorites[index];
                        return TrackCardRowWidget(
                          track: track,
                          isPlaying: track.id == currentTrackId,
                          isFavorite: true,
                          isDownloaded: downloadedIds.contains(track.id),
                          isDownloading: downloadingIds.contains(track.id),
                          onTap: () => onTrackSelect(track, favorites),
                          onToggleFavorite: () => onToggleFavorite(track),
                          onDownload: () => onDownloadTrack(track),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
