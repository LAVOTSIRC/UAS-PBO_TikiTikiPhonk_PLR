package com.plr.frontend.model;

/**
 * Enum yang mendefinisikan semua jenis noise bawaan.
 * Setiap entry berisi path resource audio, nama tampilan, dan emoji ikon.
 */
public enum NoiseType {

    WHITE_NOISE("audio/white_noise.mp3", "White Noise", "\uD83C\uDF07"),   // 🌇
    BROWN_NOISE("audio/brown_noise.mp3", "Brown Noise", "\u2615"),          // ☕
    RAIN      ("audio/rain.mp3",         "Rain",        "\uD83C\uDF27"),    // 🌧
    FOREST    ("audio/forest.mp3",       "Forest",      "\uD83C\uDF3F");    // 🌿

    private final String resourcePath;
    private final String displayName;
    private final String emoji;

    NoiseType(String resourcePath, String displayName, String emoji) {
        this.resourcePath = resourcePath;
        this.displayName  = displayName;
        this.emoji        = emoji;
    }

    public String getResourcePath() { return resourcePath; }
    public String getDisplayName()  { return displayName;  }
    public String getEmoji()        { return emoji;        }

    /** Label lengkap: "🌧 Rain" */
    public String getFullLabel() {
        return emoji + " " + displayName;
    }
}
