package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
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
    private Runnable onTasksChanged;

    // BUG-07 FIX: Ganti Map<String, Long> (title→id, rawan duplikat) dengan
    // cache bertipe Map<Long, Map<String,Object>> (id→fullData).
    // ID di-encode ke display string sebagai "title|id" agar selalu unik.
    private final Map<Long, Map<String, Object>> taskCache = new HashMap<>();

    @FXML
    public void initialize() {
        activeTasksList.setItems(activeTasks);
        completedTasksList.setItems(completedTasks);

        // Gunakan CellFactory kustom untuk merender checkbox lingkaran + teks seperti desain referensi
        activeTasksList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(8);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    javafx.scene.shape.Circle check = new javafx.scene.shape.Circle(8);
                    check.getStyleClass().add("todo-check-circle");
                    
                    Label text = new Label(extractTitle(item));
                    text.getStyleClass().add("todo-text");
                    
                    root.getChildren().addAll(check, text);
                    setGraphic(root);
                    setText(null);
                }
            }
        });

        completedTasksList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Hapus prefix "✓ " karena kita merender GUI kustom
                    String displayTitle = extractTitle(item);
                    if (displayTitle.startsWith("✓ ")) {
                        displayTitle = displayTitle.substring(2);
                    }

                    javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(8);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    javafx.scene.shape.Circle check = new javafx.scene.shape.Circle(8);
                    check.getStyleClass().addAll("todo-check-circle", "todo-checked");
                    
                    Label text = new Label(displayTitle);
                    text.getStyleClass().addAll("todo-text", "todo-text-done");
                    
                    root.getChildren().addAll(check, text);
                    setGraphic(root);
                    setText(null);
                }
            }
        });

        // Double-click active task untuk menandai selesai
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
            taskCache.clear();

            int doneCount = 0;
            for (Map<String, Object> t : tasks) {
                String title = (String) t.get("title");
                String status = (String) t.get("status");
                Long id = ((Number) t.get("id")).longValue();

                // BUG-07 FIX: Simpan full task data di cache dengan key = id (unik)
                taskCache.put(id, t);

                // Encode id ke display string: "title|id" — dijamin unik walau judul sama
                String displayKey = title + "|" + id;

                if ("DONE".equals(status)) {
                    completedTasks.add("✓ " + displayKey);
                    doneCount++;
                } else {
                    activeTasks.add(displayKey);
                }
            }

            final int finalDone = doneCount;
            Platform.runLater(() -> {
                if (sessionCountLabel != null) {
                    sessionCountLabel.setText(finalDone + " dari " + tasks.size() + " selesai");
                }
                if (onTasksChanged != null) onTasksChanged.run();
            });
        });

        fetchTask.setOnFailed(e -> {
            // BUG-19 FIX: Tampilkan error yang informatif, bukan silent fail
            Platform.runLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("⚠ Gagal memuat tugas: " + fetchTask.getException().getMessage());
            });
        });

        new Thread(fetchTask).start();
    }

    @FXML
    public void handleAddTask() {
        String title = taskInputField.getText().trim();
        if (title.isEmpty()) {
            // Feedback visual: beritahu user bahwa nama tugas harus diisi
            if (statusLabel != null) {
                statusLabel.setText("⚠ Ketik nama tugas dulu!");
            }
            taskInputField.requestFocus();
            return;
        }

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
                if (statusLabel != null) statusLabel.setText("");
                loadTasks();
            });
        });

        createTask.setOnFailed(e -> {
            // BUG-19 FIX: Tampilkan error yang informatif
            Platform.runLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("⚠ Gagal tambah: " + createTask.getException().getMessage());
            });
        });

        new Thread(createTask).start();
    }

    private void markTaskDone(String displayString) {
        Long id = extractId(displayString);
        if (id == null) return;

        // BUG-07 + BUG-11 FIX: Ambil full data dari cache, update status tanpa kehilangan field lain
        Map<String, Object> existingData = taskCache.get(id);
        if (existingData == null) return;

        // Salin semua field existing agar description & dueDate tidak hilang
        Map<String, Object> data = new HashMap<>(existingData);
        data.put("status", "DONE");
        // Hapus field yang tidak dikenali backend dari response object (id, createdAt, updatedAt)
        data.remove("id");
        data.remove("createdAt");
        data.remove("updatedAt");

        Task<Map<String, Object>> updateTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().updateTask(id, data);
            }
        };

        updateTask.setOnSucceeded(e -> Platform.runLater(() -> {
            if (statusLabel != null) statusLabel.setText("✓ Tugas diselesaikan!");
            loadTasks();
        }));

        updateTask.setOnFailed(e -> {
            // BUG-19 FIX: Tampilkan error yang informatif
            Platform.runLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("⚠ Gagal selesaikan: " + updateTask.getException().getMessage());
            });
        });

        new Thread(updateTask).start();
    }

    @FXML
    public void handleDeleteSelected() {
        String selected = activeTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) selected = completedTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // BUG-07 FIX: Ekstrak id dari display string "title|id" atau "✓ title|id"
        String cleanDisplayKey = selected.startsWith("✓ ") ? selected.substring(2) : selected;
        Long id = extractId(cleanDisplayKey);
        if (id == null) return;

        final Long taskId = id;
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().deleteTask(taskId);
                return null;
            }
        };

        deleteTask.setOnSucceeded(e -> Platform.runLater(() -> {
            if (statusLabel != null) statusLabel.setText("🗑 Tugas dihapus.");
            loadTasks();
        }));

        deleteTask.setOnFailed(e -> {
            // BUG-19 FIX: Tampilkan error yang informatif
            Platform.runLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("⚠ Gagal hapus: " + deleteTask.getException().getMessage());
            });
        });

        new Thread(deleteTask).start();
    }

    public void setOnTasksChanged(Runnable callback) {
        this.onTasksChanged = callback;
    }

    public String getFirstActiveTask() {
        if (activeTasks.isEmpty()) return null;
        return extractTitle(activeTasks.get(0));
    }

    public void loadTasks(Runnable callback) {
        loadTasks();
        if (callback != null) {
            new Thread(() -> {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(callback);
            }).start();
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Ekstrak ID dari display string format "judulTugas|123".
     * Format ini menjamin keunikan walau ada 2 tugas dengan judul sama (BUG-07 FIX).
     */
    private Long extractId(String displayString) {
        if (displayString == null) return null;
        int idx = displayString.lastIndexOf("|");
        if (idx < 0 || idx == displayString.length() - 1) return null;
        try {
            return Long.parseLong(displayString.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Ekstrak judul task dari display string format "judulTugas|123".
     * Digunakan oleh CellFactory agar ListView hanya menampilkan judul.
     */
    private String extractTitle(String displayString) {
        if (displayString == null) return "";
        int idx = displayString.lastIndexOf("|");
        return idx >= 0 ? displayString.substring(0, idx) : displayString;
    }
}
