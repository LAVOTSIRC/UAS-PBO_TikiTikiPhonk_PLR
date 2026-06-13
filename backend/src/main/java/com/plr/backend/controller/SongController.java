package com.plr.backend.controller;

import com.plr.backend.dto.SongRequest;
import com.plr.backend.dto.SongResponse;
import com.plr.backend.service.ISongService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists/{playlistId}/songs")
@CrossOrigin(origins = "*")
public class SongController {

    private static final Logger log = LoggerFactory.getLogger(SongController.class);

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
    public ResponseEntity<?> deleteSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            Authentication authentication) {
        log.info("DELETE song request: playlistId={}, songId={}, user={}",
            playlistId, songId, authentication.getName());
        try {
            songService.deleteSong(playlistId, songId, authentication.getName());
            log.info("Song deleted successfully: songId={}, playlistId={}", songId, playlistId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            log.error("Failed to delete song: songId={}, playlistId={}, error={}", songId, playlistId, msg);
            if (msg != null && msg.contains("Lagu tidak ditemukan")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            }
            return ResponseEntity.internalServerError().body(Map.of("error", msg != null ? msg : "Internal error"));
        }
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderSongs(
            @PathVariable Long playlistId,
            @RequestBody Map<String, List<Long>> body,
            Authentication authentication) {
        try {
            songService.reorderSongs(playlistId, body.get("songIds"), authentication.getName());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
