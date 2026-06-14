package com.plr.frontend.util;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String PREFS_KEY = "theme_light_mode";

    private static ThemeManager instance;
    private boolean lightMode;
    private List<Runnable> changeListeners = new ArrayList<>();
    private final Preferences prefs;

    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        lightMode = prefs.getBoolean(PREFS_KEY, false);
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    private void savePreference() {
        prefs.putBoolean(PREFS_KEY, lightMode);
    }

    public boolean isLightMode() {
        return lightMode;
    }

    public void toggle() {
        lightMode = !lightMode;
        savePreference();
    }

    public void setLightMode(boolean light) {
        this.lightMode = light;
        savePreference();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void applyToScene(Scene scene) {
        if (scene == null) return;
        
        String popupLightCss = getClass().getResource("/css/popup-light.css").toExternalForm();
        
        if (lightMode) {
            scene.getRoot().getStyleClass().add("light-mode");
            if (!scene.getStylesheets().contains(popupLightCss)) {
                scene.getStylesheets().add(popupLightCss);
            }
        } else {
            scene.getRoot().getStyleClass().remove("light-mode");
            scene.getStylesheets().remove(popupLightCss);
        }
        changeListeners.forEach(Runnable::run);
    }
}
