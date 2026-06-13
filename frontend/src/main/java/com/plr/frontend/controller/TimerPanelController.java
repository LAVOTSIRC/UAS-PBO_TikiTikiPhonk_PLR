package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimerPanelController {

    private static final int FOCUS_MINUTES = 25;
    private static final int SHORT_BREAK_MINUTES = 5;
    private static final int LONG_BREAK_MINUTES = 15;
    private static final int CYCLE_LENGTH = 5;

    @FXML private Label timerLabel;
    @FXML private Label sessionTypeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label pointsLabel;
    @FXML private Label activeTaskLabel;
    @FXML private Label cycleLabel;
    @FXML private Arc timerArc;
    @FXML private Button playPauseBtn;
    @FXML private Button skipBtn;
    @FXML private HBox sessionDots;

    private Timeline pomodoroTimeline;
    private int totalSeconds;
    private int remainingSeconds;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private String currentMode = "FOCUS";
    private LocalDateTime sessionStartTime;
    private int completedFocusSessions = 0;
    private int focusCountInCycle = 0;
    private boolean modeInitialized = false;

    @FXML
    public void initialize() {
        loadSessionHistory();
    }

    public void setActiveTask(String taskTitle) {
        Platform.runLater(() -> {
            if (activeTaskLabel == null) return;
            if (taskTitle != null && !taskTitle.isEmpty()) {
                activeTaskLabel.setText("\u25B6 " + taskTitle);
                activeTaskLabel.setVisible(true);
            } else {
                activeTaskLabel.setVisible(false);
            }
        });
    }

    @FXML
    public void handlePlayPause() {
        if (!isRunning && !isPaused) {
            startTimer();
        } else if (isPaused) {
            resumeTimer();
        } else {
            pauseTimer();
        }
    }

    @FXML
    public void handleSkip() {
        stopTimer();
        onTimerComplete();
    }

    private void applyFocusMode() {
        currentMode = "FOCUS";
        totalSeconds = FOCUS_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Sesi Fokus");
        updateCycleLabel();
    }

    private void applyShortBreakMode() {
        currentMode = "SHORT_BREAK";
        totalSeconds = SHORT_BREAK_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Istirahat Pendek");
        updateCycleLabel();
    }

    private void applyLongBreakMode() {
        currentMode = "LONG_BREAK";
        totalSeconds = LONG_BREAK_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Istirahat Panjang");
        updateCycleLabel();
    }

    private void startTimer() {
        if (remainingSeconds <= 0) {
            remainingSeconds = totalSeconds;
            updateTimerDisplay();
        }
        sessionStartTime = LocalDateTime.now();
        isRunning = true;
        isPaused = false;
        playPauseBtn.setText("\u23F8");
        if (statusLabel != null) statusLabel.setText("");
        if (pointsLabel != null) pointsLabel.setText("");

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
        playPauseBtn.setText("\u25B6");
    }

    private void resumeTimer() {
        isRunning = true;
        isPaused = false;
        playPauseBtn.setText("\u23F8");
        if (pomodoroTimeline != null) pomodoroTimeline.play();
    }

    private void stopTimer() {
        if (pomodoroTimeline != null) pomodoroTimeline.stop();
        isRunning = false;
        isPaused = false;
        playPauseBtn.setText("\u25B6");
    }

    private void onTimerComplete() {
        stopTimer();
        if (remainingSeconds < 0) remainingSeconds = 0;
        updateTimerDisplay();

        String sessionType = currentMode;
        int durationMinutes = getDurationForMode();

        statusLabel.setText("\u2705 Sesi selesai! Menyimpan...");

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

        saveTask.setOnSucceeded(e -> {
            Map<String, Object> result = saveTask.getValue();
            Platform.runLater(() -> {
                statusLabel.setText("\u2705 Sesi tersimpan!");

                if ("FOCUS".equals(sessionType)) {
                    completedFocusSessions++;
                    focusCountInCycle++;
                    updateSessionDots();
                }

                if (result != null && result.containsKey("points")) {
                    int points = ((Number) result.get("points")).intValue();
                    pointsLabel.setText("+ " + points + " poin");
                }

                advanceToNextMode();
                loadSessionHistory();
            });
        });

        saveTask.setOnFailed(e -> Platform.runLater(() -> {
            statusLabel.setText("\u26A0 Sesi selesai (gagal simpan)");
        }));

        new Thread(saveTask).start();
    }

    private void advanceToNextMode() {
        switch (currentMode) {
            case "FOCUS":
                if (focusCountInCycle >= CYCLE_LENGTH) {
                    focusCountInCycle = 0;
                    applyLongBreakMode();
                } else {
                    applyShortBreakMode();
                }
                break;
            case "SHORT_BREAK":
            case "LONG_BREAK":
                applyFocusMode();
                break;
        }
        sessionTypeLabel.setText("Selanjutnya: " + getModeLabel());
    }

    private String getModeLabel() {
        switch (currentMode) {
            case "FOCUS":
                return "Sesi Fokus";
            case "SHORT_BREAK":
                return "Istirahat Pendek";
            case "LONG_BREAK":
                return "Istirahat Panjang";
            default:
                return "";
        }
    }

    private int getDurationForMode() {
        switch (currentMode) {
            case "FOCUS": return FOCUS_MINUTES;
            case "SHORT_BREAK": return SHORT_BREAK_MINUTES;
            case "LONG_BREAK": return LONG_BREAK_MINUTES;
            default: return FOCUS_MINUTES;
        }
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (totalSeconds > 0) {
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
    }

    private void updateCycleLabel() {
        if (cycleLabel == null) return;
        switch (currentMode) {
            case "FOCUS":
                cycleLabel.setText("Sesi Fokus ke-" + (focusCountInCycle + 1) + " dari " + CYCLE_LENGTH);
                break;
            case "SHORT_BREAK":
                cycleLabel.setText("Istirahat ke-" + focusCountInCycle + " dari " + CYCLE_LENGTH);
                break;
            case "LONG_BREAK":
                cycleLabel.setText("Istirahat Panjang");
                break;
        }
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
                long totalSessions = sessions.size();
                long focusCount = sessions.stream()
                    .filter(s -> "FOCUS".equals(s.get("sessionType")))
                    .count();
                sessionCountLabel.setText(focusCount + " fokus \u00B7 " + totalSessions + " total sesi");
                completedFocusSessions = (int) focusCount;
                focusCountInCycle = (int) (focusCount % CYCLE_LENGTH);
                updateSessionDots();
                if (!modeInitialized) {
                    modeInitialized = true;
                    applyFocusMode();
                }
            });
        });

        loadTask.setOnFailed(e -> {});
        new Thread(loadTask).start();
    }

    private void updateSessionDots() {
        if (sessionDots == null) return;
        sessionDots.getChildren().clear();
        int filled = focusCountInCycle;
        if (filled > CYCLE_LENGTH) filled = CYCLE_LENGTH;
        for (int i = 0; i < CYCLE_LENGTH; i++) {
            Circle dot = new Circle(5);
            if (i < filled) {
                dot.setFill(Color.web("#C084FC"));
            } else {
                dot.setFill(Color.web("#2D2936"));
            }
            sessionDots.getChildren().add(dot);
        }
    }
}
