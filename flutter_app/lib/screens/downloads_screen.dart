import 'package:flutter/material.dart';
import '../models/track.dart';
import '../theme/spotific_theme.dart';

class DownloadsScreenWidget extends StatelessWidget {
  final List<Track> downloads;
  final String? currentTrackId;
  final Function(Track, List<Track>) onTrackSelect;
  final Function(Track) onDeleteDownload;

  const DownloadsScreenWidget({
    Key? key,
    required this.downloads,
    this.currentTrackId,
    required this.onTrackSelect,
    required this.onDeleteDownload,
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
                'Downloads',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: SpotificTheme.textWhite,
                ),
              ),
            ),
            Expanded(
              child: downloads.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: const [
                          Icon(Icons.folder_open, size: 64, color: SpotificTheme.textMuted),
                          SizedBox(height: 16),
                          Text(
                            'No downloads yet',
                            style: TextStyle(fontSize: 18, color: SpotificTheme.textGray, fontWeight: FontWeight.bold),
                          ),
                          SizedBox(height: 6),
                          Text(
                            'Downloaded songs for offline playback appear here',
                            style: TextStyle(fontSize: 13, color: SpotificTheme.textMuted),
                          ),
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.only(bottom = 120),
                      itemCount: downloads.length,
                      itemBuilder: (context, index) {
                        final track = downloads[index];
                        final isPlaying = track.id == currentTrackId;

                        return InkWell(
                          onTap: () => onTrackSelect(track, downloads),
                          child: Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                            child: Row(
                              children: [
                                ClipRRect(
                                  borderRadius: BorderRadius.circular(8),
                                  child: track.thumbnail.isNotEmpty
                                      ? Image.network(
                                          track.thumbnail,
                                          width: 52,
                                          height: 52,
                                          fit: BoxFit.cover,
                                          errorBuilder: (_, __, ___) => Container(
                                            width: 52,
                                            height: 52,
                                            color: SpotificTheme.surfaceElevated,
                                            child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                                          ),
                                        )
                                      : Container(
                                          width: 52,
                                          height: 52,
                                          color: SpotificTheme.surfaceElevated,
                                          child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                                        ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        track.title,
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                        style: TextStyle(
                                          fontSize: 15,
                                          fontWeight: FontWeight.w600,
                                          color: isPlaying ? SpotificTheme.electricBlue : SpotificTheme.textWhite,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Row(
                                        children: [
                                          const Icon(Icons.download_done, color: SpotificTheme.electricBlue, size: 14),
                                          const SizedBox(width: 4),
                                          Expanded(
                                            child: Text(
                                              '${track.artist} • Offline',
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                              style: const TextStyle(fontSize: 12, color: SpotificTheme.textGray),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ],
                                  ),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.delete_outline, color: SpotificTheme.textMuted, size: 22),
                                  onPressed: () => onDeleteDownload(track),
                                ),
                              ],
                            ),
                          ),
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
