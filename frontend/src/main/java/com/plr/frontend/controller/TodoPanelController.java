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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;

import java.util.*;
import java.util.stream.Collectors;

public class TodoPanelController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryComboBox;
    @FXML private ComboBox<String> sortTaskComboBox;
    @FXML private ListView<String> activeTasksList;
    @FXML private ListView<String> completedTasksList;
    @FXML private Label sessionCountLabel;

    private final ObservableList<String> activeTasks = FXCollections.observableArrayList();
    private final ObservableList<String> completedTasks = FXCollections.observableArrayList();

    private final Map<Long, TaskClientDto> taskCache = new HashMap<>();

    // Untuk fitur Undo
    private TaskClientDto lastDeletedTask = null;

    @FXML
    public void initialize() {
        activeTasksList.setItems(activeTasks);
        completedTasksList.setItems(completedTasks);

        filterCategoryComboBox.getItems().addAll("Semua Kategori", "Kerja", "Fokus", "Cepat");
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
            Platform.runLater(this::refreshListViews);
        });

        fetchTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showNotification("⚠ Gagal memuat tugas: " + fetchTask.getException().getMessage(), false);
            });
        });

        new Thread(fetchTask).start();
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
                if ("Kerja".equals(filterCat) && "KERJA".equals(t.getCategory())) return true;
                if ("Fokus".equals(filterCat) && "FOKUS".equals(t.getCategory())) return true;
                if ("Cepat".equals(filterCat) && "CEPAT".equals(t.getCategory())) return true;
                return false;
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
        if (sessionCountLabel != null) {
            sessionCountLabel.setText(finalDone + " dari " + taskCache.size() + " selesai");
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

        updateTaskApi(id, data, "✓ Tugas diselesaikan!");
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
                
                Circle check = new Circle(8);
                if (isCompletedList) {
                    check.setFill(Color.web("#C084FC"));
                    check.setStroke(Color.TRANSPARENT);
                } else {
                    check.setFill(Color.TRANSPARENT);
                    check.setStroke(Color.web("#4A4055"));
                    check.setStrokeWidth(1.5);
                }
                
                // Hitbox pada lingkaran
                check.setOnMouseClicked(e -> {
                    e.consume(); // Jangan teruskan klik ke HBox
                    if (taskId != null) {
                        if (isCompletedList) markTaskActive(taskId);
                        else markTaskDone(taskId);
                    }
                });
                
                // Jika baris (HBox) diklik, buka edit modal
                root.setOnMouseClicked(e -> {
                    if (taskId != null) {
                        openEditModal(taskId, isCompletedList);
                    }
                });

                Label text = new Label(displayTitle);
                if (isCompletedList) {
                    text.setStyle("-fx-text-fill: #4A4055; -fx-font-size: 13px; -fx-strikethrough: true;");
                } else {
                    text.setStyle("-fx-text-fill: #D4C8E8; -fx-font-size: 13px;");
                }
                
                root.getChildren().addAll(check, text);
                
                // Tambahkan Tag jika ada
                if (taskId != null) {
                    TaskClientDto dto = taskCache.get(taskId);
                    if (dto != null && dto.getCategory() != null) {
                        Label tagLabel = new Label();
                        tagLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-text-fill: #1A1722; -fx-font-weight: bold;");
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
                        }
                        if (isCompletedList) {
                            tagLabel.setOpacity(0.5);
                        }
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                        root.getChildren().addAll(spacer, tagLabel);
                    }
                }
                
                setGraphic(root);
                setText(null);
            }
        }
    }
}
