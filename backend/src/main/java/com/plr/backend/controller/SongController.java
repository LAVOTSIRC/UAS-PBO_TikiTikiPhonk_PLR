package com.plr.backend.controller;

import com.plr.backend.dto.SongRequest;
import com.plr.backend.dto.SongResponse;
import com.plr.backend.service.ISongService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists/{playlistId}/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private ISongService songService;

    @GetMapping
    public ResponseEntity<List<SongResponse>> getSongsByPlaylist(
            @PathVariable Long playlistId,
            Authentication authentication) {
        List<SongResponse> songs = songService.getSongsByPlaylist(playlistId, authentication.getName());
        return ResponseEntity.ok(songs);
    }

    @PostMapping
    public ResponseEntity<SongResponse> addSong(
            @PathVariable Long playlistId,
            @Valid @RequestBody SongRequest request,
            Authentication authentication) {
        SongResponse song = songService.addSong(playlistId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(song);
    }

    @GetMapping("/{songId}")
    public ResponseEntity<?> getSongById(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            Authentication authentication) {
        try {
            SongResponse song = songService.getSongById(playlistId, songId, authentication.getName());
            return ResponseEntity.ok(song);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Lagu tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    @PutMapping("/{songId}")
    public ResponseEntity<?> updateSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @Valid @RequestBody SongRequest request,
            Authentication authentication) {
        try {
            SongResponse updated = songService.updateSong(playlistId, songId, request, authentication.getName());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Lagu tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> deleteSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            Authentication authentication) {
        try {
            songService.deleteSong(playlistId, songId, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Lagu tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}
