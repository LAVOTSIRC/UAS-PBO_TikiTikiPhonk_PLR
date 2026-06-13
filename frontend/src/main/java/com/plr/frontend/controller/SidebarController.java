package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class SidebarController {

    @FXML private Label usernameInitialLabel;
    @FXML private Button tasksNavBtn;
    @FXML private Button timerNavBtn;
    @FXML private Button audioNavBtn;
    @FXML private Button settingsNavBtn;
    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        setActiveButton(tasksNavBtn);
    }

    public void setUsername(String username) {
        if (usernameInitialLabel != null && username != null && !username.isEmpty()) {
            usernameInitialLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }
    }

    @FXML
    public void handleTasksNav() {
        setActiveButton(tasksNavBtn);
    }

    @FXML
    public void handleTimerNav() {
        setActiveButton(timerNavBtn);
    }

    @FXML
    public void handleAudioNav() {
        setActiveButton(audioNavBtn);
    }

    @FXML
    public void handleSettings() {
        setActiveButton(settingsNavBtn);
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        JavaFXApp.showScene("fxml/login.fxml", "TikiTikiPhonk - Login", 480, 580);
    }

    private void setActiveButton(Button active) {
        Button[] allBtns = {tasksNavBtn, timerNavBtn, audioNavBtn, settingsNavBtn};
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
