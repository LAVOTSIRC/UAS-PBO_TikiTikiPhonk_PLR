package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.ApiClient;
import com.plr.frontend.util.SessionManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        // BUG-16 FIX: Jangan trim() password — spasi di awal/akhir adalah bagian valid dari password
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Semua field harus diisi!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Password dan konfirmasi password tidak cocok!");
            return;
        }
        if (password.length() < 6) {
            showError("Password minimal 6 karakter!");
            return;
        }

        setLoading(true);

        Task<Map<String, Object>> registerTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().register(username, email, password);
            }
        };

        registerTask.setOnSucceeded(event -> {
            Map<String, Object> response = registerTask.getValue();
            String token = (String) response.get("token");
            String uname = (String) response.get("username");
            Long uid = ((Number) response.get("userId")).longValue();
            SessionManager.getInstance().setSession(token, uname, uid);
            // BUG-04 FIX: Eksplisit Platform.runLater() untuk semua UI update pasca async
            Platform.runLater(() -> {
                setLoading(false);
                JavaFXApp.showScene("fxml/MainLayout.fxml", "TikiTikiPhonk - " + uname, 1100, 700);
            });
        });

        registerTask.setOnFailed(event -> {
            setLoading(false);
            showError("Registrasi gagal: " + registerTask.getException().getMessage());
        });

        new Thread(registerTask).start();
    }

    @FXML
    public void goToLogin() {
        JavaFXApp.showScene("fxml/login.fxml", "TikiTikiPhonk - Login", 480, 580);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
        if (!loading) errorLabel.setVisible(false);
    }
}
