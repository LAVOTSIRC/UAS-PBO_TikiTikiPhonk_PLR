package com.plr;

import javafx.application.Application;

/**
 * Launcher entry point for TikiTikiPhonk JavaFX app.
 * Must be a non-Application class to avoid JavaFX module issues.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(com.plr.frontend.JavaFXApp.class, args);
    }
}
