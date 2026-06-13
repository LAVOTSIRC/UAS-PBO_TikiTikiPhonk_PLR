package com.plr.frontend.service;

import com.plr.frontend.model.AudioTrack;
import com.plr.frontend.model.NoiseType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.function.Consumer;

/**
 * AudioPlayerService — Service Layer untuk semua logika audio.
 *
 * Tanggung jawab:
 * - Manajemen MediaPlayer (create, play, pause, stop, dispose)
 * - Manajemen playlist (tambah, hapus, navigasi prev/next)
 * - Looping noise bawaan
 * - Update progress ke callback
 * - Manajemen volume
 *
 * Controller TIDAK boleh memanggil MediaPlayer secara langsung;
 * semua interaksi audio harus melalui service ini.
 */
public class AudioPlayerService {

    // ── State ──────────────────────────────────────────────────────────────

    private MediaPlayer     mediaPlayer;
    private final ObservableList<AudioTrack> playlist = FXCollections.observableArrayList();
    private int             currentIndex    = -1;
    private boolean         isPlaying       = false;
    private double          currentVolume   = 0.7;
    private NoiseType       activeNoiseType = null;
    private boolean         isNoiseMode     = false; // true = noise, false = playlist

    // ── Callbacks ke Controller ────────────────────────────────────────────

    /** Dipanggil setiap kali progress berubah. (progress 0.0–1.0, currentSec, totalSec) */
    private Consumer<double[]>  onProgressUpdate;
    /** Dipanggil saat lagu berpindah atau dimulai. */
    private Consumer<String>    onTrackChanged;
    /** Dipanggil saat status play/pause berubah. */
    private Consumer<Boolean>   onPlayStateChanged;
    /** Dipanggil saat lagu selesai (untuk auto-next). */
    private Runnable            onTrackEnded;

    // ── Setter Callbacks ───────────────────────────────────────────────────

    public void setOnProgressUpdate(Consumer<double[]> cb)  { this.onProgressUpdate   = cb; }
    public void setOnTrackChanged(Consumer<String> cb)      { this.onTrackChanged      = cb; }
    public void setOnPlayStateChanged(Consumer<Boolean> cb) { this.onPlayStateChanged  = cb; }
    public void setOnTrackEnded(Runnable cb)                { this.onTrackEnded        = cb; }

    // ── Playlist Management ────────────────────────────────────────────────

    /** Menambahkan track ke playlist dan mengembalikan daftar terupdate. */
    public ObservableList<AudioTrack> addTrack(AudioTrack track) {
        playlist.add(track);
        return playlist;
    }

    /** Mengembalikan ObservableList playlist (digunakan langsung untuk binding ListView). */
    public ObservableList<AudioTrack> getPlaylist() {
        return playlist;
    }

    /** Memulai pemutaran track pada index tertentu di playlist. */
    public void playTrackAt(int index) {
        if (index < 0 || index >= playlist.size()) return;
        currentIndex = index;
        isNoiseMode  = false;
        activeNoiseType = null;
        playInternal(playlist.get(index).getUri(), playlist.get(index).getDisplayName(), false);
    }

    /** Lanjut ke track berikutnya di playlist. Wrap-around ke awal jika sudah di akhir. */
    public void nextTrack() {
        if (playlist.isEmpty()) return;
        int next = (currentIndex + 1) % playlist.size();
        playTrackAt(next);
    }

    /** Kembali ke track sebelumnya. Wrap-around ke akhir jika sudah di awal. */
    public void previousTrack() {
        if (playlist.isEmpty()) return;
        int prev = (currentIndex - 1 + playlist.size()) % playlist.size();
        playTrackAt(prev);
    }

    /** Mengembalikan index track yang sedang diputar (-1 jika tidak ada). */
    public int getCurrentIndex() {
        return currentIndex;
    }

    // ── Noise Playback ─────────────────────────────────────────────────────

    /**
     * Memutar noise bawaan dengan looping tanpa henti.
     * Jika noise yang sama sudah aktif, toggle pause/play.
     * Jika noise berbeda, ganti ke noise baru.
     */
    public void playNoise(NoiseType type) {
        if (isNoiseMode && activeNoiseType == type) {
            // Toggle play/pause untuk noise yang sama
            togglePlayPause();
            return;
        }

        activeNoiseType = type;
        isNoiseMode     = true;
        currentIndex    = -1;

        URL resourceUrl = getClass().getClassLoader().getResource(type.getResourcePath());
        if (resourceUrl == null) {
            notifyTrackChanged("⚠ File tidak ditemukan: " + type.getResourcePath());
            return;
        }

        playInternal(resourceUrl.toExternalForm(), type.getFullLabel(), true);
    }

