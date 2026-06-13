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
import java.util.Optional;

/**
 * Controller untuk ProfilePanel.fxml
 * Mengelola tampilan profil user, statistik, edit profil, dan pengaturan akun
 */
public class ProfilePanelController {

    @FXML private Circle userAvatarCircle;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label joinedDateLabel;
    @FXML private Button editProfileBtn;

    // Statistics labels
    @FXML private Label totalSessionsLabel;
    @FXML private Label totalFocusMinutesLabel;
    @FXML private Label tasksCompletedLabel;
    @FXML private Label currentStreakLabel;

    // Personal Info Edit
    @FXML private TextField editUsernameField;
    @FXML private TextField editEmailField;
    @FXML private Label usernameDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Button editPersonalInfoBtn;
    @FXML private Button savePersonalInfoBtn;
    @FXML private Button cancelPersonalInfoBtn;
    @FXML private HBox personalInfoButtonsBox;

    // Password Change
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Button changePasswordBtn;
    @FXML private Button savePasswordBtn;
    @FXML private Button cancelPasswordBtn;
    @FXML private VBox passwordInputsBox;
    @FXML private HBox passwordButtonsBox;

    // Danger Zone
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

    /**
     * Load user profile data dari backend
     */
    private void loadUserProfile() {
        javafx.concurrent.Task<UserProfileData> task = new javafx.concurrent.Task<UserProfileData>() {
            @Override
            protected UserProfileData call() throws Exception {
                // API call untuk mendapatkan profil user
                // GET /api/users/profile
                String response = ApiClient.get("/api/users/profile");
                // Parse response JSON
                return parseUserProfile(response);
            }

            @Override
            protected void succeeded() {
                UserProfileData profile = getValue();
                if (profile != null) {
                    displayUserProfile(profile);
                }
            }

            @Override
            protected void failed() {
                showError("Gagal memuat profil pengguna");
            }
        };
        new Thread(task).start();
    }

    /**
     * Display user profile di UI
     */
    private void displayUserProfile(UserProfileData profile) {
        Platform.runLater(() -> {
            currentUsername = profile.username;
            currentEmail = profile.email;

            usernameLabel.setText(profile.username);
            emailLabel.setText(profile.email);
            usernameDisplayLabel.setText(profile.username);
            emailDisplayLabel.setText(profile.email);

            // Format joined date
            if (profile.joinedDate != null) {
                joinedDateLabel.setText("Bergabung " + formatDate(profile.joinedDate));
            }

            // Set avatar dengan initial user
            setAvatarInitial(profile.username);
        });
    }

    /**
     * Load user statistics dari backend
     */
    private void loadUserStatistics() {
        javafx.concurrent.Task<UserStatistics> task = new javafx.concurrent.Task<UserStatistics>() {
            @Override
            protected UserStatistics call() throws Exception {
                // API call untuk mendapatkan statistik user
                // GET /api/users/stats
                String response = ApiClient.get("/api/users/stats");
                return parseUserStatistics(response);
            }

            @Override
            protected void succeeded() {
                UserStatistics stats = getValue();
                if (stats != null) {
                    displayStatistics(stats);
                }
            }

            @Override
            protected void failed() {
                showError("Gagal memuat statistik pengguna");
            }
        };
        new Thread(task).start();
    }

