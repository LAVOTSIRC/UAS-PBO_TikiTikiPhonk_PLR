package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoPanelController {

    @FXML private TextField taskInputField;
    @FXML private ListView<String> activeTasksList;
    @FXML private ListView<String> completedTasksList;
    @FXML private Label sessionCountLabel;
    @FXML private Label statusLabel;

    private final ObservableList<String> activeTasks = FXCollections.observableArrayList();
    private final ObservableList<String> completedTasks = FXCollections.observableArrayList();
    // Map to store task title -> id mapping for deletion/update
    private final Map<String, Long> taskIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        activeTasksList.setItems(activeTasks);
        completedTasksList.setItems(completedTasks);

        // Double-click active task to mark as done
        activeTasksList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = activeTasksList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    markTaskDone(selected);
                }
            }
        });
    }

    public void loadTasks() {
        Task<List<Map<String, Object>>> fetchTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getTasks();
            }
        };

        fetchTask.setOnSucceeded(e -> {
            List<Map<String, Object>> tasks = fetchTask.getValue();
            activeTasks.clear();
            completedTasks.clear();
            taskIdMap.clear();

            int doneCount = 0;
            for (Map<String, Object> t : tasks) {
                String title = (String) t.get("title");
                String status = (String) t.get("status");
                Long id = ((Number) t.get("id")).longValue();
                taskIdMap.put(title, id);

                if ("DONE".equals(status)) {
                    completedTasks.add("✓ " + title);
                    doneCount++;
                } else {
                    activeTasks.add(title);
                }
            }

            final int finalDone = doneCount;
            Platform.runLater(() -> {
                if (sessionCountLabel != null) {
                    sessionCountLabel.setText(finalDone + " dari " + tasks.size() + " selesai");
                }
            });
        });

        fetchTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Gagal memuat tugas");
            });
        });

        new Thread(fetchTask).start();
    }

    @FXML
    public void handleAddTask() {
        String title = taskInputField.getText().trim();
        if (title.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("status", "TODO");

        Task<Map<String, Object>> createTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().createTask(data);
            }
        };

        createTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                taskInputField.clear();
                loadTasks();
            });
        });

        createTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("Gagal: " + createTask.getException().getMessage());
            });
        });

        new Thread(createTask).start();
    }

    private void markTaskDone(String taskTitle) {
        Long id = taskIdMap.get(taskTitle);
        if (id == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("title", taskTitle);
        data.put("status", "DONE");

        Task<Map<String, Object>> updateTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().updateTask(id, data);
            }
        };

        updateTask.setOnSucceeded(e -> Platform.runLater(this::loadTasks));
        updateTask.setOnFailed(e -> {});
        new Thread(updateTask).start();
    }

    @FXML
    public void handleDeleteSelected() {
        String selected = activeTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) selected = completedTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String cleanTitle = selected.startsWith("✓ ") ? selected.substring(2) : selected;
        Long id = taskIdMap.get(cleanTitle);
        if (id == null) return;

        final Long taskId = id;
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().deleteTask(taskId);
                return null;
            }
        };

        deleteTask.setOnSucceeded(e -> Platform.runLater(this::loadTasks));
        deleteTask.setOnFailed(e -> {});
        new Thread(deleteTask).start();
    }
}
