package com.plr.frontend.controller;

import com.plr.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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

        if (timerPanelController != null) {
            timerPanelController.loadSessionHistory();
        }
        if (todoPanelController != null) {
            todoPanelController.setOnTasksChanged(() -> {
                // Jangan otomatis set active task lagi, biarkan user yang milih manual via focusBtn
            });
            todoPanelController.loadTasks(() -> {
                // Kosongkan
            });
            
            todoPanelController.setOnFocusTaskRequested(taskDto -> {
                if (timerPanelController != null) {
                    timerPanelController.setActiveTask(taskDto.getTitle());
                    timerPanelController.setFocusedTaskId(taskDto.getId());
                }
            });
        }
    }

    public static void showGlobalNotification(String text, boolean isUndoOption, Runnable undoAction) {
        if (instance == null || instance.globalStatusLabel == null) return;
        
        Platform.runLater(() -> {
            boolean isLight = com.plr.frontend.util.ThemeManager.getInstance().isLightMode();
            String bgColor = isLight ? "#FFFFFF" : "#2D2936";
            String defaultText = isLight ? "#1E1A29" : "#F0EAFF";
            String undoText = isLight ? "#7C3AED" : "#C084FC";
            String dropShadow = isLight ? "rgba(0,0,0,0.1)" : "rgba(0,0,0,0.5)";

            instance.globalStatusLabel.setText(text);
            instance.globalStatusLabel.setVisible(true);
            
            if (isUndoOption && undoAction != null) {
                instance.globalStatusLabel.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-underline: true; -fx-effect: dropshadow(three-pass-box, %s, 10, 0, 0, 5);",
                    bgColor, undoText, dropShadow));
                instance.globalStatusLabel.setOnMouseClicked(e -> {
                    undoAction.run();
                    instance.globalStatusLabel.setVisible(false);
                    instance.globalStatusLabel.setOnMouseClicked(null);
                });
            } else {
                instance.globalStatusLabel.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: default; -fx-underline: false; -fx-effect: dropshadow(three-pass-box, %s, 10, 0, 0, 5);",
                    bgColor, defaultText, dropShadow));
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
