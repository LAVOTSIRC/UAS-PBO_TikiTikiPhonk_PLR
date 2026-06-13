package com.plr.frontend.controller;

import com.plr.frontend.model.AudioTrack;
import com.plr.frontend.model.NoiseType;
import com.plr.frontend.service.AudioPlayerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * AudioPanelController — Controller Layer (MVC).
 *
 * Tanggung jawab:
 * - Menginisialisasi binding antara komponen UI dan AudioPlayerService
 * - Menangani event dari tombol / slider
 * - Memperbarui UI sebagai respons terhadap callback dari service
 *
 * TIDAK boleh mengandung logika MediaPlayer secara langsung.
 * Semua operasi audio didelegasikan ke {@link AudioPlayerService}.
 */
public class AudioPanelController {

    // ── FXML Injections ────────────────────────────────────────────────────

    // --- Noise Tab ---
    @FXML private Button whiteNoiseBtn;
    @FXML private Button brownNoiseBtn;
    @FXML private Button rainNoiseBtn;
    @FXML private Button forestNoiseBtn;

    // --- Playlist Tab ---
    @FXML private ListView<AudioTrack> playlistView;
    @FXML private VBox                 emptyPlaylistLabel; // VBox placeholder "playlist kosong"

    // --- Volume (shared) ---
    @FXML private Slider volumeSlider;
    @FXML private Label  volumeLabel;

    // --- Player Bar (bottom) ---
    @FXML private Label       nowPlayingLabel;
    @FXML private Label       currentTimeLabel;
    @FXML private Label       totalDurationLabel;
    @FXML private ProgressBar audioProgressBar;
    @FXML private Button      prevBtn;
    @FXML private Button      playPauseBtn;
    @FXML private Button      nextBtn;
    @FXML private Button      stopBtn;

    // ── Service ────────────────────────────────────────────────────────────

    private final AudioPlayerService audioService = new AudioPlayerService();

