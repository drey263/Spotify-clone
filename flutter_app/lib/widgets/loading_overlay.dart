import 'package:flutter/material.dart';
import '../theme/spotific_theme.dart';

class LoadingOverlayWidget extends StatelessWidget {
  final bool isBuffering;
  final String statusText;

  const LoadingOverlayWidget({
    Key? key,
    required this.isBuffering,
    this.statusText = 'Connecting Stream...',
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (!isBuffering) return const SizedBox.shrink();

    return Container(
      color: Colors.black.withOpacity(0.6),
      child: Center(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
          decoration: BoxDecoration(
            color: SpotificTheme.surfaceElevated,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.white10),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const CircularProgressIndicator(
                color: SpotificTheme.electricBlue,
                strokeWidth = 3,
              ),
              const SizedBox(height: 14),
              Text(
                statusText,
                style: const TextStyle(
                  color: SpotificTheme.textWhite,
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
