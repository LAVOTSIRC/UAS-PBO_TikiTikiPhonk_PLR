package com.plr.frontend.util;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static ThemeManager instance;
    private boolean lightMode = false;
    private List<Runnable> changeListeners = new ArrayList<>();

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

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void applyToScene(Scene scene) {
        if (scene == null) return;
        if (lightMode) {
            scene.getRoot().getStyleClass().add("light-mode");
        } else {
            scene.getRoot().getStyleClass().remove("light-mode");
        }
        changeListeners.forEach(Runnable::run);
    }
}
