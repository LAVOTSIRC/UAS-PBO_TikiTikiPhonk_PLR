package com.plr.frontend.controller;

import com.plr.frontend.util.SessionManager;
import javafx.fxml.FXML;

/**
 * Controller untuk MainLayout.fxml (shell utama).
 * Memegang referensi ke semua sub-controller via fx:include injection.
 */
public class MainLayoutController {

    @FXML private SidebarController sidebarController;
    @FXML private TodoPanelController todoPanelController;
    @FXML private TimerPanelController timerPanelController;
    @FXML private AudioPanelController audioPanelController;

    @FXML
    public void initialize() {
        // Pass username to sidebar for display
        String username = SessionManager.getInstance().getUsername();
        if (sidebarController != null) {
            sidebarController.setUsername(username);
        }
        // Load initial task data
        if (todoPanelController != null) {
            todoPanelController.loadTasks();
        }
        // Load pomodoro session history
        if (timerPanelController != null) {
            timerPanelController.loadSessionHistory();
        }
    }
}
