package com.aicoach.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;

/** Device-local voice preferences; the shared enable flag remains in SettingsPrefs. */
public final class VoicePreferences {
    private final SharedPreferences prefs;
    public VoicePreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("aicoach_voice", Context.MODE_PRIVATE);
    }
    public float speed() { return clamp(prefs.getFloat("speed", 1f), .5f, 2f, 1f, .1f); }
    public float volume() { return clamp(prefs.getFloat("volume", 70f), 0f, 100f, 70f, 5f); }
    public String voice() { return prefs.getString("voice", ""); }
    public boolean enabled(String category) { return prefs.getBoolean(category, !"rest".equals(category)); }
    public void speed(float value) { prefs.edit().putFloat("speed", clamp(value, .5f, 2f, 1f, .1f)).apply(); }
    public void volume(float value) { prefs.edit().putFloat("volume", clamp(value, 0f, 100f, 70f, 5f)).apply(); }
    public void voice(String value) { prefs.edit().putString("voice", value).apply(); }
    public void enabled(String category, boolean value) { prefs.edit().putBoolean(category, value).apply(); }
    private static float clamp(float value, float min, float max, float fallback, float step) {
        if (!Float.isFinite(value)) return fallback;
        return Math.round(Math.max(min, Math.min(max, value)) / step) * step;
    }
}
