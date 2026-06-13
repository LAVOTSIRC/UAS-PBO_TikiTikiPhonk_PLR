package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.SessionManager;
import com.plr.frontend.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SidebarController {

    @FXML private Label usernameInitialLabel;
    @FXML private Button tasksNavBtn;
    @FXML private Button statsNavBtn;
    @FXML private Button themeToggleBtn;
    @FXML private Label themeIconLabel;
    @FXML private Button logoutBtn;

    private Runnable onNavigateTasks;
    private Runnable onNavigateStats;

    public void setOnNavigateTasks(Runnable r) { this.onNavigateTasks = r; }
    public void setOnNavigateStats(Runnable r) { this.onNavigateStats = r; }

    @FXML
    public void initialize() {
        setActiveButton(tasksNavBtn);
        updateThemeIcon();
    }

    @FXML
    public void handleThemeToggle() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.toggle();
        tm.applyToScene(logoutBtn.getScene());
        updateThemeIcon();
    }

    private void updateThemeIcon() {
        if (themeIconLabel != null) {
            themeIconLabel.setText(ThemeManager.getInstance().isLightMode() ? "\u2600" : "\u263E");
        }
    }

    public void setUsername(String username) {
        if (usernameInitialLabel != null && username != null && !username.isEmpty()) {
            usernameInitialLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }
    }

    @FXML
    public void handleTasksNav() {
        setActiveButton(tasksNavBtn);
        if (onNavigateTasks != null) onNavigateTasks.run();
    }

    @FXML
    public void handleStatsNav() {
        setActiveButton(statsNavBtn);
        if (onNavigateStats != null) onNavigateStats.run();
    }


    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        JavaFXApp.showScene("fxml/login.fxml", "TikiTikiPhonk - Login", 480, 580);
    }

    private void setActiveButton(Button active) {
        Button[] allBtns = {tasksNavBtn, statsNavBtn};
        for (Button btn : allBtns) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }
        if (active != null) {
            if (!active.getStyleClass().contains("active")) {
                active.getStyleClass().add("active");
            }
        }
    }
}
