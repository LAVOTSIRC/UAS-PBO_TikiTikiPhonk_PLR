package com.plr.frontend.controller;

import com.plr.frontend.dto.TaskClientDto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.util.Callback;

public class TaskModalController {

    @FXML private Label modalTitleLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> hourComboBox;
    @FXML private ComboBox<String> minuteComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private Button deleteBtn;
    @FXML private Button cancelCompleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private Stage dialogStage;
    private TaskClientDto task;
    private boolean saveClicked = false;
    private boolean deleteClicked = false;
    private boolean cancelCompleteClicked = false;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll("Tidak Ada", "Kerja", "Fokus", "Cepat");
        categoryComboBox.getSelectionModel().selectFirst();

        for (int i = 0; i <= 23; i++) hourComboBox.getItems().add(String.format("%02d", i));
        for (int i = 0; i <= 59; i++) minuteComboBox.getItems().add(String.format("%02d", i));

        // Disable past dates in Calendar UI
        dueDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #1A1722; -fx-text-fill: #7C6E8A; -fx-opacity: 0.5;");
                        }
                    }
                };
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTask(TaskClientDto task, boolean isNew, boolean isCompleted) {
        this.task = task;

        if (isNew) {
            modalTitleLabel.setText("Tambah Tugas Baru");
            if (this.task == null) this.task = new TaskClientDto();
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
            cancelCompleteBtn.setVisible(false);
            cancelCompleteBtn.setManaged(false);
            saveBtn.setVisible(true);
            saveBtn.setManaged(true);
        } else {
            modalTitleLabel.setText("Detail Tugas");
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            if (task.getCategory() != null) {
                switch (task.getCategory()) {
                    case "KERJA": categoryComboBox.getSelectionModel().select("Kerja"); break;
                    case "FOKUS": categoryComboBox.getSelectionModel().select("Fokus"); break;
                    case "CEPAT": categoryComboBox.getSelectionModel().select("Cepat"); break;
                    default: categoryComboBox.getSelectionModel().selectFirst(); break;
                }
            }
            if (task.getDueDate() != null) {
                dueDatePicker.setValue(task.getDueDate().toLocalDate());
                hourComboBox.getSelectionModel().select(String.format("%02d", task.getDueDate().getHour()));
                minuteComboBox.getSelectionModel().select(String.format("%02d", task.getDueDate().getMinute()));
            }

            if (isCompleted) {
                titleField.setEditable(false);
                descriptionArea.setEditable(false);
                categoryComboBox.setDisable(true);
                dueDatePicker.setDisable(true);
                hourComboBox.setDisable(true);
                minuteComboBox.setDisable(true);
                saveBtn.setVisible(false);
                saveBtn.setManaged(false);
                deleteBtn.setVisible(true);
                deleteBtn.setManaged(true);
                cancelCompleteBtn.setVisible(true);
                cancelCompleteBtn.setManaged(true);
            } else {
                deleteBtn.setVisible(true);
                deleteBtn.setManaged(true);
                cancelCompleteBtn.setVisible(false);
                cancelCompleteBtn.setManaged(false);
                saveBtn.setVisible(true);
                saveBtn.setManaged(true);
            }
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public boolean isDeleteClicked() {
        return deleteClicked;
    }

    public boolean isCancelCompleteClicked() {
        return cancelCompleteClicked;
    }

    public TaskClientDto getTask() {
        return task;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            task.setTitle(titleField.getText());
            task.setDescription(descriptionArea.getText());
            
            LocalDate date = dueDatePicker.getValue();
            int hour = Integer.parseInt(hourComboBox.getValue());
            int min = Integer.parseInt(minuteComboBox.getValue());
            task.setDueDate(LocalDateTime.of(date, LocalTime.of(hour, min)));
            
            String selectedCat = categoryComboBox.getSelectionModel().getSelectedItem();
            if ("Kerja".equals(selectedCat)) task.setCategory("KERJA");
            else if ("Fokus".equals(selectedCat)) task.setCategory("FOKUS");
            else if ("Cepat".equals(selectedCat)) task.setCategory("CEPAT");
            else task.setCategory(null);

            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleDelete() {
        deleteClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancelComplete() {
        cancelCompleteClicked = true;
        dialogStage.close();
    }

    private boolean isInputValid() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorLabel.setText("⚠ Judul tugas harus diisi!");
            return false;
        }
        
        if ("Tidak Ada".equals(categoryComboBox.getValue()) || categoryComboBox.getValue() == null) {
            errorLabel.setText("⚠ Kategori harus dipilih!");
            return false;
        }

        if (dueDatePicker.getValue() == null) {
            errorLabel.setText("⚠ Tanggal tenggat waktu harus diisi!");
            return false;
        }

        if (hourComboBox.getValue() == null || minuteComboBox.getValue() == null) {
            errorLabel.setText("⚠ Jam dan menit harus dipilih!");
            return false;
        }

        LocalDate date = dueDatePicker.getValue();
        int hour = Integer.parseInt(hourComboBox.getValue());
        int min = Integer.parseInt(minuteComboBox.getValue());
        LocalDateTime selectedDateTime = LocalDateTime.of(date, LocalTime.of(hour, min));

        if (selectedDateTime.isBefore(LocalDateTime.now())) {
            errorLabel.setText("⚠ Tanggal dan jam tidak boleh berlalu!");
            return false;
        }

        errorLabel.setText("");
        return true;
    }
}
