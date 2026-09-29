import 'package:flutter/material.dart';
import '../theme/spotific_theme.dart';

class AboutScreenWidget extends StatelessWidget {
  const AboutScreenWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: SpotificTheme.backgroundDark,
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 24.0),
          children: [
            const SizedBox(height: 20),
            Center(
              child: Container(
                width: 90,
                height: 90,
                decoration: BoxDecoration(
                  color: SpotificTheme.surfaceElevated,
                  borderRadius: BorderRadius.circular(22),
                  boxShadow: [
                    BoxShadow(
                      color: SpotificTheme.electricBlue.withOpacity(0.4),
                      blurRadius: 24,
                    ),
                  ],
                ),
                child: const Icon(Icons.graphic_eq, color: SpotificTheme.electricBlue, size: 50),
              ),
            ),
            const SizedBox(height: 16),
            const Center(
              child: Text(
                'Spotific',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: SpotificTheme.electricBlue,
                ),
              ),
            ),
            const SizedBox(height: 4),
            const Center(
              child: Text(
                'Version 1.0.0',
                style: TextStyle(fontSize: 12, color: SpotificTheme.textMuted),
              ),
            ),
            const SizedBox(height: 16),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 16.0),
              child: Text(
                'A high-performance music streaming application with persistent background playback, low-latency audio engine, offline downloads, and Spotify music library integration.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: SpotificTheme.textGray, height: 1.4),
              ),
            ),
            const SizedBox(height: 32),
            Container(
              decoration: BoxDecoration(
                color: SpotificTheme.surfaceGlass,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                children: [
                  _buildRow(Icons.star, 'Rate App', 'Enjoying Spotific? Leave a review', () {}),
                  const Divider(height: 1, color: Colors.white10),
                  _buildRow(Icons.share, 'Share App', 'Share Spotific with friends & family', () {}),
                  const Divider(height: 1, color: Colors.white10),
                  _buildRow(Icons.lock, 'Privacy Policy', 'Learn how your data is protected', () {}),
                  const Divider(height: 1, color: Colors.white10),
                  _buildRow(Icons.email, 'Contact Support', 'Questions or feedback? Email us', () {}),
                ],
              ),
            ),
            const SizedBox(height: 40),
            const Center(
              child: Text(
                'Crafted with passion for music lovers worldwide.\nSpotific © 2026',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 12, color: SpotificTheme.textMuted, height: 1.5),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRow(IconData icon, String title, String subtitle, VoidCallback onTap) {
    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: SpotificTheme.surfaceElevated,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Icon(icon, color: SpotificTheme.electricBlue, size: 20),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14, color: SpotificTheme.textWhite)),
      subtitle: Text(subtitle, style: const TextStyle(fontSize: 12, color: SpotificTheme.textGray)),
      trailing: const Icon(Icons.arrow_forward_ios, size: 14, color: SpotificTheme.textMuted),
      onTap: onTap,
    );
  }
}
