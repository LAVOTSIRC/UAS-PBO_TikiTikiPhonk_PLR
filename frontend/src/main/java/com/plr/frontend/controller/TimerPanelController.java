package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimerPanelController {

    @FXML private Label timerLabel;
    @FXML private Label sessionTypeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label statusLabel;
    @FXML private Arc timerArc;
    @FXML private Button playPauseBtn;
    @FXML private Button skipBtn;
    @FXML private Button focusTabBtn;
    @FXML private Button breakTabBtn;

    private Timeline pomodoroTimeline;
    private int totalSeconds;
    private int remainingSeconds;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean isFocusMode = true;
    private LocalDateTime sessionStartTime;

    @FXML
    public void initialize() {
        setFocusMode();
    }

    @FXML
    public void handleFocusTab() {
        isFocusMode = true;
        stopTimer();
        setFocusMode();
        updateTabStyles();
    }

    @FXML
    public void handleBreakTab() {
        isFocusMode = false;
        stopTimer();
        setBreakMode();
        updateTabStyles();
    }

    @FXML
    public void handlePlayPause() {
        if (!isRunning) {
            startTimer();
        } else {
            pauseTimer();
        }
    }

    @FXML
    public void handleSkip() {
        stopTimer();
        if (isFocusMode) {
            handleBreakTab();
        } else {
            handleFocusTab();
        }
    }

    private void setFocusMode() {
        totalSeconds = 25 * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        if (sessionTypeLabel != null) sessionTypeLabel.setText("Sesi Fokus");
        if (focusTabBtn != null) focusTabBtn.getStyleClass().add("active");
        if (breakTabBtn != null) breakTabBtn.getStyleClass().remove("active");
    }

    private void setBreakMode() {
        totalSeconds = 5 * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        if (sessionTypeLabel != null) sessionTypeLabel.setText("Istirahat Pendek");
        if (breakTabBtn != null) breakTabBtn.getStyleClass().add("active");
        if (focusTabBtn != null) focusTabBtn.getStyleClass().remove("active");
    }

    private void startTimer() {
        if (!isPaused) {
            sessionStartTime = LocalDateTime.now();
        }

        isRunning = true;
        isPaused = false;
        if (playPauseBtn != null) playPauseBtn.setText("⏸");

        pomodoroTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                remainingSeconds--;
                updateTimerDisplay();
                if (remainingSeconds <= 0) {
                    onTimerComplete();
                }
            })
        );
        pomodoroTimeline.setCycleCount(Timeline.INDEFINITE);
        pomodoroTimeline.play();
    }

    private void pauseTimer() {
        if (pomodoroTimeline != null) pomodoroTimeline.pause();
        isRunning = false;
        isPaused = true;
        if (playPauseBtn != null) playPauseBtn.setText("▶");
    }

    private void stopTimer() {
        if (pomodoroTimeline != null) pomodoroTimeline.stop();
        isRunning = false;
        isPaused = false;
        if (playPauseBtn != null) playPauseBtn.setText("▶");
    }

    private void onTimerComplete() {
        stopTimer();
        remainingSeconds = 0;
        updateTimerDisplay();

        String sessionType = isFocusMode ? "FOCUS" : "SHORT_BREAK";
        int durationMinutes = isFocusMode ? 25 : 5;

        if (statusLabel != null) statusLabel.setText("✅ Sesi selesai! Menyimpan...");

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("durationMinutes", durationMinutes);
        sessionData.put("sessionType", sessionType);
        sessionData.put("startTime", sessionStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Task<Map<String, Object>> saveTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().logPomodoroSession(sessionData);
            }
        };

        saveTask.setOnSucceeded(e -> Platform.runLater(() -> {
            if (statusLabel != null)
                statusLabel.setText("✅ Sesi tersimpan!");
            loadSessionHistory();
        }));

        saveTask.setOnFailed(e -> Platform.runLater(() -> {
            if (statusLabel != null)
                statusLabel.setText("Sesi selesai (gagal simpan)");
        }));

        new Thread(saveTask).start();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String time = String.format("%02d:%02d", minutes, seconds);

        Platform.runLater(() -> {
            if (timerLabel != null) timerLabel.setText(time);
            if (timerArc != null && totalSeconds > 0) {
                double progress = (double) remainingSeconds / totalSeconds;
                timerArc.setLength(360.0 * progress);
                if (progress > 0.5) {
                    timerArc.setStroke(Color.web("#C084FC"));
                } else if (progress > 0.25) {
                    timerArc.setStroke(Color.web("#f9e2af"));
                } else {
                    timerArc.setStroke(Color.web("#f38ba8"));
                }
            }
        });
    }

    public void loadSessionHistory() {
        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getPomodoroSessions();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Map<String, Object>> sessions = loadTask.getValue();
            Platform.runLater(() -> {
                if (sessionCountLabel != null) {
                    long todaySessions = sessions.size();
                    sessionCountLabel.setText("Total: " + todaySessions + " sesi");
                }
            });
        });

        loadTask.setOnFailed(e -> {});
        new Thread(loadTask).start();
    }

    private void updateTabStyles() {
        // Tab style updates handled in handleFocusTab / handleBreakTab
    }
}
