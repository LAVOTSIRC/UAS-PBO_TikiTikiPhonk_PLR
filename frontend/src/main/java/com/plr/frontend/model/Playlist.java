package com.plr.frontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class Playlist {

    private final String id;
    private final String name;
    private final String description;
    private final ObservableList<AudioTrack> tracks = FXCollections.observableArrayList();
    private Long backendId;

    public Playlist(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ObservableList<AudioTrack> getTracks() { return tracks; }
    public Long getBackendId() { return backendId; }
    public void setBackendId(Long backendId) { this.backendId = backendId; }

    public int getTrackCount() { return tracks.size(); }

    public void addTrack(AudioTrack track) { tracks.add(track); }
    public void addAllTracks(java.util.List<AudioTrack> newTracks) { tracks.addAll(newTracks); }

    @Override
    public String toString() {
        return name + " (" + tracks.size() + " lagu)";
    }
}
