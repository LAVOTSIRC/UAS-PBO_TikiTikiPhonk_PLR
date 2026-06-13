package com.plr.frontend.controller;

import com.plr.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller untuk MainLayout.fxml (shell utama).
 * Memegang referensi ke semua sub-controller via fx:include injection.
 */
public class MainLayoutController {

    @FXML private SidebarController sidebarController;
    @FXML private TodoPanelController todoPanelController;
    @FXML private TimerPanelController timerPanelController;
    @FXML private AudioPanelController audioPanelController;
    
    @FXML private Label globalStatusLabel;
    private static MainLayoutController instance;

    @FXML
    public void initialize() {
        instance = this;
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

    public static void showGlobalNotification(String text, boolean isUndoOption, Runnable undoAction) {
        if (instance == null || instance.globalStatusLabel == null) return;
        
        Platform.runLater(() -> {
            instance.globalStatusLabel.setText(text);
            instance.globalStatusLabel.setVisible(true);
            
            if (isUndoOption && undoAction != null) {
                instance.globalStatusLabel.setStyle("-fx-background-color: #2D2936; -fx-text-fill: #C084FC; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-underline: true; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");
                instance.globalStatusLabel.setOnMouseClicked(e -> {
                    undoAction.run();
                    instance.globalStatusLabel.setVisible(false);
                    instance.globalStatusLabel.setOnMouseClicked(null);
                });
            } else {
                instance.globalStatusLabel.setStyle("-fx-background-color: #2D2936; -fx-text-fill: #F0EAFF; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: default; -fx-underline: false; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");
                instance.globalStatusLabel.setOnMouseClicked(null);
            }
            
            new Thread(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    if (instance.globalStatusLabel.getText().equals(text)) {
                        instance.globalStatusLabel.setVisible(false);
                    }
                });
            }).start();
        });
    }
}
