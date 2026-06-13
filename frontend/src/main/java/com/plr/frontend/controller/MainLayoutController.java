package com.plr.frontend.controller;

import com.plr.frontend.util.SessionManager;
import javafx.fxml.FXML;

public class MainLayoutController {

    @FXML private SidebarController sidebarController;
    @FXML private TodoPanelController todoPanelController;
    @FXML private TimerPanelController timerPanelController;
    @FXML private AudioPanelController audioPanelController;

    @FXML
    public void initialize() {
        String username = SessionManager.getInstance().getUsername();
        if (sidebarController != null) {
            sidebarController.setUsername(username);
        }

        if (timerPanelController != null) {
            timerPanelController.loadSessionHistory();
        }

        if (todoPanelController != null) {
            todoPanelController.setOnTasksChanged(() -> {
                String activeTask = todoPanelController.getFirstActiveTask();
                if (timerPanelController != null) {
                    timerPanelController.setActiveTask(activeTask);
                }
            });
            todoPanelController.loadTasks(() -> {
                String activeTask = todoPanelController.getFirstActiveTask();
                if (timerPanelController != null) {
                    timerPanelController.setActiveTask(activeTask);
                }
            });
        }
    }
}
