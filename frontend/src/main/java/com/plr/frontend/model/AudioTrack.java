package com.plr.frontend.model;

import java.io.File;

/**
 * Model yang merepresentasikan satu track audio dalam playlist.
 * Menyimpan informasi file dan metadata tampilan.
 */
public class AudioTrack {

    private final File file;
    private final String displayName;
    private final String uri;
    private double durationSeconds;
    private Long backendId;

    public AudioTrack(File file) {
        this.file = file;
        this.uri = file.toURI().toString();
        // Hilangkan ekstensi untuk tampilan yang lebih bersih
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        this.displayName = (dotIndex > 0) ? name.substring(0, dotIndex) : name;
    }

    /** Konstruktor khusus untuk bundled audio (resources). */
    public AudioTrack(String resourceUri, String displayName) {
        this.file = null;
        this.uri = resourceUri;
        this.displayName = displayName;
    }

    public File getFile() {
        return file;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUri() {
        return uri;
    }

    public double getDurationSeconds() { return durationSeconds; }

    public void setDurationSeconds(double durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getBackendId() { return backendId; }

    public void setBackendId(Long backendId) { this.backendId = backendId; }

    /** Dipakai ListView agar langsung tampil nama yang benar. */
    @Override
    public String toString() {
        return displayName;
    }
}
