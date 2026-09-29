import 'dart:ui';
import 'package:flutter/material.dart';
import '../models/playback_state.dart';
import '../theme/spotific_theme.dart';

class FullPlayerScreen extends StatefulWidget {
  final PlaybackStateModel playbackState;
  final bool isFavorite;
  final bool isDownloaded;
  final bool isDownloading;
  final VoidCallback onCollapse;
  final VoidCallback onPlayPause;
  final VoidCallback onPrevious;
  final VoidCallback onNext;
  final ValueChanged<int> onSeek;
  final VoidCallback onToggleFavorite;
  final VoidCallback onDownload;

  const FullPlayerScreen({
    Key? key,
    required this.playbackState,
    required this.isFavorite,
    required this.isDownloaded,
    required this.isDownloading,
    required this.onCollapse,
    required this.onPlayPause,
    required this.onPrevious,
    required this.onNext,
    required this.onSeek,
    required this.onToggleFavorite,
    required this.onDownload,
  }) : super(key: key);

  @override
  State<FullPlayerScreen> createState() => _FullPlayerScreenState();
}

class _FullPlayerScreenState extends State<FullPlayerScreen> {
  double? _dragValue;

  @override
  Widget build(BuildContext context) {
    final track = widget.playbackState.currentTrack;
    if (track == null) return const SizedBox.shrink();

    final currentProgress = _dragValue ?? widget.playbackState.progress;

    return Scaffold(
      backgroundColor: SpotificTheme.backgroundDark,
      body: Stack(
        children: [
          // Blurred background album art
          if (track.thumbnail.isNotEmpty)
            Positioned.fill(
              child: Image.network(
                track.thumbnail,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => const SizedBox.shrink(),
              ),
            ),
          Positioned.fill(
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 45, sigmaY: 45),
              child: Container(
                color: Colors.black.withOpacity(0.75),
              ),
            ),
          ),

          // Content
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 12.0),
              child: Column(
                children: [
                  // Top Bar
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.keyboard_arrow_down, size: 32, color: Colors.white),
                        onPressed: widget.onCollapse,
                      ),
                      const Text(
                        'NOW PLAYING',
                        style: TextStyle(
                          color: SpotificTheme.textGray,
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 2,
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.more_vert, size: 24, color: Colors.white),
                        onPressed: () {},
                      ),
                    ],
                  ),

                  const Spacer(flex: 1),

                  // Square Album Art with Ambient Blue Glow
                  Container(
                    width: 290,
                    height: 290,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(
                          color: SpotificTheme.electricBlue.withOpacity(0.4),
                          blurRadius: 36,
                          spreadRadius: 4,
                        ),
                      ],
                    ),
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(20),
                      child: track.thumbnail.isNotEmpty
                          ? Image.network(
                              track.thumbnail,
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) => Container(
                                color: SpotificTheme.surfaceElevated,
                                child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue, size: 80),
                              ),
                            )
                          : Container(
                              color: SpotificTheme.surfaceElevated,
                              child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue, size: 80),
                            ),
                    ),
                  ),

                  const Spacer(flex: 1),

                  // Title and Artist
                  Text(
                    track.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: SpotificTheme.textWhite,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    track.artist,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 16,
                      color: SpotificTheme.textGray,
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Attached Knob Slider & Timers
                  Column(
                    children: [
                      SliderTheme(
                        data: SliderTheme.of(context).copyWith(
                          trackHeight: 3.5,
                          activeTrackColor: SpotificTheme.electricBlue,
                          inactiveTrackColor: Colors.white24,
                          thumbColor: SpotificTheme.electricBlue,
                          thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 7.5),
                          overlayColor: SpotificTheme.electricBlue.withOpacity(0.2),
                          overlayShape: const RoundSliderOverlayShape(overlayRadius: 16),
                        ),
                        child: Slider(
                          value: currentProgress.clamp(0.0, 1.0),
                          onChanged: (val) {
                            setState(() {
                              _dragValue = val;
                            });
                          },
                          onChangeEnd: (val) {
                            final targetMs = (val * widget.playbackState.durationMs).round();
                            widget.onSeek(targetMs);
                            setState(() {
                              _dragValue = null;
                            });
                          },
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8.0),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              _dragValue != null
                                  ? PlaybackStateModel.formatMillis((_dragValue! * widget.playbackState.durationMs).round())
                                  : widget.playbackState.formattedPosition,
                              style: const TextStyle(fontSize: 12, color: SpotificTheme.textMuted),
                            ),
                            Text(
                              widget.playbackState.formattedDuration,
                              style: const TextStyle(fontSize: 12, color: SpotificTheme.textMuted),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 20),

                  // Controls Row: Heart (left), Prev, Play/Pause, Next, Download (right)
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      IconButton(
                        icon: Icon(
                          widget.isFavorite ? Icons.favorite : Icons.favorite_border,
                          color: widget.isFavorite ? SpotificTheme.electricBlue : SpotificTheme.textGray,
                          size: 28,
                        ),
                        onPressed: widget.onToggleFavorite,
                      ),
                      IconButton(
                        icon: const Icon(Icons.skip_previous, size: 36, color: Colors.white),
                        onPressed: widget.onPrevious,
                      ),
                      GestureDetector(
                        onTap: widget.onPlayPause,
                        child: Container(
                          width: 68,
                          height: 68,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: const LinearGradient(
                              colors: [SpotificTheme.electricBlue, SpotificTheme.electricBlueLight],
                            ),
                            boxShadow: [
                              BoxShadow(
                                color: SpotificTheme.electricBlue.withOpacity(0.4),
                                blurRadius: 16,
                                spreadRadius: 2,
                              ),
                            ],
                          ),
                          child: Center(
                            child: widget.playbackState.isBuffering
                                ? const SizedBox(
                                    width: 28,
                                    height: 28,
                                    child: CircularProgressIndicator(color: Colors.white, strokeWidth: 3),
                                  )
                                : Icon(
                                    widget.playbackState.isPlaying ? Icons.pause : Icons.play_arrow,
                                    color: Colors.white,
                                    size: 38,
                                  ),
                          ),
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.skip_next, size: 36, color: Colors.white),
                        onPressed: widget.onNext,
                      ),
                      if (widget.isDownloading)
                        const SizedBox(
                          width: 24,
                          height: 24,
                          child: CircularProgressIndicator(color: SpotificTheme.electricBlue, strokeWidth: 2),
                        )
                      else
                        IconButton(
                          icon: Icon(
                            widget.isDownloaded ? Icons.download_done : Icons.download,
                            color: widget.isDownloaded ? SpotificTheme.electricBlue : SpotificTheme.textGray,
                            size: 26,
                          ),
                          onPressed: widget.onDownload,
                        ),
                    ],
                  ),

                  const Spacer(flex: 1),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
