package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.SessionManager;
import com.plr.frontend.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SidebarController {

    @FXML private javafx.scene.shape.Circle userAvatarCircle;
    @FXML private Button tasksNavBtn;
    @FXML private Button statsNavBtn;
    @FXML private Button themeToggleBtn;
    @FXML private Label themeIconLabel;
    @FXML private Button logoutBtn;

    private Runnable onNavigateTasks;
    private Runnable onNavigateStats;
    private Runnable onProfileNav;

    public void setOnNavigateTasks(Runnable r) { this.onNavigateTasks = r; }
    public void setOnNavigateStats(Runnable r) { this.onNavigateStats = r; }

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

    @FXML
    public void initialize() {
        setActiveButton(tasksNavBtn);
        updateThemeIcon();
        
        // Load profil.png into the sidebar circle
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/profil.png"));
            if (userAvatarCircle != null) {
                userAvatarCircle.setFill(new javafx.scene.paint.ImagePattern(img));
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar profil di Sidebar: " + e.getMessage());
        }
    }

    public void setUsername(String username) {
        // Not used anymore as we use static image
    }

    @FXML
    public void handleTasksNav() {
        setActiveButton(tasksNavBtn);
        if (onNavigateTasks != null) onNavigateTasks.run();
    }

    @FXML
    public void handleProfileNav() {
        setActiveButton(null);
        if (userAvatarCircle != null && !userAvatarCircle.getStyleClass().contains("active")) {
            userAvatarCircle.getStyleClass().add("active");
        }
        if (onProfileNav != null) {
            onProfileNav.run();
        }
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
        if (userAvatarCircle != null) {
            userAvatarCircle.getStyleClass().remove("active");
        }
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

    public void setOnProfileNav(Runnable onProfileNav) {
        this.onProfileNav = onProfileNav;
    }
}
