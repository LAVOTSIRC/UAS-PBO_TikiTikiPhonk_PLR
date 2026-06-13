package com.plr.frontend.util;

import javafx.scene.Scene;

public class ThemeManager {

    private static ThemeManager instance;
    private boolean lightMode = false;

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public boolean isLightMode() {
        return lightMode;
    }

    public void toggle() {
        lightMode = !lightMode;
    }

    public void setLightMode(boolean light) {
        this.lightMode = light;
    }

    public void applyToScene(Scene scene) {
        if (scene == null) return;
        if (lightMode) {
            scene.getRoot().getStyleClass().add("light-mode");
        } else {
            scene.getRoot().getStyleClass().remove("light-mode");
        }
    }
}
