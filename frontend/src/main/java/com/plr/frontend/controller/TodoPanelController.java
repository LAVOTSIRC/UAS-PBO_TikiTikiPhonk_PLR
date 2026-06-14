package com.plr.frontend.controller;

import com.plr.frontend.dto.TaskClientDto;
import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TodoPanelController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryComboBox;
    @FXML private ComboBox<String> sortTaskComboBox;
    @FXML private ListView<String> activeTasksList;
    @FXML private ListView<String> completedTasksList;
    @FXML private Label sessionCountLabel;
    @FXML private Label focusMinutesLabel;
    @FXML private Label tasksDoneLabel;

    private final ObservableList<String> activeTasks = FXCollections.observableArrayList();
    private final ObservableList<String> completedTasks = FXCollections.observableArrayList();
    
    private final Map<Long, TaskClientDto> taskCache = new HashMap<>();
    private Runnable onTasksChangedCallback;
    private Consumer<TaskClientDto> focusCallback;

    // Untuk fitur Undo
    private TaskClientDto lastDeletedTask = null;

    // Callback ketika tugas ditandai selesai
    private Consumer<Long> onTaskCompletedCallback;

    @FXML
    public void initialize() {
        activeTasksList.setItems(activeTasks);
        completedTasksList.setItems(completedTasks);
        loadSessionStats();

        filterCategoryComboBox.getItems().addAll("Semua Kategori", "Kerja", "Fokus", "Cepat", "Belajar", "Olahraga", "Meeting", "Baca", "Personal");
        filterCategoryComboBox.getSelectionModel().selectFirst();
        
        sortTaskComboBox.getItems().addAll("Paling Baru", "Tenggat Waktu", "Sesuai Abjad");
        sortTaskComboBox.getSelectionModel().selectFirst();

        filterCategoryComboBox.valueProperty().addListener((obs, oldV, newV) -> refreshListViews());
        sortTaskComboBox.valueProperty().addListener((obs, oldV, newV) -> refreshListViews());
        
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> refreshListViews());
        }

        activeTasksList.setCellFactory(lv -> new TaskListCell(false));
        completedTasksList.setCellFactory(lv -> new TaskListCell(true));

        // Bersihkan seleksi di list sebelah agar hanya 1 tugas yang bisa dipilih (UX fix)
        activeTasksList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                completedTasksList.getSelectionModel().clearSelection();
                Long taskId = extractId(newVal);
                if (taskId != null && focusCallback != null) {
                    TaskClientDto dto = taskCache.get(taskId);
                    if (dto != null) {
                        focusCallback.accept(dto);
                    }
                }
            }
        });

        activeTasksList.setOnMouseClicked(e -> {
            String selected = activeTasksList.getSelectionModel().getSelectedItem();
            if (selected != null && e.getClickCount() == 1) {
                Long taskId = extractId(selected);
                if (taskId != null && focusCallback != null) {
                    TaskClientDto dto = taskCache.get(taskId);
                    if (dto != null) {
                        focusCallback.accept(dto);
                    }
                }
            }
        });
        
        completedTasksList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeTasksList.getSelectionModel().clearSelection();
            }
        });
    }

    public void loadTasks() {
        Task<List<TaskClientDto>> fetchTask = new Task<>() {
            @Override
            protected List<TaskClientDto> call() throws Exception {
                return ApiClient.getInstance().getTasks();
            }
        };

        fetchTask.setOnSucceeded(e -> {
            List<TaskClientDto> tasks = fetchTask.getValue();
            taskCache.clear();
            for (TaskClientDto t : tasks) {
                taskCache.put(t.getId(), t);
            }
            Platform.runLater(() -> {
                refreshListViews();
                if (tasksDoneLabel != null) {
                    long doneCount = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
                    tasksDoneLabel.setText(doneCount + " tugas");
                }
                if (onTasksChangedCallback != null) onTasksChangedCallback.run();
            });
        });

        fetchTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showNotification("⚠ Gagal memuat tugas: " + fetchTask.getException().getMessage(), false);
            });
        });

        new Thread(fetchTask).start();
        loadSessionStats();
    }

    private void refreshListViews() {
        String searchText = searchField != null && searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String filterCat = filterCategoryComboBox.getValue();
        String sortMode = sortTaskComboBox.getValue();

        List<TaskClientDto> filtered = taskCache.values().stream()
            .filter(t -> t.getTitle().toLowerCase().contains(searchText))
            .filter(t -> {
                if ("Semua Kategori".equals(filterCat)) return true;
                if (t.getCategory() == null) return false;
                return filterCat.toUpperCase().equals(t.getCategory());
            })
            .collect(Collectors.toList());

        // Sortir
        if ("Sesuai Abjad".equals(sortMode)) {
            filtered.sort(Comparator.comparing(TaskClientDto::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else if ("Tenggat Waktu".equals(sortMode)) {
            filtered.sort((a, b) -> {
                if (a.getDueDate() == null && b.getDueDate() == null) return 0;
                if (a.getDueDate() == null) return 1;
                if (b.getDueDate() == null) return -1;
                return a.getDueDate().compareTo(b.getDueDate());
            });
        } else { // Paling Baru (Berdasarkan ID turun)
            filtered.sort((a, b) -> Long.compare(b.getId(), a.getId()));
        }

        activeTasks.clear();
        completedTasks.clear();
        int doneCount = 0;

        for (TaskClientDto t : filtered) {
            String displayKey = t.getTitle() + "|" + t.getId();
            if ("DONE".equals(t.getStatus())) {
                completedTasks.add("✓ " + displayKey);
                doneCount++;
            } else {
                activeTasks.add(displayKey);
            }
        }

        int finalDone = doneCount;
        if (tasksDoneLabel != null) {
            tasksDoneLabel.setText(finalDone + " tugas selesai");
        }
    }

    @FXML
    public void handleOpenTaskModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskModal.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Tambah Tugas");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);
            
            // Mengambil stage parent dengan cara yang lebih aman
            if (activeTasksList != null && activeTasksList.getScene() != null) {
                dialogStage.initOwner(activeTasksList.getScene().getWindow());
            }

            TaskModalController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            TaskClientDto newTask = new TaskClientDto();
            newTask.setStatus("TODO");
            controller.setTask(newTask, true, false);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            com.plr.frontend.util.ThemeManager.getInstance().applyToScene(scene);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                TaskClientDto data = controller.getTask();
                Task<TaskClientDto> createTask = new Task<>() {
                    @Override
                    protected TaskClientDto call() throws Exception {
                        return ApiClient.getInstance().createTask(data);
                    }
                };

                createTask.setOnSucceeded(e -> Platform.runLater(() -> {
                    showNotification("✅ Tugas ditambah!", false);
                    loadTasks();
                }));

                createTask.setOnFailed(e -> Platform.runLater(() -> {
                    Throwable ex = createTask.getException();
                    System.err.println("Gagal membuat tugas: " + ex.getMessage());
                    showNotification("❌ Gagal menyimpan tugas!", true);
                }));

                new Thread(createTask).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditModal(Long id, boolean isCompleted) {
        TaskClientDto existing = taskCache.get(id);
        if (existing == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskModal.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detail Tugas");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(activeTasksList.getScene().getWindow());
            dialogStage.setResizable(false);

            TaskModalController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            // Kirim copy data
            TaskClientDto copy = new TaskClientDto();
            copy.setId(existing.getId());
            copy.setTitle(existing.getTitle());
            copy.setDescription(existing.getDescription());
            copy.setCategory(existing.getCategory());
            copy.setDueDate(existing.getDueDate());
            copy.setStatus(existing.getStatus());
            
            controller.setTask(copy, false, isCompleted);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            com.plr.frontend.util.ThemeManager.getInstance().applyToScene(scene);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                TaskClientDto data = controller.getTask();
                updateTaskApi(id, data, "✅ Tugas diperbarui!");
            } else if (controller.isDeleteClicked()) {
                deleteTaskApi(id);
            } else if (controller.isCancelCompleteClicked()) {
                TaskClientDto data = controller.getTask();
                data.setStatus("TODO");
                updateTaskApi(id, data, "✅ Tugas dikembalikan ke aktif!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markTaskDone(Long id) {
        TaskClientDto existing = taskCache.get(id);
        if (existing == null) return;

        TaskClientDto data = new TaskClientDto();
        data.setTitle(existing.getTitle());
        data.setDescription(existing.getDescription());
        data.setCategory(existing.getCategory());
        data.setDueDate(existing.getDueDate());
        data.setStatus("DONE");

        Task<TaskClientDto> updateTask = new Task<>() {
            @Override
            protected TaskClientDto call() throws Exception {
                return ApiClient.getInstance().updateTask(id, data);
            }
        };

        updateTask.setOnSucceeded(e -> Platform.runLater(() -> {
            showNotification("✓ Tugas diselesaikan!", false);
            loadTasks();
            if (onTaskCompletedCallback != null) {
                onTaskCompletedCallback.accept(id);
            }
        }));

        updateTask.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = updateTask.getException();
            System.err.println("Gagal menyelesaikan tugas: " + ex.getMessage());
            showNotification("❌ Gagal menyelesaikan tugas!", false);
        }));

        new Thread(updateTask).start();
    }
    
    private void markTaskActive(Long id) {
        TaskClientDto existing = taskCache.get(id);
        if (existing == null) return;

        TaskClientDto data = new TaskClientDto();
        data.setTitle(existing.getTitle());
        data.setDescription(existing.getDescription());
        data.setCategory(existing.getCategory());
        data.setDueDate(existing.getDueDate());
        data.setStatus("TODO");

        updateTaskApi(id, data, "✓ Tugas dikembalikan!");
    }

    private void updateTaskApi(Long id, TaskClientDto data, String successMsg) {
        Task<TaskClientDto> updateTask = new Task<>() {
            @Override
            protected TaskClientDto call() throws Exception {
                return ApiClient.getInstance().updateTask(id, data);
            }
        };

        updateTask.setOnSucceeded(e -> Platform.runLater(() -> {
            showNotification(successMsg, false);
            loadTasks();
        }));

        new Thread(updateTask).start();
    }

    @FXML
    public void handleDeleteSelected() {
        String selected = activeTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) selected = completedTasksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String cleanDisplayKey = selected.startsWith("✓ ") ? selected.substring(2) : selected;
        Long id = extractId(cleanDisplayKey);
        if (id != null) {
            deleteTaskApi(id);
        }
    }

    private void deleteTaskApi(Long id) {
        TaskClientDto toDelete = taskCache.get(id);
        if (toDelete != null) {
            lastDeletedTask = toDelete; // Simpan untuk Undo
        }

        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().deleteTask(id);
                return null;
            }
        };

        deleteTask.setOnSucceeded(e -> Platform.runLater(() -> {
            showNotification("🗑 Dihapus. [Klik Disini untuk Undo]", true);
            loadTasks();
        }));

        new Thread(deleteTask).start();
    }

    private void undoDelete() {
        if (lastDeletedTask == null) return;
        
        TaskClientDto data = new TaskClientDto();
        data.setTitle(lastDeletedTask.getTitle());
        data.setDescription(lastDeletedTask.getDescription());
        data.setCategory(lastDeletedTask.getCategory());
        data.setDueDate(lastDeletedTask.getDueDate());
        data.setStatus(lastDeletedTask.getStatus());

        Task<TaskClientDto> recreateTask = new Task<>() {
            @Override
            protected TaskClientDto call() throws Exception {
                return ApiClient.getInstance().createTask(data);
            }
        };

        recreateTask.setOnSucceeded(e -> Platform.runLater(() -> {
            showNotification("✅ Penghapusan dibatalkan!", false);
            lastDeletedTask = null;
            loadTasks();
        }));

        new Thread(recreateTask).start();
    }

    private void showNotification(String text, boolean isUndoOption) {
        Runnable undoAction = isUndoOption ? this::undoDelete : null;
        MainLayoutController.showGlobalNotification(text, isUndoOption, undoAction);
    }

    public void setOnTasksChanged(Runnable callback) {
        this.onTasksChangedCallback = callback;
    }

    public void setOnFocusTaskRequested(Consumer<TaskClientDto> callback) {
        this.focusCallback = callback;
    }

    public void setOnTaskCompleted(Consumer<Long> callback) {
        this.onTaskCompletedCallback = callback;
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

    public void loadSessionStats() {
        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getPomodoroSessions();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Map<String, Object>> sessions = loadTask.getValue();
            Platform.runLater(() -> {
                long totalSessions = sessions.size();
                long focusCount = sessions.stream()
                    .filter(s -> "FOCUS".equals(s.get("sessionType")))
                    .count();
                int totalMinutes = sessions.stream()
                    .mapToInt(s -> {
                        Object dur = s.get("durationMinutes");
                        return dur instanceof Number ? ((Number) dur).intValue() : 0;
                    })
                    .sum();

                if (sessionCountLabel != null) {
                    sessionCountLabel.setText(focusCount + " fokus");
                }
                if (focusMinutesLabel != null) {
                    focusMinutesLabel.setText(totalMinutes + " mnt");
                }
            });
        });

        loadTask.setOnFailed(e -> {});
        new Thread(loadTask).start();
    }

    // ========== HELPER METHODS ==========

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

    private String extractTitle(String displayString) {
        if (displayString == null) return "";
        int idx = displayString.lastIndexOf("|");
        return idx >= 0 ? displayString.substring(0, idx) : displayString;
    }

    // Custom Cell Factory class
    private class TaskListCell extends ListCell<String> {
        private final boolean isCompletedList;
        
        public TaskListCell(boolean isCompletedList) {
            this.isCompletedList = isCompletedList;
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String cleanItem = isCompletedList && item.startsWith("✓ ") ? item.substring(2) : item;
                String displayTitle = extractTitle(cleanItem);
                Long taskId = extractId(cleanItem);

                HBox root = new HBox(8);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setStyle("-fx-cursor: hand;");
                root.getStyleClass().add("todo-cell");
                
                Button check = new Button();
                check.getStyleClass().addAll("todo-check-box", "todo-check-btn");
                if (isCompletedList) {
                    check.setText("❎");
                    check.getStyleClass().add("todo-checked");
                }

                check.setOnAction(e -> {
                    e.consume();
                    if (taskId != null) {
                        if (isCompletedList) {
                            check.setText("");
                            check.getStyleClass().remove("todo-checked");
                            check.setStyle("");
                            markTaskActive(taskId);
                        } else {
                            check.setText("❎");
                            check.getStyleClass().add("todo-checked");
                            check.setStyle("-fx-background-color: #C084FC; -fx-border-color: #C084FC; -fx-text-fill: white;");
                            markTaskDone(taskId);
                        }
                    }
                });

                if (!isCompletedList) {
                    check.setOnMouseEntered(e -> {
                        if (!check.getStyleClass().contains("todo-checked")) {
                            check.setText("✅");
                            check.setStyle("-fx-border-color: #22C55E; -fx-text-fill: #22C55E;");
                        }
                    });
                    check.setOnMouseExited(e -> {
                        if (!check.getStyleClass().contains("todo-checked")) {
                            check.setText("");
                            check.setStyle("");
                        }
                    });
                    Tooltip.install(check, new Tooltip("Selesai?"));
                }
                
                // Event handling untuk klik tugas sudah dipindahkan ke activeTasksList (List level)
                // agar klik di bagian kosong row tetap terdeteksi dengan baik.

                Label text = new Label(displayTitle);
                text.getStyleClass().add("todo-text");
                if (isCompletedList) {
                    text.getStyleClass().add("todo-text-done");
                }
                // Mencegah judul tugas yang panjang mendorong elemen lain keluar batas (horizontal scroll)
                text.setMinWidth(0);
                text.setMaxWidth(Double.MAX_VALUE);
                text.setWrapText(false);
                
                // text diizinkan mengecil ketika tidak ada ruang
                HBox.setHgrow(text, javafx.scene.layout.Priority.SOMETIMES);
                
                root.getChildren().addAll(check, text);
                
                // Tambahkan Spacer (selalu)
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                root.getChildren().add(spacer);
                
                // Mengikat ukuran root agar tidak pernah melebihi lebar list view (mencegah scrollbar horizontal)
                if (getListView() != null) {
                    root.maxWidthProperty().bind(getListView().widthProperty().subtract(15));
                }

                // Tombol detail (📋) — selalu tampak, buka popup edit
                if (taskId != null) {
                    javafx.scene.control.Button detailBtn = new javafx.scene.control.Button("📋");
                    detailBtn.getStyleClass().addAll("focus-btn", "detail-btn");
                    detailBtn.setMinWidth(Region.USE_PREF_SIZE); // Jangan sampai icon tersembunyi
                    detailBtn.setOnMouseClicked(e -> {
                        e.consume();
                        openEditModal(taskId, isCompletedList);
                    });
                    root.getChildren().add(detailBtn);
                }

                // Tambahkan Tag jika ada
                if (taskId != null) {
                    TaskClientDto dto = taskCache.get(taskId);
                    if (dto != null && dto.getCategory() != null) {
                        Label tagLabel = new Label();
                        tagLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-text-fill: #1A1722; -fx-font-weight: bold;");
                        tagLabel.setMinWidth(Region.USE_PREF_SIZE); // Jangan disembunyikan/dikecilkan
                        switch (dto.getCategory()) {
                            case "KERJA": 
                                tagLabel.setText("Kerja");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #C084FC;");
                                break;
                            case "FOKUS": 
                                tagLabel.setText("Fokus");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #84FCB0;");
                                break;
                            case "CEPAT": 
                                tagLabel.setText("Cepat");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #FCA984;");
                                break;
                            case "BELAJAR":
                                tagLabel.setText("Belajar");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #818CF8;");
                                break;
                            case "OLAHRAGA":
                                tagLabel.setText("Olahraga");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #FBBF24;");
                                break;
                            case "MEETING":
                                tagLabel.setText("Meeting");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #F472B6;");
                                break;
                            case "BACA":
                                tagLabel.setText("Baca");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #2DD4BF;");
                                break;
                            case "PERSONAL":
                                tagLabel.setText("Personal");
                                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #A78BFA;");
                                break;
                        }
                        if (isCompletedList) {
                            tagLabel.setOpacity(0.5);
                        }
                        root.getChildren().add(tagLabel);
                    }
                }
                
                setGraphic(root);
                setText(null);
            }
        }
    }
}
