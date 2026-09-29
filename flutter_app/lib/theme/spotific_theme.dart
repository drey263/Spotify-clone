import 'package:flutter/material.dart';

class SpotificTheme {
  static const Color electricBlue = Color(0xFF1E90FF);
  static const Color electricBlueLight = Color(0xFF4FACFE);
  static const Color backgroundDark = Color(0xFF0A0A0F);
  static const Color surfaceDark = Color(0xFF13141F);
  static const Color surfaceElevated = Color(0xFF1B1D2C);
  static const Color surfaceGlass = Color(0x14FFFFFF);
  static const Color textWhite = Color(0xFFFFFFFF);
  static const Color textGray = Color(0xFFA0A0B0);
  static const Color textMuted = Color(0xFF6E6E82);

  static ThemeData get themeData {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: backgroundDark,
      primaryColor: electricBlue,
      colorScheme: const ColorScheme.dark(
        primary: electricBlue,
        secondary: electricBlueLight,
        surface: surfaceDark,
        background: backgroundDark,
      ),
      fontFamily: 'Roboto',
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: surfaceDark,
        selectedItemColor: electricBlue,
        unselectedItemColor: textMuted,
        type: BottomNavigationBarType.fixed,
        elevation: 0,
      ),
    );
  }
}
