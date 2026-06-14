package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import com.plr.frontend.util.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
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
    private static final int CYCLE_LENGTH = 4;
    private static final String ACCENT_RED = "timer-accent-red";
    private static final String ACCENT_BLUE = "timer-accent-blue";
    private static final double CIRCUMFERENCE = 2 * Math.PI * 120;

    @FXML private Label timerLabel;
    @FXML private Label sessionTypeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label pointsLabel;
    @FXML private Label activeTaskLabel;
    @FXML private Label cycleLabel;
    @FXML private Circle timerArc;
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
    private long currentTotalSessions = 0;
    private Long focusedTaskId;

    // State per tugas (bertahan selama session aplikasi)
    private final Map<Long, TaskTimerState> taskStateMap = new HashMap<>();

    private AudioClip clickSound;
    private AudioClip completeSound;

    // ========== Inner class state per tugas ==========

    private static class TaskTimerState {
        String mode;
        int focusCountInCycle;
        int completedFocusSessions;
        long totalSessions;

        TaskTimerState(String mode, int focusCountInCycle, int completedFocusSessions, long totalSessions) {
            this.mode = mode;
            this.focusCountInCycle = focusCountInCycle;
            this.completedFocusSessions = completedFocusSessions;
            this.totalSessions = totalSessions;
        }
    }

    // ========== Lifecycle ==========

    @FXML
    public void initialize() {
        try {
            clickSound = new AudioClip(getClass().getResource("/audio/click.wav").toExternalForm());
            completeSound = new AudioClip(getClass().getResource("/audio/complete.wav").toExternalForm());
        } catch (Exception e) {
            System.err.println("Failed to load audio: " + e.getMessage());
        }
        ThemeManager.getInstance().addChangeListener(this::onThemeChanged);
        // Inisialisasi awal timer tanpa bergantung pada jaringan
        applyFocusMode();
    }

    private void onThemeChanged() {
        Platform.runLater(() -> {
            updateAccentColors();
            updateTimerDisplay();
        });
    }

    // ========== Task switching ==========

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

    /**
     * Dipanggil saat user memilih tugas dari TodoPanel.
     * SELALU memuat data terbaru dari backend agar konsisten setelah restart.
     */
    public void setFocusedTaskId(Long id) {
        // Simpan state tugas sebelumnya ke memory
        saveCurrentTaskState();

        this.focusedTaskId = id;
        stopTimer();
        if (remainingSeconds < 0) remainingSeconds = 0;
        sessionDots.getChildren().clear();

        if (id == null) {
            // Mode tanpa tugas aktif: reset ke awal
            completedFocusSessions = 0;
            focusCountInCycle = 0;
            currentTotalSessions = 0;
            sessionCountLabel.setText("");
            pointsLabel.setText("");
            statusLabel.setText("");
            applyFocusMode();
            updateSessionDots();
        } else if (taskStateMap.containsKey(id)) {
            // Sudah ada di memory: restore langsung (tugas yang sudah pernah dibuka sesi ini)
            TaskTimerState saved = taskStateMap.get(id);
            completedFocusSessions = saved.completedFocusSessions;
            focusCountInCycle = saved.focusCountInCycle;
            currentTotalSessions = saved.totalSessions;
            restoreMode(saved.mode);
            updateSessionDots();
            updateTimerDisplay();
            
            // Bersihkan label poin jika berganti tugas
            if (pointsLabel != null) pointsLabel.setText("");
            
            // Instantly update label from memory
            sessionCountLabel.setText(completedFocusSessions + " fokus \u00B7 " + currentTotalSessions + " total sesi");
            
            // Lakukan sinkronisasi ke backend secara asynchronous
            refreshSessionCountLabel();
        } else {
            // Tugas baru / restart aplikasi: WAJIB muat dari backend
            completedFocusSessions = 0;
            focusCountInCycle = 0;
            currentTotalSessions = 0;
            sessionCountLabel.setText(""); // Kosongkan saat memuat
            if (pointsLabel != null) pointsLabel.setText(""); // Bersihkan label poin
            applyFocusMode();
            updateSessionDots();
            loadSessionHistoryForTask(id);
        }
    }

    // ========== Controls ==========

    @FXML
    public void handlePlayPause() {
        if (clickSound != null) clickSound.play();
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
        if (clickSound != null) clickSound.play();
        if (completeSound != null) completeSound.play();
        stopTimer();
        if (remainingSeconds < 0) remainingSeconds = 0;
        updateTimerDisplay();
        statusLabel.setText("\u23ED Sesi dilewati");
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        pt.setOnFinished(evt -> statusLabel.setText(""));
        pt.play();

        String skippedType = currentMode;
        if ("FOCUS".equals(currentMode)) {
            completedFocusSessions++;
            focusCountInCycle++;
            updateSessionDots();
        }
        advanceToNextMode();
        saveCurrentTaskState();
        saveSkippedSession(skippedType);
    }

    // ========== Session saving ==========

    private void saveSkippedSession(String sessionType) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("durationMinutes", 0);
        sessionData.put("sessionType", sessionType);
        sessionData.put("startTime", (sessionStartTime != null ? sessionStartTime : LocalDateTime.now())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (focusedTaskId != null) {
            sessionData.put("taskId", focusedTaskId);
        }

        Task<Map<String, Object>> saveTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().logPomodoroSession(sessionData);
            }
        };

        // Setelah simpan: hanya refresh label, TIDAK reset state cycle
        saveTask.setOnSucceeded(e -> Platform.runLater(this::refreshSessionCountLabel));
        saveTask.setOnFailed(e -> {});
        new Thread(saveTask).start();
    }

    // ========== External task completion ==========

    /**
     * Dipanggil dari luar (TodoPanel) ketika tugas yang sedang difokuskan
     * ditandai selesai. Timer dihentikan, sesi berjalan disimpan, fokus dibersihkan.
     */
    public void handleFocusedTaskCompleted(Long taskId) {
        if (taskId == null || !taskId.equals(focusedTaskId)) return;

        boolean wasActive = isRunning || isPaused;

        if (wasActive) {
            int elapsedMinutes = Math.max(1, (totalSeconds - (Math.max(0, remainingSeconds))) / 60);
            saveCurrentPartialSession(elapsedMinutes);
        }

        stopTimer();
        saveCurrentTaskState();

        focusedTaskId = null;

        Platform.runLater(() -> {
            setActiveTask(null);
            completedFocusSessions = 0;
            focusCountInCycle = 0;
            currentTotalSessions = 0;
            if (sessionCountLabel != null) sessionCountLabel.setText("");
            if (pointsLabel != null) pointsLabel.setText("");
            if (statusLabel != null) statusLabel.setText("");
            applyFocusMode();
            updateSessionDots();
        });
    }

    private void saveCurrentPartialSession(int elapsedMinutes) {
        String sessionType = currentMode;
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("durationMinutes", elapsedMinutes);
        sessionData.put("sessionType", sessionType);
        sessionData.put("startTime", (sessionStartTime != null ? sessionStartTime : LocalDateTime.now())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        sessionData.put("taskId", focusedTaskId);

        Task<Map<String, Object>> saveTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return ApiClient.getInstance().logPomodoroSession(sessionData);
            }
        };

        saveTask.setOnSucceeded(e -> Platform.runLater(this::refreshSessionCountLabel));
        saveTask.setOnFailed(e -> {});
        new Thread(saveTask).start();
    }

    // ========== Mode transitions ==========

    private void applyFocusMode() {
        currentMode = "FOCUS";
        totalSeconds = FOCUS_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Sesi Fokus");
        updateCycleLabel();
        updateAccentColors();
    }

    private void applyShortBreakMode() {
        currentMode = "SHORT_BREAK";
        totalSeconds = SHORT_BREAK_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Istirahat Pendek");
        updateCycleLabel();
        updateAccentColors();
    }

    private void applyLongBreakMode() {
        currentMode = "LONG_BREAK";
        totalSeconds = LONG_BREAK_MINUTES * 60;
        remainingSeconds = totalSeconds;
        updateTimerDisplay();
        sessionTypeLabel.setText("Istirahat Panjang");
        updateCycleLabel();
        updateAccentColors();
    }

    private void restoreMode(String mode) {
        switch (mode) {
            case "FOCUS":       applyFocusMode();      break;
            case "SHORT_BREAK": applyShortBreakMode(); break;
            case "LONG_BREAK":  applyLongBreakMode();  break;
            default:            applyFocusMode();
        }
    }

    // ========== Timer engine ==========

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
        updateAccentColors();

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
        updateAccentColors();
    }

    private void resumeTimer() {
        isRunning = true;
        isPaused = false;
        playPauseBtn.setText("\u23F8");
        if (pomodoroTimeline != null) pomodoroTimeline.play();
        updateAccentColors();
    }

    private void stopTimer() {
        if (pomodoroTimeline != null) pomodoroTimeline.stop();
        isRunning = false;
        isPaused = false;
        playPauseBtn.setText("\u25B6");
        updateAccentColors();
    }

    private void onTimerComplete() {
        if (completeSound != null) completeSound.play();
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
        if (focusedTaskId != null) {
            sessionData.put("taskId", focusedTaskId);
        }

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
                PauseTransition pt = new PauseTransition(Duration.seconds(5));
                pt.setOnFinished(evt -> statusLabel.setText(""));
                pt.play();

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
                saveCurrentTaskState();
                // Hanya refresh label angka, TIDAK ubah state cycle
                refreshSessionCountLabel();
            });
        });

        saveTask.setOnFailed(e -> Platform.runLater(() -> {
            statusLabel.setText("\u26A0 Sesi selesai (gagal simpan)");
            PauseTransition pt = new PauseTransition(Duration.seconds(5));
            pt.setOnFinished(evt -> statusLabel.setText(""));
            pt.play();
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

    // ========== State persistence per tugas ==========

    private void saveCurrentTaskState() {
        if (focusedTaskId != null) {
            taskStateMap.put(focusedTaskId, new TaskTimerState(
                currentMode,
                focusCountInCycle,
                completedFocusSessions,
                currentTotalSessions
            ));
        }
    }

    /**
     * Memuat sesi tugas dari backend dan merestore state cycle.
     * Dipanggil HANYA saat task pertama kali dipilih setelah restart.
     * State yang di-restore kemudian disimpan ke taskStateMap (memory).
     */
    public void loadSessionHistoryForTask(Long taskId) {
        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getPomodoroSessionsByTask(taskId);
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Map<String, Object>> sessions = loadTask.getValue();
            Platform.runLater(() -> {
                // Hanya proses jika task ini masih dipilih (hindari race condition)
                if (!taskId.equals(focusedTaskId)) return;

                long focusCount = sessions.stream()
                    .filter(s -> "FOCUS".equals(s.get("sessionType")))
                    .count();

                completedFocusSessions = (int) focusCount;
                focusCountInCycle = (int) (focusCount % CYCLE_LENGTH);

                // Tentukan mode selanjutnya berdasarkan sesi terakhir (sudah terurut ASC dari DB)
                String lastSessionType = sessions.isEmpty() ? null
                    : (String) sessions.get(sessions.size() - 1).get("sessionType");
                String restoredMode = deriveNextMode(lastSessionType, focusCountInCycle);

                // Simpan ke memory agar perpindahan tugas berikutnya bisa pakai cache
                currentTotalSessions = sessions.size();
                taskStateMap.put(taskId, new TaskTimerState(
                    restoredMode, focusCountInCycle, completedFocusSessions, currentTotalSessions));

                restoreMode(restoredMode);
                updateSessionDots();

                // Update label sesi
                sessionCountLabel.setText(focusCount + " fokus \u00B7 " + currentTotalSessions + " total sesi");
            });
        });

        loadTask.setOnFailed(e -> Platform.runLater(() -> {
            if (!taskId.equals(focusedTaskId)) return;
            applyFocusMode();
            updateSessionDots();
        }));

        new Thread(loadTask).start();
    }

    /**
     * Menentukan mode timer berikutnya berdasarkan tipe sesi terakhir yang selesai.
     * Dipanggil saat me-restore state dari DB setelah restart.
     */
    private String deriveNextMode(String lastSessionType, int cyclePos) {
        if (lastSessionType == null) return "FOCUS";
        switch (lastSessionType) {
            case "FOCUS":
                // cyclePos == 0 artinya cycle baru saja penuh (4 % 4 = 0) → saatnya long break
                return (cyclePos == 0) ? "LONG_BREAK" : "SHORT_BREAK";
            case "SHORT_BREAK":
            case "LONG_BREAK":
                return "FOCUS";
            default:
                return "FOCUS";
        }
    }

    /**
     * Hanya memperbarui label statistik sesi (jumlah fokus & total sesi).
     * TIDAK mengubah mode timer, dots, atau counter cycle.
     * Dipanggil setelah sesi selesai/dilewati untuk sinkronisasi angka dari DB.
     */
    private void refreshSessionCountLabel() {
        if (focusedTaskId == null) return;
        final Long currentTaskId = focusedTaskId;

        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getPomodoroSessionsByTask(currentTaskId);
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Map<String, Object>> sessions = loadTask.getValue();
            Platform.runLater(() -> {
                if (!currentTaskId.equals(focusedTaskId)) return;
                long focusCount = sessions.stream()
                    .filter(s -> "FOCUS".equals(s.get("sessionType")))
                    .count();
                currentTotalSessions = sessions.size();
                completedFocusSessions = (int) focusCount;
                sessionCountLabel.setText(focusCount + " fokus \u00B7 " + currentTotalSessions + " total sesi");
            });
        });

        loadTask.setOnFailed(e -> {});
        new Thread(loadTask).start();
    }

    // Method ini dipanggil dari MainLayoutController, dijaga agar tidak mengubah state
    public void loadSessionHistory() {
        // Kosongkan — state cycle sekarang hanya dikelola oleh loadSessionHistoryForTask
        // agar tidak ada race condition antara global load dan task-specific load
    }

    // ========== UI helpers ==========

    private void updateSessionDots() {
        if (sessionDots == null) return;
        sessionDots.getChildren().clear();
        int filled = focusCountInCycle;
        if (filled > CYCLE_LENGTH) filled = CYCLE_LENGTH;
        String filledColor = getCurrentAccentColor();
        for (int i = 0; i < CYCLE_LENGTH; i++) {
            Circle dot = new Circle(5);
            if (i < filled) {
                dot.getStyleClass().add("timer-dot-filled");
                dot.setFill(Color.web(filledColor));
            } else {
                dot.getStyleClass().add("timer-dot-empty");
            }
            sessionDots.getChildren().add(dot);
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

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (totalSeconds > 0) {
            double progress = Math.max(0, Math.min(1, (double) remainingSeconds / totalSeconds));
            double dashLength = CIRCUMFERENCE * progress;
            timerArc.getStrokeDashArray().setAll(dashLength, CIRCUMFERENCE);
            timerArc.setStroke(Color.web(getCurrentAccentColor()));
        }
    }

    private void updateAccentColors() {
        playPauseBtn.getStyleClass().removeAll(ACCENT_RED, ACCENT_BLUE);
        pointsLabel.getStyleClass().removeAll(ACCENT_RED, ACCENT_BLUE);
        activeTaskLabel.getStyleClass().removeAll(ACCENT_RED, ACCENT_BLUE);

        if (isRunning) {
            String cls = "FOCUS".equals(currentMode) ? ACCENT_RED : ACCENT_BLUE;
            playPauseBtn.getStyleClass().add(cls);
            pointsLabel.getStyleClass().add(cls);
            activeTaskLabel.getStyleClass().add(cls);
        }

        timerArc.setStroke(Color.web(getCurrentAccentColor()));
        updateSessionDots();
    }

    private String getCurrentAccentColor() {
        boolean light = ThemeManager.getInstance().isLightMode();
        if (isRunning) {
            if ("FOCUS".equals(currentMode)) return light ? "#d06a84" : "#f38ba8";
            return light ? "#5a7fc8" : "#7aa2f7";
        }
        return "#C084FC";
    }

    private String getModeLabel() {
        switch (currentMode) {
            case "FOCUS":       return "Sesi Fokus";
            case "SHORT_BREAK": return "Istirahat Pendek";
            case "LONG_BREAK":  return "Istirahat Panjang";
            default:            return "";
        }
    }

    private int getDurationForMode() {
        switch (currentMode) {
            case "FOCUS":       return FOCUS_MINUTES;
            case "SHORT_BREAK": return SHORT_BREAK_MINUTES;
            case "LONG_BREAK":  return LONG_BREAK_MINUTES;
            default:            return FOCUS_MINUTES;
        }
    }
}
