package com.plr.frontend.service;

import com.plr.frontend.model.AudioTrack;
import com.plr.frontend.model.NoiseType;
import com.plr.frontend.model.Playlist;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
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

    public enum LoopMode { NONE, ALL, ONE }

    // ── State ──────────────────────────────────────────────────────────────

    private MediaPlayer     mediaPlayer;
    private final ObservableList<AudioTrack> playlist = FXCollections.observableArrayList();
    private int             currentIndex    = -1;
    private boolean         isPlaying       = false;
    private double          currentVolume   = 0.7;
    private NoiseType       activeNoiseType = null;
    private boolean         isNoiseMode     = false; // true = noise, false = playlist
    private LoopMode        loopMode        = LoopMode.NONE;
    private boolean         shuffle         = false;
    private Timeline        progressTimer;

    // ── Named Playlists ────────────────────────────────────────────────────

    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    // ── Callbacks ke Controller ────────────────────────────────────────────

    /** Dipanggil setiap kali progress berubah. (progress 0.0–1.0, currentSec, totalSec) */
    private Consumer<double[]>  onProgressUpdate;
    /** Dipanggil saat lagu berpindah atau dimulai. */
    private Consumer<String>    onTrackChanged;
    /** Dipanggil saat status play/pause berubah. */
    private Consumer<Boolean>   onPlayStateChanged;
    /** Dipanggil saat lagu selesai (untuk auto-next). */
    private Runnable            onTrackEnded;
    /** Dipanggil saat mode loop atau shuffle berubah. */
    private Runnable            onModeChanged;

    // ── Setter Callbacks ───────────────────────────────────────────────────

    public void setOnProgressUpdate(Consumer<double[]> cb)  { this.onProgressUpdate   = cb; }
    public void setOnTrackChanged(Consumer<String> cb)      { this.onTrackChanged      = cb; }
    public void setOnPlayStateChanged(Consumer<Boolean> cb) { this.onPlayStateChanged  = cb; }
    public void setOnTrackEnded(Runnable cb)                { this.onTrackEnded        = cb; }
    public void setOnModeChanged(Runnable cb)               { this.onModeChanged       = cb; }

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
        int next;
        if (shuffle) {
            List<Integer> indices = java.util.stream.IntStream.range(0, playlist.size())
                .boxed()
                .collect(java.util.stream.Collectors.toList());
            Collections.shuffle(indices);
            next = indices.get(0);
        } else {
            next = (currentIndex + 1) % playlist.size();
        }
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

    // ── Named Playlists ────────────────────────────────────────────────────

    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    public Playlist createPlaylist(String name, String description, java.util.List<File> files) {
        Playlist pl = new Playlist(name, description);
        for (File f : files) {
            pl.addTrack(new AudioTrack(f));
        }
        playlists.add(pl);
        return pl;
    }

    public Playlist getPlaylistById(String id) {
        for (Playlist p : playlists) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    /** Muat semua track dari playlist ke antrian dan putar track pertama. */
    public void playPlaylist(Playlist pl) {
        if (pl.getTracks().isEmpty()) return;
        playlist.setAll(pl.getTracks());
        playTrackAt(0);
    }

    /** Muat semua track dan putar secara acak. */
    public void playPlaylistShuffled(Playlist pl) {
        if (pl.getTracks().isEmpty()) return;
        playlist.setAll(pl.getTracks());
        java.util.List<Integer> indices = java.util.stream.IntStream.range(0, playlist.size())
            .boxed()
            .collect(java.util.stream.Collectors.toList());
        Collections.shuffle(indices);
        currentIndex = indices.get(0);
        playTrackAt(currentIndex);
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

    // ── Loop / Shuffle ─────────────────────────────────────────────────────

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    /** Cycles: NONE → ALL → ONE → NONE */
    public void toggleLoopMode() {
        loopMode = switch (loopMode) {
            case NONE -> LoopMode.ALL;
            case ALL  -> LoopMode.ONE;
            case ONE  -> LoopMode.NONE;
        };
        if (onModeChanged != null) onModeChanged.run();
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
        if (onModeChanged != null) onModeChanged.run();
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
            int cycles = (loop || (!loop && loopMode == LoopMode.ONE))
                ? MediaPlayer.INDEFINITE : 1;
            mediaPlayer.setCycleCount(cycles);

            // Perbarui progress setiap 1 detik via Timeline
            if (progressTimer != null) progressTimer.stop();
            progressTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (mediaPlayer == null) return;
                Duration total = mediaPlayer.getMedia().getDuration();
                Duration cur   = mediaPlayer.getCurrentTime();
                if (total == null || total.equals(Duration.UNKNOWN))
                    total = mediaPlayer.getTotalDuration();
                if (cur != null) {
                    double currentSec = cur.toSeconds();
                    double totalSec = (total != null && total.greaterThan(Duration.ZERO))
                        ? total.toSeconds() : 0;
                    if (totalSec > 0) {
                        notifyProgress(currentSec / totalSec, currentSec, totalSec);
                    }
                }
            }));
            progressTimer.setCycleCount(Timeline.INDEFINITE);

            // Ketika media siap: mulai timeline + notify
            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                if (total != null && total.greaterThan(Duration.ZERO)) {
                    notifyProgress(0, 0, total.toSeconds());
                }
                progressTimer.play();
                notifyTrackChanged(displayName);
                notifyPlayState(true);
            });

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
        if (progressTimer != null) { progressTimer.stop(); progressTimer = null; }
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
