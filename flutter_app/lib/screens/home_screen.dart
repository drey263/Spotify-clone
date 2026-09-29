import 'package:flutter/material.dart';
import '../models/track.dart';
import '../services/api_service.dart';
import '../theme/spotific_theme.dart';

class HomeScreenWidget extends StatefulWidget {
  final Function(Track, List<Track>) onTrackSelect;
  final VoidCallback onNavigateToSearch;

  const HomeScreenWidget({
    Key? key,
    required this.onTrackSelect,
    required this.onNavigateToSearch,
  }) : super(key: key);

  @override
  State<HomeScreenWidget> createState() => _HomeScreenWidgetState();
}

class _HomeScreenWidgetState extends State<HomeScreenWidget> {
  final ApiService _apiService = ApiService();
  List<Track> _trendingTracks = [];
  List<Track> _popularTracks = [];
  bool _isLoading = true;

  final List<Map<String, String>> _topArtists = [
    {'name': 'The Weeknd', 'image': 'https://i.scdn.co/image/ab6761610000e5eb214f3cf1cbe7139c1e26ffbb'},
    {'name': 'Taylor Swift', 'image': 'https://i.scdn.co/image/ab6761610000e5eb5a00969a4698c3132a15fbb0'},
    {'name': 'Drake', 'image': 'https://i.scdn.co/image/ab6761610000e5eb4293385d324e238817b44bd5'},
    {'name': 'Billie Eilish', 'image': 'https://i.scdn.co/image/ab6761610000e5ebd8b9980db67272cb4d2c3daf'},
    {'name': 'Dua Lipa', 'image': 'https://i.scdn.co/image/ab6761610000e5ebd42a27db3286b58553da8858'},
  ];

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    final trending = await _apiService.search('Trending Hits');
    final popular = await _apiService.search('Today Top Hits');
    if (mounted) {
      setState(() {
        _trendingTracks = trending;
        _popularTracks = popular;
        _isLoading = false;
      });
    }
  }

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(
        child: CircularProgressIndicator(color: SpotificTheme.electricBlue),
      );
    }

    return Scaffold(
      backgroundColor: SpotificTheme.backgroundDark,
      body: SafeArea(
        child: RefreshIndicator(
          color: SpotificTheme.electricBlue,
          onRefresh: _loadData,
          child: ListView(
            padding: const EdgeInsets.only(bottom = 120),
            children: [
              // Greeting Header with Search Icon (NO notification bell)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      _getGreeting(),
                      style: const TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                        color: SpotificTheme.textWhite,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.search, size: 28, color: SpotificTheme.textWhite),
                      onPressed: widget.onNavigateToSearch,
                    ),
                  ],
                ),
              ),

              // Hero Banner of Featured Tracks
              if (_trendingTracks.isNotEmpty) ...[
                Builder(builder: (context) {
                  final featured = _trendingTracks.first;
                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20.0),
                    child: GestureDetector(
                      onTap: () => widget.onTrackSelect(featured, _trendingTracks),
                      child: Container(
                        height: 180,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(16),
                          image: featured.thumbnail.isNotEmpty
                              ? DecorationImage(
                                  image: NetworkImage(featured.thumbnail),
                                  fit: BoxFit.cover,
                                )
                              : null,
                          color: SpotificTheme.surfaceElevated,
                        ),
                        child: Container(
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(16),
                            gradient: LinearGradient(
                              begin: Alignment.topCenter,
                              end: Alignment.bottomCenter,
                              colors: [Colors.transparent, Colors.black.withOpacity(0.85)],
                            ),
                          ),
                          padding: const EdgeInsets.all(16),
                          alignment: Alignment.bottomLeft,
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text(
                                      'FEATURED TRACK',
                                      style: TextStyle(
                                        color: SpotificTheme.electricBlue,
                                        fontSize: 11,
                                        fontWeight: FontWeight.bold,
                                        letterSpacing: 1.5,
                                      ),
                                    ),
                                    Text(
                                      featured.title,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        fontSize: 20,
                                        fontWeight: FontWeight.bold,
                                        color: Colors.white,
                                      ),
                                    ),
                                    Text(
                                      featured.artist,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: SpotificTheme.textGray,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              Container(
                                width: 48,
                                height: 48,
                                decoration: const BoxDecoration(
                                  color: SpotificTheme.electricBlue,
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(Icons.play_arrow, color: Colors.white, size: 30),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  );
                }),
              ],

              // Top Artists Row
              const Padding(
                padding: EdgeInsets.fromLTRB(20, 24, 20, 12),
                child: Text(
                  'Top Artists',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: SpotificTheme.textWhite,
                  ),
                ),
              ),
              SizedBox(
                height: 115,
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  scrollDirection: Axis.horizontal,
                  itemCount: _topArtists.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 16),
                  itemBuilder: (context, index) {
                    final artist = _topArtists[index];
                    return GestureDetector(
                      onTap: () async {
                        final tracks = await _apiService.search(artist['name']!);
                        if (tracks.isNotEmpty) {
                          widget.onTrackSelect(tracks.first, tracks);
                        }
                      },
                      child: Column(
                        children: [
                          CircleAvatar(
                            radius: 36,
                            backgroundImage: NetworkImage(artist['image']!),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            artist['name']!,
                            style: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                              color: SpotificTheme.textWhite,
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ),

              // Trending Now Row
              if (_trendingTracks.isNotEmpty) ...[
                const Padding(
                  padding: EdgeInsets.fromLTRB(20, 24, 20, 12),
                  child: Text(
                    'Trending Now',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: SpotificTheme.textWhite,
                    ),
                  ),
                ),
                SizedBox(
                  height: 180,
                  child: ListView.separated(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    scrollDirection: Axis.horizontal,
                    itemCount: _trendingTracks.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 14),
                    itemBuilder: (context, index) {
                      final track = _trendingTracks[index];
                      return GestureDetector(
                        onTap: () => widget.onTrackSelect(track, _trendingTracks),
                        child: SizedBox(
                          width: 130,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              ClipRRect(
                                borderRadius: BorderRadius.circular(12),
                                child: Image.network(
                                  track.thumbnail,
                                  width: 130,
                                  height: 130,
                                  fit: BoxFit.cover,
                                  errorBuilder: (_, __, ___) => Container(
                                    width: 130,
                                    height: 130,
                                    color: SpotificTheme.surfaceElevated,
                                    child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                                  ),
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                track.title,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w600,
                                  color: SpotificTheme.textWhite,
                                ),
                              ),
                              Text(
                                track.artist,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 11,
                                  color: SpotificTheme.textGray,
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ],

              // Popular Hits Row
              if (_popularTracks.isNotEmpty) ...[
                const Padding(
                  padding: EdgeInsets.fromLTRB(20, 24, 20, 12),
                  child: Text(
                    'Today\'s Top Hits',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: SpotificTheme.textWhite,
                    ),
                  ),
                ),
                SizedBox(
                  height: 180,
                  child: ListView.separated(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    scrollDirection: Axis.horizontal,
                    itemCount: _popularTracks.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 14),
                    itemBuilder: (context, index) {
                      final track = _popularTracks[index];
                      return GestureDetector(
                        onTap: () => widget.onTrackSelect(track, _popularTracks),
                        child: SizedBox(
                          width: 130,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              ClipRRect(
                                borderRadius: BorderRadius.circular(12),
                                child: Image.network(
                                  track.thumbnail,
                                  width: 130,
                                  height: 130,
                                  fit: BoxFit.cover,
                                  errorBuilder: (_, __, ___) => Container(
                                    width: 130,
                                    height: 130,
                                    color: SpotificTheme.surfaceElevated,
                                    child: const Icon(Icons.music_note, color: SpotificTheme.electricBlue),
                                  ),
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                track.title,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w600,
                                  color: SpotificTheme.textWhite,
                                ),
                              ),
                              Text(
                                track.artist,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 11,
                                  color: SpotificTheme.textGray,
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
