package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class ProfilePanelController {

    @FXML private Circle userAvatarCircle;
    @FXML private Label avatarInitialLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label joinedDateLabel;
    @FXML private Button editProfileBtn;

    @FXML private Label totalSessionsLabel;
    @FXML private Label totalFocusMinutesLabel;
    @FXML private Label tasksCompletedLabel;
    @FXML private Label currentStreakLabel;

    @FXML private TextField editUsernameField;
    @FXML private TextField editEmailField;
    @FXML private Label usernameDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Button editPersonalInfoBtn;
    @FXML private Button savePersonalInfoBtn;
    @FXML private Button cancelPersonalInfoBtn;
    @FXML private HBox personalInfoButtonsBox;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Button changePasswordBtn;
    @FXML private Button savePasswordBtn;
    @FXML private Button cancelPasswordBtn;
    @FXML private VBox passwordInputsBox;
    @FXML private HBox passwordButtonsBox;

    @FXML private Button deleteAccountBtn;

    private String currentUsername;
    private String currentEmail;
    private boolean isEditingPersonalInfo = false;
    private boolean isChangingPassword = false;

    private Runnable onLogoutCallback;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadUserStatistics();
        setupListeners();
    }

    public void loadUserProfile() {
        new Thread(() -> {
            try {
                Map<String, Object> profile = ApiClient.getInstance().getUserProfile();
                Platform.runLater(() -> displayUserProfile(profile));
            } catch (Exception e) {
                System.err.println("Gagal memuat profil: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void displayUserProfile(Map<String, Object> profile) {
        currentUsername = (String) profile.getOrDefault("username", "");
        currentEmail = (String) profile.getOrDefault("email", "");

        usernameLabel.setText(currentUsername);
        emailLabel.setText(currentEmail);
        usernameDisplayLabel.setText(currentUsername);
        emailDisplayLabel.setText(currentEmail);

        if (profile.containsKey("joinedDate") && profile.get("joinedDate") != null) {
            String dateStr = (String) profile.get("joinedDate");
            try {
                LocalDateTime joinedDate = LocalDateTime.parse(dateStr);
                joinedDateLabel.setText("Bergabung " + formatDate(joinedDate));
            } catch (Exception e) {
                joinedDateLabel.setText("Bergabung " + dateStr);
            }
        }

        setAvatarInitial(currentUsername);
    }

    public void loadUserStatistics() {
        new Thread(() -> {
            try {
                Map<String, Object> stats = ApiClient.getInstance().getStats();
                Platform.runLater(() -> displayStatistics(stats));
            } catch (Exception e) {
                System.err.println("Gagal memuat statistik: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void displayStatistics(Map<String, Object> stats) {
        totalSessionsLabel.setText(String.valueOf(stats.getOrDefault("totalSessions", 0)));
        int totalMinutes = ((Number) stats.getOrDefault("totalFocusMinutes", 0)).intValue();
        totalFocusMinutesLabel.setText(totalMinutes + " menit");
        tasksCompletedLabel.setText(String.valueOf(stats.getOrDefault("completedTasks", 0)));
        int streak = ((Number) stats.getOrDefault("currentStreak", 0)).intValue();
        currentStreakLabel.setText(String.valueOf(streak));
    }

    private void setupListeners() {
        oldPasswordField.textProperty().addListener((obs, old, val) ->
            passwordErrorLabel.setVisible(false));
        newPasswordField.textProperty().addListener((obs, old, val) ->
            passwordErrorLabel.setVisible(false));
    }

    @FXML
    private void handleEditProfile() {
        isEditingPersonalInfo = !isEditingPersonalInfo;
        if (isEditingPersonalInfo) {
            editUsernameField.setText(currentUsername);
            editEmailField.setText(currentEmail);
            editUsernameField.setVisible(true);
            editEmailField.setVisible(true);
            usernameDisplayLabel.setVisible(false);
            emailDisplayLabel.setVisible(false);
            editPersonalInfoBtn.setVisible(false);
            savePersonalInfoBtn.setVisible(true);
            cancelPersonalInfoBtn.setVisible(true);
        }
    }

    @FXML
    private void handleEditPersonalInfo() {
        isEditingPersonalInfo = true;
        editUsernameField.setText(currentUsername);
        editEmailField.setText(currentEmail);
        editUsernameField.setVisible(true);
        editEmailField.setVisible(true);
        usernameDisplayLabel.setVisible(false);
        emailDisplayLabel.setVisible(false);
        editPersonalInfoBtn.setVisible(false);
        savePersonalInfoBtn.setVisible(true);
        cancelPersonalInfoBtn.setVisible(true);
    }

    @FXML
    private void handleSavePersonalInfo() {
        String newUsername = editUsernameField.getText().trim();
        String newEmail = editEmailField.getText().trim();

        if (newUsername.isEmpty() || newUsername.length() < 3) {
            showError("Username minimal 3 karakter");
            return;
        }
        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            showError("Email tidak valid");
            return;
        }

        new Thread(() -> {
            try {
                ApiClient.getInstance().updateUserProfile(newUsername, newEmail);
                currentUsername = newUsername;
                currentEmail = newEmail;
                Platform.runLater(() -> {
                    cancelPersonalInfoEdit();
                    usernameLabel.setText(newUsername);
                    emailLabel.setText(newEmail);
                    setAvatarInitial(newUsername);
                    showSuccess("Profil berhasil diperbarui");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Gagal memperbarui profil: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCancelPersonalInfo() {
        cancelPersonalInfoEdit();
    }

    private void cancelPersonalInfoEdit() {
        isEditingPersonalInfo = false;
        editUsernameField.setVisible(false);
        editEmailField.setVisible(false);
        usernameDisplayLabel.setText(currentUsername);
        emailDisplayLabel.setText(currentEmail);
        usernameDisplayLabel.setVisible(true);
        emailDisplayLabel.setVisible(true);
        editPersonalInfoBtn.setVisible(true);
        savePersonalInfoBtn.setVisible(false);
        cancelPersonalInfoBtn.setVisible(false);
    }

    @FXML
    private void handleChangePasswordClick() {
        isChangingPassword = true;
        passwordInputsBox.setVisible(true);
        changePasswordBtn.setVisible(false);
        savePasswordBtn.setVisible(true);
        cancelPasswordBtn.setVisible(true);
    }

    @FXML
    private void handleSavePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty()) {
            showPasswordError("Password lama tidak boleh kosong");
            return;
        }
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            showPasswordError("Password baru minimal 6 karakter");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("Password baru dan konfirmasi tidak cocok");
            return;
        }

        String oldPwd = oldPassword;
        String newPwd = newPassword;
        new Thread(() -> {
            try {
                ApiClient.getInstance().changePassword(oldPwd, newPwd);
                Platform.runLater(() -> {
                    cancelPasswordChange();
                    showSuccess("Password berhasil diubah");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showPasswordError(e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCancelPassword() {
        cancelPasswordChange();
    }

    private void cancelPasswordChange() {
        isChangingPassword = false;
        passwordInputsBox.setVisible(false);
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordErrorLabel.setVisible(false);
        changePasswordBtn.setVisible(true);
        savePasswordBtn.setVisible(false);
        cancelPasswordBtn.setVisible(false);
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Hapus Akun");
        alert.setHeaderText("Apakah Anda yakin ingin menghapus akun?");
        alert.setContentText("Tindakan ini tidak dapat dibatalkan. Semua data Anda akan dihapus permanen.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Konfirmasi Penghapusan");
            dialog.setHeaderText("Masukkan password Anda untuk mengkonfirmasi penghapusan akun");
            dialog.setContentText("Password:");

            Optional<String> password = dialog.showAndWait();
            if (password.isPresent()) {
                deleteAccountWithPassword(password.get());
            }
        }
    }

    private void deleteAccountWithPassword(String password) {
        String pwd = password;
        new Thread(() -> {
            try {
                ApiClient.getInstance().deleteAccount(pwd);
                Platform.runLater(() -> {
                    showSuccess("Akun berhasil dihapus. Anda akan dikembalikan ke login page...");
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        if (onLogoutCallback != null) {
                            onLogoutCallback.run();
                        }
                    });
                    pause.play();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Gagal menghapus akun: " + e.getMessage()));
            }
        }).start();
    }

    private void setAvatarInitial(String username) {
        if (username != null && !username.isEmpty()) {
            userAvatarCircle.setFill(Color.web("#C084FC"));
            avatarInitialLabel.setText(username.substring(0, 1).toUpperCase());
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private void showSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showPasswordError(String message) {
        Platform.runLater(() -> {
            passwordErrorLabel.setText(message);
            passwordErrorLabel.setVisible(true);
        });
    }

    public void setOnLogoutCallback(Runnable callback) {
        this.onLogoutCallback = callback;
    }
}
