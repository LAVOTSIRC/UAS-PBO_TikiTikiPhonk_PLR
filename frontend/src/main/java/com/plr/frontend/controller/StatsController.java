package com.plr.frontend.controller;

import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;

public class StatsController {

    @FXML private Label totalSessionsLabel;
    @FXML private Label totalFocusLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label totalPointsLabel;
    @FXML private BarChart<String, Number> focusBarChart;
    @FXML private PieChart taskPieChart;

    @FXML private Label statsErrorLabel;

    public void loadStats() {
        if (statsErrorLabel != null) {
            statsErrorLabel.setVisible(false);
            statsErrorLabel.setText("");
        }
        new Thread(() -> {
            try {
                Map<String, Object> stats = ApiClient.getInstance().getStats();
                Platform.runLater(() -> populateStats(stats));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (statsErrorLabel != null) {
                        statsErrorLabel.setText("Gagal memuat data: " + e.getMessage());
                        statsErrorLabel.setVisible(true);
                    }
                });
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void populateStats(Map<String, Object> stats) {
        totalSessionsLabel.setText(String.valueOf(stats.getOrDefault("totalSessions", 0)));
        totalFocusLabel.setText(String.valueOf(stats.getOrDefault("totalFocusMinutes", 0)));
        completedTasksLabel.setText(String.valueOf(stats.getOrDefault("completedTasks", 0)));
        totalPointsLabel.setText(String.valueOf(stats.getOrDefault("totalPoints", 0)));

        focusBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Menit Fokus");

        List<Map<String, Object>> days = (List<Map<String, Object>>) stats.get("focusMinutesByDay");
        if (days != null) {
            for (Map<String, Object> day : days) {
                String date = (String) day.get("date");
                int minutes = ((Number) day.get("minutes")).intValue();
                series.getData().add(new XYChart.Data<>(date, minutes));
            }
        }
        focusBarChart.getData().add(series);

        taskPieChart.getData().clear();
        int completed = ((Number) stats.getOrDefault("completedTasks", 0)).intValue();
        int active = ((Number) stats.getOrDefault("activeTasks", 0)).intValue();

        double doneVal = completed > 0 ? completed : 0.5;
        double activeVal = active > 0 ? active : 0.5;

        PieChart.Data doneSlice = new PieChart.Data("Selesai", doneVal);
        PieChart.Data activeSlice = new PieChart.Data("Aktif", activeVal);
        taskPieChart.getData().addAll(doneSlice, activeSlice);

        Platform.runLater(() -> {
            if (doneSlice.getNode() != null) {
                doneSlice.getNode().setStyle("-fx-pie-color: #C084FC;");
            }
            if (activeSlice.getNode() != null) {
                activeSlice.getNode().setStyle("-fx-pie-color: #3B2E4A;");
            }
        });
    }
}
