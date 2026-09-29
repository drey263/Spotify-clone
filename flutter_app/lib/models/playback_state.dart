import 'track.dart';

class PlaybackStateModel {
  final Track? currentTrack;
  final bool isPlaying;
  final bool isBuffering;
  final int currentPositionMs;
  final int durationMs;
  final String? error;

  PlaybackStateModel({
    this.currentTrack,
    this.isPlaying = false,
    this.isBuffering = false,
    this.currentPositionMs = 0,
    this.durationMs = 0,
    this.error,
  });

  double get progress {
    if (durationMs > 0) {
      return (currentPositionMs / durationMs).clamp(0.0, 1.0);
    }
    return 0.0;
  }

  String get formattedPosition => formatMillis(currentPositionMs);
  String get formattedDuration {
    if (durationMs > 0) {
      return formatMillis(durationMs);
    }
    return currentTrack?.duration ?? '--:--';
  }

  static String formatMillis(int millis) {
    final totalSeconds = (millis / 1000).floor();
    final minutes = (totalSeconds / 60).floor();
    final seconds = totalSeconds % 60;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  factory PlaybackStateModel.fromMap(Map<dynamic, dynamic> map) {
    final title = map['title'] as String? ?? '';
    final artist = map['artist'] as String? ?? '';
    final url = map['url'] as String? ?? '';
    final trackId = map['trackId'] as String? ?? '';
    final thumbnail = map['thumbnail'] as String? ?? '';

    Track? track;
    if (title.isNotEmpty || url.isNotEmpty) {
      track = Track(
        id: trackId.isNotEmpty ? trackId : url,
        title: title,
        artist: artist,
        thumbnail: thumbnail,
        url: url,
      );
    }

    return PlaybackStateModel(
      currentTrack: track,
      isPlaying: map['isPlaying'] as bool? ?? false,
      isBuffering: map['isBuffering'] as bool? ?? false,
      currentPositionMs: (map['currentPositionMs'] as num?)?.toInt() ?? 0,
      durationMs: (map['durationMs'] as num?)?.toInt() ?? 0,
      error: map['error'] as String?,
    );
  }
}
