package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.SessionManager;
import com.plr.frontend.util.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

public class SidebarController {

    @FXML private javafx.scene.shape.Circle userAvatarCircle;
    @FXML private Label avatarInitialLabel;
    @FXML private Button tasksNavBtn;
    @FXML private Button statsNavBtn;
    @FXML private Button themeToggleBtn;
    @FXML private Label themeIconLabel;
    @FXML private Button logoutBtn;

    private Runnable onNavigateTasks;
    private Runnable onNavigateStats;
    private Runnable onProfileNav;
    private Runnable onLogout;

    public void setOnNavigateTasks(Runnable r) { this.onNavigateTasks = r; }
    public void setOnNavigateStats(Runnable r) { this.onNavigateStats = r; }
    public void setOnLogout(Runnable r) { this.onLogout = r; }

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
        showInitial();
        loadProfilePicture();
    }

    private void showInitial() {
        String username = SessionManager.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            avatarInitialLabel.setText(username.substring(0, 1).toUpperCase());
            avatarInitialLabel.setVisible(true);
        }
    }

    private void loadImageToCircle(Image img) {
        Platform.runLater(() -> {
            userAvatarCircle.setFill(new javafx.scene.paint.ImagePattern(img));
            avatarInitialLabel.setVisible(false);
        });
    }

    public void loadProfilePicture() {
        Long userId = SessionManager.getInstance().getUserId();
        if (userId == null) return;

        new Thread(() -> {
            java.net.HttpURLConnection conn = null;
            try {
                String urlStr = "http://localhost:8080/api/users/profile-picture/file/" + userId;
                conn = (java.net.HttpURLConnection) new java.net.URL(urlStr).openConnection();
                conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    byte[] bytes;
                    try (java.io.InputStream in = conn.getInputStream();
                         java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                        bytes = out.toByteArray();
                    }
                    Image img = new Image(new java.io.ByteArrayInputStream(bytes), 34, 34, true, true);
                    loadImageToCircle(img);
                }
            } catch (Exception e) {
                // keep initial letter — Circle already has fill from FXML
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
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
        if (onLogout != null) onLogout.run();
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
