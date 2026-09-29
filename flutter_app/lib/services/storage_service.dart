import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/track.dart';
import 'api_service.dart';

class StorageService {
  static const String _keyFavorites = 'spotific_favorites';
  static const String _keyDownloads = 'spotific_downloads';

  final ApiService _apiService = ApiService();

  Future<List<Track>> getFavorites() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonList = prefs.getStringList(_keyFavorites) ?? [];
    return jsonList.map((item) => Track.fromJson(json.decode(item))).toList();
  }

  Future<void> toggleFavorite(Track track) async {
    final prefs = await SharedPreferences.getInstance();
    final list = await getFavorites();
    final index = list.indexWhere((t) => t.id == track.id);
    if (index >= 0) {
      list.removeAt(index);
    } else {
      list.insert(0, track);
    }
    final encoded = list.map((t) => json.encode(t.toJson())).toList();
    await prefs.setStringList(_keyFavorites, encoded);
  }

  Future<bool> isFavorite(String trackId) async {
    final list = await getFavorites();
    return list.any((t) => t.id == trackId);
  }

  Future<List<Track>> getDownloads() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonList = prefs.getStringList(_keyDownloads) ?? [];
    return jsonList.map((item) => Track.fromJson(json.decode(item))).toList();
  }

  Future<bool> isDownloaded(String trackId) async {
    final list = await getDownloads();
    return list.any((t) => t.id == trackId);
  }

  Future<bool> downloadTrack(Track track) async {
    try {
      final streamUrl = await _apiService.resolveStreamUrl(track);
      if (streamUrl == null) return false;

      final dir = await getApplicationDocumentsDirectory();
      final downloadsDir = Directory('${dir.path}/downloads');
      if (!await downloadsDir.exists()) {
        await downloadsDir.create(recursive: true);
      }

      final safeId = track.id.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');
      final file = File('${downloadsDir.path}/${safeId}_${DateTime.now().millisecondsSinceEpoch}.mp3');

      final response = await http.get(Uri.parse(streamUrl));
      if (response.statusCode == 200) {
        await file.writeAsBytes(response.bodyBytes);

        final localTrack = track.copyWith(
          isLocal: true,
          localFilePath: file.path,
        );

        final prefs = await SharedPreferences.getInstance();
        final list = await getDownloads();
        list.removeWhere((t) => t.id == track.id);
        list.insert(0, localTrack);

        final encoded = list.map((t) => json.encode(t.toJson())).toList();
        await prefs.setStringList(_keyDownloads, encoded);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<void> deleteDownload(String trackId) async {
    final list = await getDownloads();
    final index = list.indexWhere((t) => t.id == trackId);
    if (index >= 0) {
      final track = list[index];
      if (track.localFilePath != null) {
        try {
          final file = File(track.localFilePath!);
          if (await file.exists()) {
            await file.delete();
          }
        } catch (_) {}
      }
      list.removeAt(index);
      final prefs = await SharedPreferences.getInstance();
      final encoded = list.map((t) => json.encode(t.toJson())).toList();
      await prefs.setStringList(_keyDownloads, encoded);
    }
  }
}
