import 'package:flutter/material.dart';
import '../models/playback_state.dart';
import '../theme/spotific_theme.dart';

class MiniPlayerWidget extends StatelessWidget {
  final PlaybackStateModel playbackState;
  final bool isFavorite;
  final VoidCallback onExpand;
  final VoidCallback onPlayPause;
  final VoidCallback onToggleFavorite;

  const MiniPlayerWidget({
    Key? key,
    required this.playbackState,
    required this.isFavorite,
    required this.onExpand,
    required this.onPlayPause,
    required this.onToggleFavorite,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final track = playbackState.currentTrack;
    if (track == null) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
      child: GestureDetector(
        onTap: onExpand,
        child: Container(
          decoration: BoxDecoration(
            color: SpotificTheme.surfaceElevated,
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: Colors.white.withOpacity(0.08), width: 1),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 8.0),
                child: Row(
                  children: [
                    // Small square art
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: track.thumbnail.isNotEmpty
                          ? Image.network(
                              track.thumbnail,
                              width: 44,
                              height: 44,
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) => Container(
                                width: 44,
                                height: 44,
                                color: Colors.white10,
                                child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                              ),
                            )
                          : Container(
                              width: 44,
                              height: 44,
                              color: Colors.white10,
                              child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                            ),
                    ),
                    const SizedBox(width: 12),
                    // Title and artist stacked
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            track.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                              color: SpotificTheme.textWhite,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            track.artist,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontSize: 12,
                              color: SpotificTheme.textGray,
                            ),
                          ),
                        ],
                      ),
                    ),
                    // Heart icon
                    IconButton(
                      icon: Icon(
                        isFavorite ? Icons.favorite : Icons.favorite_border,
                        color: isFavorite ? SpotificTheme.electricBlue : SpotificTheme.textMuted,
                        size: 22,
                      ),
                      onPressed: onToggleFavorite,
                    ),
                    // Play/pause icon
                    IconButton(
                      icon: playbackState.isBuffering
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: SpotificTheme.electricBlue,
                              ),
                            )
                          : Icon(
                              playbackState.isPlaying ? Icons.pause : Icons.play_arrow,
                              color: SpotificTheme.electricBlue,
                              size: 28,
                            ),
                      onPressed: onPlayPause,
                    ),
                  ],
                ),
              ),
              // Slim progress bar along bottom
              ClipRRect(
                borderRadius: const BorderRadius.vertical(bottom: Radius.circular(14)),
                child: LinearProgressIndicator(
                  value: playbackState.progress,
                  minHeight: 2.5,
                  backgroundColor: Colors.white10,
                  valueColor: const AlwaysStoppedAnimation<Color>(SpotificTheme.electricBlue),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
