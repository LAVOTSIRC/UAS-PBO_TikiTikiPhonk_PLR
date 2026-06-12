package com.plr.frontend.controller;

import com.plr.frontend.JavaFXApp;
import com.plr.frontend.util.ApiClient;
import com.plr.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password tidak boleh kosong!");
            return;
        }

        setLoading(true);

        Task<Map<String, Object>> loginTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            Map<String, Object> response = loginTask.getValue();
            String token = (String) response.get("token");
            String uname = (String) response.get("username");
            Long uid = ((Number) response.get("userId")).longValue();
            SessionManager.getInstance().setSession(token, uname, uid);
            setLoading(false);
            JavaFXApp.showScene("fxml/MainLayout.fxml", "TikiTikiPhonk - " + uname, 1100, 700);
        });

        loginTask.setOnFailed(event -> {
            setLoading(false);
            showError("Login gagal: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    @FXML
    public void goToRegister() {
        JavaFXApp.showScene("fxml/register.fxml", "TikiTikiPhonk - Daftar", 480, 580);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
        if (!loading) errorLabel.setVisible(false);
    }
}
