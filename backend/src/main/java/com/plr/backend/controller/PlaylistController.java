package com.plr.backend.controller;

import com.plr.backend.dto.PlaylistRequest;
import com.plr.backend.dto.PlaylistResponse;
import com.plr.backend.model.User;
import com.plr.backend.service.IPlaylistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistController {

    @Autowired
    private IPlaylistService playlistService;

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists(Authentication authentication) {
        List<PlaylistResponse> playlists = playlistService.getAllPlaylists(getUserId(authentication));
        return ResponseEntity.ok(playlists);
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody PlaylistRequest request,
            Authentication authentication) {
        PlaylistResponse playlist = playlistService.createPlaylist(request, getUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylistById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            PlaylistResponse playlist = playlistService.getPlaylistById(id, getUserId(authentication));
            return ResponseEntity.ok(playlist);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Playlist tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistRequest request,
            Authentication authentication) {
        try {
            PlaylistResponse updated = playlistService.updatePlaylist(id, request, getUserId(authentication));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Playlist tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            playlistService.deletePlaylist(id, getUserId(authentication));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Playlist tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
