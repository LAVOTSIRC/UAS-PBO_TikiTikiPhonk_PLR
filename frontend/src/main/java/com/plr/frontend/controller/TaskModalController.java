package com.plr.frontend.controller;

import com.plr.frontend.dto.TaskClientDto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TaskModalController {

    @FXML private Label modalTitleLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
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
        categoryComboBox.getItems().addAll("Tidak Ada", "Kerja", "Fokus", "Cepat", "Belajar", "Olahraga", "Meeting", "Baca", "Personal");
        categoryComboBox.getSelectionModel().selectFirst();

        dueDatePicker.setEditable(false);
        dueDatePicker.setPromptText("Pilih tanggal");
        dueDatePicker.setValue(LocalDate.now());

        // Spinner jam — bisa diketik, format 2 digit
        SpinnerValueFactory.IntegerSpinnerValueFactory hourFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalTime.now().getHour());
        hourSpinner.setValueFactory(hourFactory);
        hourSpinner.setEditable(true);
        hourSpinner.getValueFactory().setConverter(new StringConverter<Integer>() {
            @Override public String toString(Integer value) {
                return value == null ? "00" : String.format("%02d", value);
            }
            @Override public Integer fromString(String string) {
                try {
                    int val = Integer.parseInt(string);
                    return Math.max(0, Math.min(23, val));
                } catch (NumberFormatException e) {
                    return hourSpinner.getValue();
                }
            }
        });

        // Spinner menit — bisa diketik, format 2 digit
        SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalTime.now().getMinute());
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);
        minuteSpinner.getValueFactory().setConverter(new StringConverter<Integer>() {
            @Override public String toString(Integer value) {
                return value == null ? "00" : String.format("%02d", value);
            }
            @Override public Integer fromString(String string) {
                try {
                    int val = Integer.parseInt(string);
                    return Math.max(0, Math.min(59, val));
                } catch (NumberFormatException e) {
                    return minuteSpinner.getValue();
                }
            }
        });

        // Disable past dates in Calendar UI (styling via CSS)
        dueDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
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
                    case "BELAJAR": categoryComboBox.getSelectionModel().select("Belajar"); break;
                    case "OLAHRAGA": categoryComboBox.getSelectionModel().select("Olahraga"); break;
                    case "MEETING": categoryComboBox.getSelectionModel().select("Meeting"); break;
                    case "BACA": categoryComboBox.getSelectionModel().select("Baca"); break;
                    case "PERSONAL": categoryComboBox.getSelectionModel().select("Personal"); break;
                    default: categoryComboBox.getSelectionModel().selectFirst(); break;
                }
            }
            if (task.getDueDate() != null) {
                dueDatePicker.setValue(task.getDueDate().toLocalDate());
                hourSpinner.getValueFactory().setValue(task.getDueDate().getHour());
                minuteSpinner.getValueFactory().setValue(task.getDueDate().getMinute());
            }

            if (isCompleted) {
                titleField.setEditable(false);
                descriptionArea.setEditable(false);
                categoryComboBox.setDisable(true);
                dueDatePicker.setDisable(true);
                hourSpinner.setDisable(true);
                minuteSpinner.setDisable(true);
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
            int hour = hourSpinner.getValue();
            int min = minuteSpinner.getValue();
            task.setDueDate(LocalDateTime.of(date, LocalTime.of(hour, min)));

            String selectedCat = categoryComboBox.getSelectionModel().getSelectedItem();
            switch (selectedCat) {
                case "Kerja": task.setCategory("KERJA"); break;
                case "Fokus": task.setCategory("FOKUS"); break;
                case "Cepat": task.setCategory("CEPAT"); break;
                case "Belajar": task.setCategory("BELAJAR"); break;
                case "Olahraga": task.setCategory("OLAHRAGA"); break;
                case "Meeting": task.setCategory("MEETING"); break;
                case "Baca": task.setCategory("BACA"); break;
                case "Personal": task.setCategory("PERSONAL"); break;
                default: task.setCategory(null); break;
            }

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
            errorLabel.setText("Judul tugas harus diisi!");
            return false;
        }

        if ("Tidak Ada".equals(categoryComboBox.getValue()) || categoryComboBox.getValue() == null) {
            errorLabel.setText("Kategori harus dipilih!");
            return false;
        }

        if (dueDatePicker.getValue() == null) {
            errorLabel.setText("Tanggal tenggat waktu harus diisi!");
            return false;
        }

        if (hourSpinner.getValue() == null || minuteSpinner.getValue() == null) {
            errorLabel.setText("Jam dan menit harus dipilih!");
            return false;
        }

        LocalDate date = dueDatePicker.getValue();
        int hour = hourSpinner.getValue();
        int min = minuteSpinner.getValue();
        LocalDateTime selectedDateTime = LocalDateTime.of(date, LocalTime.of(hour, min));

        if (selectedDateTime.isBefore(LocalDateTime.now())) {
            errorLabel.setText("Tanggal dan jam tidak boleh berlalu!");
            return false;
        }

        errorLabel.setText("");
        return true;
    }
}
