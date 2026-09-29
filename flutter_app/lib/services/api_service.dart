import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/track.dart';

class ApiService {
  static const String baseUrl = 'https://api.nexray.eu.cc';

  Future<List<Track>> search(String query) async {
    try {
      final uri = Uri.parse('$baseUrl/search/spotify?q=${Uri.encodeComponent(query)}');
      final response = await http.get(uri).timeout(const Duration(seconds: 15));

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['status'] == true && data['result'] is List) {
          final list = data['result'] as List;
          return list.map((item) => Track.fromJson(item as Map<String, dynamic>)).toList();
        }
      }
      return [];
    } catch (e) {
      return [];
    }
  }

  Future<String?> resolveStreamUrl(Track track) async {
    if (track.isLocal && track.localFilePath != null) {
      return track.localFilePath;
    }

    // 1. Primary endpoint with retries
    if (track.url.isNotEmpty) {
      for (int attempt = 0; attempt <= 2; attempt++) {
        try {
          final primaryUri = Uri.parse('$baseUrl/downloader/spotify?url=${Uri.encodeComponent(track.url)}');
          final response = await http.get(primaryUri).timeout(const Duration(seconds: 15));
          if (response.statusCode == 200) {
            final data = json.decode(response.body);
            if (data['status'] == true && data['result'] != null) {
              final streamUrl = data['result']['url'] ?? data['result']['download_url'];
              if (streamUrl != null && streamUrl.toString().isNotEmpty) {
                return streamUrl.toString();
              }
            }
          }
        } catch (_) {}
        await Future.delayed(Duration(milliseconds: 500 * (attempt + 1)));
      }
    }

    // 2. Fallback endpoint: /downloader/spotifyplay?q={title} {artist}
    try {
      final q = Uri.encodeComponent('${track.title} ${track.artist}'.trim());
      final fallbackUri = Uri.parse('$baseUrl/downloader/spotifyplay?q=$q');
      final response = await http.get(fallbackUri).timeout(const Duration(seconds: 15));
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['status'] == true && data['result'] != null) {
          final streamUrl = data['result']['download_url'] ?? data['result']['url'];
          if (streamUrl != null && streamUrl.toString().isNotEmpty) {
            return streamUrl.toString();
          }
        }
      }
    } catch (_) {}

    return null;
  }
}
