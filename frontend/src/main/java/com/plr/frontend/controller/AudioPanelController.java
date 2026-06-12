package com.plr.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;

public class AudioPanelController {

    @FXML private Label nowPlayingLabel;
    @FXML private Button playPauseBtn;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeLabel;
    @FXML private ProgressBar audioProgressBar;
    @FXML private Button whiteNoiseBtn;
    @FXML private Button brownNoiseBtn;
    @FXML private Button pinkNoiseBtn;
    @FXML private Button blueNoiseBtn;
    @FXML private TextField filePathField;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Button activeNoiseBtn = null;

    @FXML
    public void initialize() {
        volumeSlider.setValue(0.7);
        volumeSlider.valueProperty().addListener((obs, old, val) -> {
            if (volumeLabel != null)
                volumeLabel.setText((int)(val.doubleValue() * 100) + "%");
            if (mediaPlayer != null)
                mediaPlayer.setVolume(val.doubleValue());
        });
        if (volumeLabel != null) volumeLabel.setText("70%");
        if (playPauseBtn != null) playPauseBtn.setDisable(true);
        if (audioProgressBar != null) audioProgressBar.setProgress(0);
        if (nowPlayingLabel != null) nowPlayingLabel.setText("Tidak ada audio");
    }

    @FXML
    public void playWhiteNoise() {
        setActiveNoise(whiteNoiseBtn);
        playBundledAudio("audio/rain.mp3", "🌧 White Noise");
    }

    @FXML
    public void playBrownNoise() {
        setActiveNoise(brownNoiseBtn);
        playBundledAudio("audio/coffee_shop.mp3", "☕ Brown Noise");
    }

    @FXML
    public void playPinkNoise() {
        setActiveNoise(pinkNoiseBtn);
        playBundledAudio("audio/forest.mp3", "🌿 Pink Noise");
    }

    @FXML
    public void playBlueNoise() {
        setActiveNoise(blueNoiseBtn);
        playBundledAudio("audio/keyboard.mp3", "⌨ Blue Noise");
    }

    private void playBundledAudio(String resourcePath, String displayName) {
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            if (nowPlayingLabel != null)
                nowPlayingLabel.setText("⚠ File tidak ditemukan: " + resourcePath);
            return;
        }
        playMedia(resourceUrl.toExternalForm(), displayName);
    }

    @FXML
    public void browseLocalFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Audio");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            if (filePathField != null) filePathField.setText(selectedFile.getAbsolutePath());
            setActiveNoise(null);
            playMedia(selectedFile.toURI().toString(), selectedFile.getName());
        }
    }

    private void playMedia(String mediaUri, String displayName) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(mediaUri);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volumeSlider.getValue());
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
                if (media.getDuration().greaterThan(Duration.ZERO)) {
                    double progress = now.toSeconds() / media.getDuration().toSeconds();
                    if (audioProgressBar != null) audioProgressBar.setProgress(progress);
                }
            });

            mediaPlayer.setOnReady(() -> {
                if (playPauseBtn != null) playPauseBtn.setDisable(false);
            });

            mediaPlayer.play();
            isPlaying = true;
            if (playPauseBtn != null) playPauseBtn.setText("⏸");
            if (nowPlayingLabel != null) nowPlayingLabel.setText("▶ " + displayName);

        } catch (Exception e) {
            if (nowPlayingLabel != null) nowPlayingLabel.setText("⚠ Error: " + e.getMessage());
        }
    }

    @FXML
    public void handlePlayPause() {
        if (mediaPlayer == null) return;
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            if (playPauseBtn != null) playPauseBtn.setText("▶");
        } else {
            mediaPlayer.play();
            isPlaying = true;
            if (playPauseBtn != null) playPauseBtn.setText("⏸");
        }
    }

    @FXML
    public void handleStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            if (playPauseBtn != null) playPauseBtn.setText("▶");
            if (audioProgressBar != null) audioProgressBar.setProgress(0);
            if (nowPlayingLabel != null) nowPlayingLabel.setText("⏹ Dihentikan");
            setActiveNoise(null);
        }
    }

    private void setActiveNoise(Button btn) {
        Button[] noiseBtns = {whiteNoiseBtn, brownNoiseBtn, pinkNoiseBtn, blueNoiseBtn};
        for (Button b : noiseBtns) {
            if (b != null) b.getStyleClass().remove("active");
        }
        if (btn != null) {
            if (!btn.getStyleClass().contains("active"))
                btn.getStyleClass().add("active");
        }
        activeNoiseBtn = btn;
    }
}