    /**
     * Display statistics di UI
     */
    private void displayStatistics(UserStatistics stats) {
        Platform.runLater(() -> {
            totalSessionsLabel.setText(String.valueOf(stats.totalSessions));
            totalFocusMinutesLabel.setText(stats.totalFocusMinutes + " menit");
            tasksCompletedLabel.setText(String.valueOf(stats.tasksCompleted));
            currentStreakLabel.setText(stats.currentStreak);
        });
    }

    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Clear error message saat user mulai mengetik password
        oldPasswordField.textProperty().addListener((obs, old, val) -> 
            passwordErrorLabel.setVisible(false));
        newPasswordField.textProperty().addListener((obs, old, val) -> 
            passwordErrorLabel.setVisible(false));
    }

    /**
     * Handle edit profile button click
     */
    @FXML
    private void handleEditProfile() {
        isEditingPersonalInfo = !isEditingPersonalInfo;

        if (isEditingPersonalInfo) {
            // Switch to edit mode
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

    /**
     * Handle edit personal info button
     */
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

    /**
     * Handle save personal info
     */
    @FXML
    private void handleSavePersonalInfo() {
        String newUsername = editUsernameField.getText().trim();
        String newEmail = editEmailField.getText().trim();

        // Validation
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            showError("Username minimal 3 karakter");
            return;
        }

        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            showError("Email tidak valid");
            return;
        }

        // API call untuk update profile
        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // PUT /api/users/profile
                String payload = String.format("{\"username\":\"%s\",\"email\":\"%s\"}", 
                    newUsername, newEmail);
                String response = ApiClient.put("/api/users/profile", payload);
                return response.contains("success");
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    currentUsername = newUsername;
                    currentEmail = newEmail;
                    cancelPersonalInfoEdit();
                    showSuccess("Profil berhasil diperbarui");
                } else {
                    showError("Gagal memperbarui profil");
                }
            }

            @Override
            protected void failed() {
                showError("Gagal memperbarui profil: " + getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    /**
     * Handle cancel personal info edit
     */
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

    /**
     * Handle change password click
     */
    @FXML
    private void handleChangePasswordClick() {
        isChangingPassword = true;
        passwordInputsBox.setVisible(true);
        changePasswordBtn.setVisible(false);
        savePasswordBtn.setVisible(true);
        cancelPasswordBtn.setVisible(true);
    }

    /**
     * Handle save password
     */
    @FXML
    private void handleSavePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
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

        // API call untuk change password
        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // PUT /api/users/change-password
                String payload = String.format(
                    "{\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}", 
                    oldPassword, newPassword);
                String response = ApiClient.put("/api/users/change-password", payload);
                return response.contains("success");
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    cancelPasswordChange();
                    showSuccess("Password berhasil diubah");
                } else {
                    showPasswordError("Password lama tidak cocok");
                }
            }

            @Override
            protected void failed() {
                showPasswordError("Gagal mengubah password: " + getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    /**
     * Handle cancel password change
     */
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

    /**
     * Handle delete account
     */
    @FXML
    private void handleDeleteAccount() {
        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Hapus Akun");
        alert.setHeaderText("Apakah Anda yakin ingin menghapus akun?");
        alert.setContentText("Tindakan ini tidak dapat dibatalkan. Semua data Anda akan dihapus permanen.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Ask for password confirmation
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
        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // DELETE /api/users/delete
                String payload = String.format("{\"password\":\"%s\"}", password);
                String response = ApiClient.delete("/api/users/delete", payload);
                return response.contains("success");
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    showSuccess("Akun berhasil dihapus. Anda akan dikembalikan ke login page...");
                    // After 2 seconds, redirect to login
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        if (onLogoutCallback != null) {
                            onLogoutCallback.run();
                        }
                    });
                    pause.play();
                } else {
                    showError("Password tidak cocok");
                }
            }

            @Override
            protected void failed() {
                showError("Gagal menghapus akun: " + getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    /**
     * Utility methods
     */
    private void setAvatarInitial(String username) {
        if (username != null && !username.isEmpty()) {
            // Get first character
            char initial = Character.toUpperCase(username.charAt(0));
            
            // Create label dengan initial
            Label label = new Label(String.valueOf(initial));
            label.setStyle("-fx-font-size: 28; -fx-text-fill: #0E0C10; -fx-font-weight: bold;");
            
            userAvatarCircle.setFill(Color.web("#C084FC"));
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            showNotification(message, "success");
        });
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

    private void showNotification(String message, String type) {
        // Show notification via MainLayoutController
        System.out.println("[" + type.toUpperCase() + "] " + message);
    }

    // Callback to notify logout
    public void setOnLogoutCallback(Runnable callback) {
        this.onLogoutCallback = callback;
    }

    /**
     * Parse user profile dari JSON response
     */
    private UserProfileData parseUserProfile(String json) {
        try {
            // Simple JSON parsing (in production use Jackson/Gson)
            UserProfileData data = new UserProfileData();
            
            // Extract username
            int usernameIdx = json.indexOf("\"username\":\"") + 12;
            data.username = json.substring(usernameIdx, json.indexOf("\"", usernameIdx));
            
            // Extract email
            int emailIdx = json.indexOf("\"email\":\"") + 9;
            data.email = json.substring(emailIdx, json.indexOf("\"", emailIdx));
            
            // Extract joinedDate if present
            if (json.contains("\"joinedDate\":")) {
                int dateIdx = json.indexOf("\"joinedDate\":\"") + 14;
                String dateStr = json.substring(dateIdx, json.indexOf("\"", dateIdx));
                data.joinedDate = LocalDateTime.parse(dateStr);
            }
            
            return data;
        } catch (Exception e) {
            System.err.println("Error parsing user profile: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse user statistics dari JSON response
     */
    private UserStatistics parseUserStatistics(String json) {
        try {
            UserStatistics stats = new UserStatistics();
            
            // Extract totalSessions
            int sessionsIdx = json.indexOf("\"totalSessions\":") + 16;
            String sessionsStr = json.substring(sessionsIdx, json.indexOf(",", sessionsIdx));
            stats.totalSessions = Integer.parseInt(sessionsStr);
            
            // Extract totalFocusMinutes
            int focusIdx = json.indexOf("\"totalFocusMinutes\":") + 20;
            String focusStr = json.substring(focusIdx, json.indexOf(",", focusIdx));
            stats.totalFocusMinutes = Integer.parseInt(focusStr);
            
            // Extract tasksCompleted
            int tasksIdx = json.indexOf("\"tasksCompleted\":") + 17;
            String tasksStr = json.substring(tasksIdx, json.indexOf(",", tasksIdx));
            stats.tasksCompleted = Integer.parseInt(tasksStr);
            
            // Extract currentStreak
            int streakIdx = json.indexOf("\"currentStreak\":\"") + 17;
            stats.currentStreak = json.substring(streakIdx, json.indexOf("\"", streakIdx));
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error parsing user statistics: " + e.getMessage());
            return new UserStatistics(); // Return default
        }
    }

    /**
     * Inner classes untuk data
     */
    private static class UserProfileData {
        String username;
        String email;
        LocalDateTime joinedDate;
    }

    private static class UserStatistics {
        int totalSessions = 0;
        int totalFocusMinutes = 0;
        int tasksCompleted = 0;
        String currentStreak = "0";
    }
}