    // ── Initialize ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupServiceCallbacks();
        setupVolumeSlider();
        setupPlaylistView();
        resetPlayerBar();
    }

    // ── Setup Helpers ──────────────────────────────────────────────────────

    /**
     * Mendaftarkan semua callback dari service ke UI update yang sesuai.
     */
    private void setupServiceCallbacks() {
        // Progress: [progress, currentSec, totalSec]
        audioService.setOnProgressUpdate(data -> {
            audioProgressBar.setProgress(data[0]);
            currentTimeLabel.setText(formatTime(data[1]));
            totalDurationLabel.setText(formatTime(data[2]));
        });

        // Track berubah → update label "Now Playing"
        audioService.setOnTrackChanged(name -> {
            nowPlayingLabel.setText(name);
        });

        // State play/pause berubah → update ikon tombol
        audioService.setOnPlayStateChanged(playing -> {
            playPauseBtn.setText(playing ? "\u23F8" : "\u25B6"); // ⏸ atau ▶
            playPauseBtn.setDisable(false);
        });

        // Track selesai → otomatis putar berikutnya
        audioService.setOnTrackEnded(() -> {
            audioService.nextTrack();
            // Perbarui highlight item di playlist
            playlistView.getSelectionModel().select(audioService.getCurrentIndex());
        });
    }

    /**
     * Menghubungkan slider volume ke service dan label persentase.
     */
    private void setupVolumeSlider() {
        volumeSlider.setValue(audioService.getCurrentVolume());
        updateVolumeLabel(audioService.getCurrentVolume());

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            audioService.setVolume(vol);
            updateVolumeLabel(vol);
        });
    }

    /**
     * Menghubungkan ListView playlist ke ObservableList dari service,
     * serta listener untuk klik item agar langsung memutar track.
     */
    private void setupPlaylistView() {
        // Binding dua arah: ListView otomatis update saat playlist di service berubah
        playlistView.setItems(audioService.getPlaylist());

        // Putar langsung saat item diklik
        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                int idx = playlistView.getSelectionModel().getSelectedIndex();
                if (idx >= 0) {
                    clearNoiseActiveState();
                    audioService.playTrackAt(idx);
                    playPauseBtn.setDisable(false);
                    updateEmptyPlaylistVisibility();
                }
            }
        });

        updateEmptyPlaylistVisibility();
    }

    /** Mereset tampilan player bar ke kondisi awal. */
    private void resetPlayerBar() {
        nowPlayingLabel.setText("Tidak ada audio");
        currentTimeLabel.setText("0:00");
        totalDurationLabel.setText("0:00");
        audioProgressBar.setProgress(0);
        playPauseBtn.setDisable(true);
        playPauseBtn.setText("\u25B6"); // ▶
    }

    // ── Noise Tab Event Handlers ───────────────────────────────────────────

    @FXML
    public void playWhiteNoise() {
        handleNoiseButton(whiteNoiseBtn, NoiseType.WHITE_NOISE);
    }

    @FXML
    public void playBrownNoise() {
        handleNoiseButton(brownNoiseBtn, NoiseType.BROWN_NOISE);
    }

    @FXML
    public void playRainNoise() {
        handleNoiseButton(rainNoiseBtn, NoiseType.RAIN);
    }

    @FXML
    public void playForestNoise() {
        handleNoiseButton(forestNoiseBtn, NoiseType.FOREST);
    }

    /**
     * Menangani klik tombol noise:
     * - Jika tombol ini sudah aktif → toggle play/pause
     * - Jika tombol lain → ganti noise & set active style
     */
    private void handleNoiseButton(Button clickedBtn, NoiseType type) {
        setNoiseActiveState(clickedBtn);
        clearPlaylistSelection();
        audioService.playNoise(type);
        playPauseBtn.setDisable(false);
    }

    // ── Playlist Tab Event Handlers ────────────────────────────────────────

    /**
     * Membuka FileChooser untuk import file audio lokal.
     * File yang dipilih langsung diputar dan ditambahkan ke playlist.
     */
    @FXML
    public void handleImportMusic() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import File Audio");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files (MP3, WAV)", "*.mp3", "*.wav"),
            new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"),
            new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );

        // Ambil stage dari salah satu komponen yang ada
        Stage stage = (Stage) playlistView.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            int firstNewIndex = audioService.getPlaylist().size();

            for (File file : selectedFiles) {
                AudioTrack track = new AudioTrack(file);
                audioService.addTrack(track);
            }

            updateEmptyPlaylistVisibility();

            // Langsung putar file pertama yang baru diimport
            clearNoiseActiveState();
            audioService.playTrackAt(firstNewIndex);
            playlistView.getSelectionModel().select(firstNewIndex);
            playlistView.scrollTo(firstNewIndex);
            playPauseBtn.setDisable(false);
        }
    }

    // ── Audio Controls Event Handlers ─────────────────────────────────────

    @FXML
    public void handlePlayPause() {
        audioService.togglePlayPause();
    }

    @FXML
    public void handleStop() {
        audioService.stop();
        clearNoiseActiveState();
        clearPlaylistSelection();
        playPauseBtn.setDisable(true);
        playPauseBtn.setText("\u25B6"); // ▶
    }

    @FXML
    public void handleNext() {
        if (audioService.isNoiseMode() || audioService.getPlaylist().isEmpty()) return;
        audioService.nextTrack();
        playlistView.getSelectionModel().select(audioService.getCurrentIndex());
        playlistView.scrollTo(audioService.getCurrentIndex());
    }

    @FXML
    public void handlePrevious() {
        if (audioService.isNoiseMode() || audioService.getPlaylist().isEmpty()) return;
        audioService.previousTrack();
        playlistView.getSelectionModel().select(audioService.getCurrentIndex());
        playlistView.scrollTo(audioService.getCurrentIndex());
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    /**
     * Dipanggil saat controller dihancurkan.
     * Memastikan MediaPlayer dilepaskan dengan benar.
     */
    public void shutdown() {
        audioService.dispose();
    }

    // ── UI State Helpers ───────────────────────────────────────────────────

    /**
     * Mengatur tombol noise mana yang aktif (dengan style "active"),
     * dan menghapus style "active" dari semua tombol lainnya.
     */
    private void setNoiseActiveState(Button activeBtn) {
        Button[] noiseBtns = { whiteNoiseBtn, brownNoiseBtn, rainNoiseBtn, forestNoiseBtn };
        for (Button btn : noiseBtns) {
            if (btn != null) btn.getStyleClass().remove("noise-btn-active");
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("noise-btn-active")) {
            activeBtn.getStyleClass().add("noise-btn-active");
        }
    }

    /** Menghapus semua highlight dari tombol noise. */
    private void clearNoiseActiveState() {
        setNoiseActiveState(null);
    }

    /** Menghapus seleksi di playlist. */
    private void clearPlaylistSelection() {
        playlistView.getSelectionModel().clearSelection();
    }

    /** Menampilkan atau menyembunyikan label placeholder "playlist kosong". */
    private void updateEmptyPlaylistVisibility() {
        boolean empty = audioService.getPlaylist().isEmpty();
        emptyPlaylistLabel.setVisible(empty);
        emptyPlaylistLabel.setManaged(empty);
    }

    /**
     * Memformat detik menjadi format "M:SS".
     *
     * @param totalSeconds total detik
     * @return string dalam format "1:05"
     */
    private String formatTime(double totalSeconds) {
        if (totalSeconds < 0 || Double.isNaN(totalSeconds)) return "0:00";
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /** Memperbarui label volume dengan persentase. */
    private void updateVolumeLabel(double volume) {
        volumeLabel.setText((int) (volume * 100) + "%");
    }
}