    /** Mengembalikan tipe noise yang sedang aktif, atau null jika tidak ada. */
    public NoiseType getActiveNoiseType() {
        return activeNoiseType;
    }

    public boolean isNoiseMode() {
        return isNoiseMode;
    }

    // ── Playback Controls ──────────────────────────────────────────────────

    /** Melanjutkan pemutaran. */
    public void play() {
        if (mediaPlayer == null) return;
        mediaPlayer.play();
        isPlaying = true;
        notifyPlayState(true);
    }

    /** Menjeda pemutaran. */
    public void pause() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        isPlaying = false;
        notifyPlayState(false);
    }

    /** Toggle antara play dan pause. */
    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Menghentikan pemutaran sepenuhnya, me-reset progress,
     * dan membersihkan state noise jika sedang dalam mode noise.
     */
    public void stop() {
        if (mediaPlayer == null) return;
        mediaPlayer.stop();
        isPlaying    = false;
        isNoiseMode  = false;
        activeNoiseType = null;
        notifyPlayState(false);
        notifyProgress(0, 0, 0);
        notifyTrackChanged("Tidak ada audio");
    }

    /** Menetapkan volume (0.0 – 1.0). */
    public void setVolume(double volume) {
        this.currentVolume = Math.max(0.0, Math.min(1.0, volume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.currentVolume);
        }
    }

    public double getCurrentVolume() {
        return currentVolume;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    /**
     * Harus dipanggil saat panel ditutup / aplikasi keluar
     * untuk melepaskan resource native MediaPlayer.
     */
    public void dispose() {
        disposeCurrentPlayer();
    }

    // ── Internal Helpers ───────────────────────────────────────────────────

    /**
     * Inti dari semua pemutaran audio.
     *
     * @param uri         URI media yang akan dimainkan
     * @param displayName Nama yang ditampilkan di label "Now Playing"
     * @param loop        true = loop tak terbatas (noise), false = putar sekali (playlist)
     */
    private void playInternal(String uri, String displayName, boolean loop) {
        disposeCurrentPlayer();

        try {
            Media media = new Media(uri);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(currentVolume);
            mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);

            // Update progress bar secara real-time
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                Duration total = media.getDuration();
                if (total != null && total.greaterThan(Duration.ZERO)) {
                    double progress   = newTime.toSeconds() / total.toSeconds();
                    double currentSec = newTime.toSeconds();
                    double totalSec   = total.toSeconds();
                    Platform.runLater(() -> notifyProgress(progress, currentSec, totalSec));
                }
            });

            // Ketika media siap, aktifkan kontrol dan umumkan track
            mediaPlayer.setOnReady(() -> Platform.runLater(() -> {
                notifyTrackChanged(displayName);
                notifyPlayState(true);
            }));

            // Saat track selesai (mode playlist), notifikasi controller
            mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
                if (!loop) {
                    isPlaying = false;
                    notifyPlayState(false);
                    if (onTrackEnded != null) onTrackEnded.run();
                }
            }));

            mediaPlayer.setOnError(() -> {
                String errMsg = mediaPlayer.getError() != null
                    ? mediaPlayer.getError().getMessage()
                    : "Unknown error";
                Platform.runLater(() -> notifyTrackChanged("⚠ Error: " + errMsg));
            });

            mediaPlayer.play();
            isPlaying = true;

        } catch (Exception e) {
            notifyTrackChanged("⚠ Tidak dapat memutar: " + displayName);
        }
    }

    /** Menghentikan dan membuang MediaPlayer yang sedang aktif. */
    private void disposeCurrentPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        isPlaying = false;
    }

    // ── Notification Helpers ───────────────────────────────────────────────

    private void notifyProgress(double progress, double currentSec, double totalSec) {
        if (onProgressUpdate != null) {
            onProgressUpdate.accept(new double[]{ progress, currentSec, totalSec });
        }
    }

    private void notifyTrackChanged(String name) {
        if (onTrackChanged != null) {
            onTrackChanged.accept(name);
        }
    }

    private void notifyPlayState(boolean playing) {
        if (onPlayStateChanged != null) {
            onPlayStateChanged.accept(playing);
        }
    }
}
