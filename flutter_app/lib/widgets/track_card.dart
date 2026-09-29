import 'package:flutter/material.dart';
import '../models/track.dart';
import '../theme/spotific_theme.dart';

class TrackCardRowWidget extends StatelessWidget {
  final Track track;
  final bool isPlaying;
  final bool isFavorite;
  final bool isDownloaded;
  final bool isDownloading;
  final VoidCallback onTap;
  final VoidCallback onToggleFavorite;
  final VoidCallback onDownload;

  const TrackCardRowWidget({
    Key? key,
    required this.track,
    required this.isPlaying,
    required this.isFavorite,
    required this.isDownloaded,
    required this.isDownloading,
    required this.onTap,
    required this.onToggleFavorite,
    required this.onDownload,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Stack(
                alignment: Alignment.center,
                children: [
                  track.thumbnail.isNotEmpty
                      ? Image.network(
                          track.thumbnail,
                          width: 52,
                          height: 52,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            width: 52,
                            height: 52,
                            color: Colors.white10,
                            child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                          ),
                        )
                      : Container(
                          width: 52,
                          height: 52,
                          color: Colors.white10,
                          child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                        ),
                  if (isPlaying)
                    Container(
                      width: 52,
                      height: 52,
                      color: SpotificTheme.electricBlue.withOpacity(0.4),
                      child: const Icon(Icons.play_arrow, color: Colors.white, size: 28),
                    ),
                ],
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
                      Flexible(
                        child: Text(
                          track.artist,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 13,
                            color: SpotificTheme.textGray,
                          ),
                        ),
                      ),
                      if (track.duration != null && track.duration!.isNotEmpty) ...[
                        Text(
                          ' • ${track.duration}',
                          style: const TextStyle(
                            fontSize: 12,
                            color: SpotificTheme.textMuted,
                          ),
                        ),
                      ]
                    ],
                  ),
                ],
              ),
            ),
            if (isDownloading)
              const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2, color: SpotificTheme.electricBlue),
              )
            else
              IconButton(
                icon: Icon(
                  isDownloaded ? Icons.download_done : Icons.download,
                  color: isDownloaded ? SpotificTheme.electricBlue : SpotificTheme.textMuted,
                  size: 20,
                ),
                onPressed: onDownload,
              ),
            IconButton(
              icon: Icon(
                isFavorite ? Icons.favorite : Icons.favorite_border,
                color: isFavorite ? SpotificTheme.electricBlue : SpotificTheme.textMuted,
                size: 20,
              ),
              onPressed: onToggleFavorite,
            ),
          ],
        ),
      ),
    );
  }
}
