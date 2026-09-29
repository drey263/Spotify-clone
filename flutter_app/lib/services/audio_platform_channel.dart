import 'dart:async';
import 'package:flutter/services.dart';
import '../models/playback_state.dart';
import '../models/track.dart';

class AudioPlatformChannel {
  static const MethodChannel _methodChannel = MethodChannel('com.spotific.audio/methods');
  static const EventChannel _eventChannel = EventChannel('com.spotific.audio/events');

  final StreamController<PlaybackStateModel> _stateController = StreamController<PlaybackStateModel>.broadcast();
  Stream<PlaybackStateModel> get stateStream => _stateController.stream;

  PlaybackStateModel _currentState = PlaybackStateModel();
  PlaybackStateModel get currentState => _currentState;

  StreamSubscription? _eventSub;

  Future<void> init() async {
    // Critical requirement:
    // Query currently running Kotlin service state BEFORE building UI on startup
    try {
      final stateMap = await _methodChannel.invokeMethod('getState');
      if (stateMap is Map) {
        _currentState = PlaybackStateModel.fromMap(stateMap);
        _stateController.add(_currentState);
      }
    } catch (e) {
      // Service might not be started yet
    }

    // Subscribe to EventChannel for live playback state updates
    try {
      _eventSub = _eventChannel.receiveBroadcastStream().listen((event) {
        if (event is Map) {
          _currentState = PlaybackStateModel.fromMap(event);
          _stateController.add(_currentState);
        }
      });
    } catch (e) {
      // Event channel registration
    }
  }

  Future<void> playTrack(Track track) async {
    await _methodChannel.invokeMethod('play', {
      'id': track.id,
      'title': track.title,
      'artist': track.artist,
      'thumbnail': track.thumbnail,
      'url': track.url,
      'duration': track.duration,
      'isLocal': track.isLocal,
      'localFilePath': track.localFilePath,
    });
  }

  Future<void> pause() async {
    await _methodChannel.invokeMethod('pause');
  }

  Future<void> resume() async {
    await _methodChannel.invokeMethod('resume');
  }

  Future<void> togglePlayPause() async {
    await _methodChannel.invokeMethod('togglePlayPause');
  }

  Future<void> seek(int positionMs) async {
    await _methodChannel.invokeMethod('seek', {'positionMs': positionMs});
  }

  Future<void> skipNext() async {
    await _methodChannel.invokeMethod('skipNext');
  }

  Future<void> skipPrevious() async {
    await _methodChannel.invokeMethod('skipPrevious');
  }

  void dispose() {
    _eventSub?.cancel();
    _stateController.close();
  }
}
