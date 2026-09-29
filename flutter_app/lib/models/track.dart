class Track {
  final String id;
  final String title;
  final String artist;
  final String thumbnail;
  final String url;
  final String? duration;
  final bool isLocal;
  final String? localFilePath;

  Track({
    required this.id,
    required this.title,
    required this.artist,
    required this.thumbnail,
    required this.url,
    this.duration,
    this.isLocal = false,
    this.localFilePath,
  });

  factory Track.fromJson(Map<String, dynamic> json) {
    final title = json['title'] as String? ?? 'Unknown Title';
    final artist = json['artist'] as String? ?? 'Unknown Artist';
    final url = json['url'] as String? ?? '';
    final id = json['id'] as String? ?? (url.isNotEmpty ? url : '${title}_$artist');

    return Track(
      id: id,
      title: title,
      artist: artist,
      thumbnail: json['thumbnail'] as String? ?? '',
      url: url,
      duration: json['duration'] as String?,
      isLocal: json['isLocal'] as bool? ?? false,
      localFilePath: json['localFilePath'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'artist': artist,
      'thumbnail': thumbnail,
      'url': url,
      'duration': duration,
      'isLocal': isLocal,
      'localFilePath': localFilePath,
    };
  }

  Track copyWith({
    String? id,
    String? title,
    String? artist,
    String? thumbnail,
    String? url,
    String? duration,
    bool? isLocal,
    String? localFilePath,
  }) {
    return Track(
      id: id ?? this.id,
      title: title ?? this.title,
      artist: artist ?? this.artist,
      thumbnail: thumbnail ?? this.thumbnail,
      url: url ?? this.url,
      duration: duration ?? this.duration,
      isLocal: isLocal ?? this.isLocal,
      localFilePath: localFilePath ?? this.localFilePath,
    );
  }
}
