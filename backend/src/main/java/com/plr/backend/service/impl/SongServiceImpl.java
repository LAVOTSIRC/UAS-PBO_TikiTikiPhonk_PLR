package com.plr.backend.service.impl;

import com.plr.backend.dto.SongRequest;
import com.plr.backend.dto.SongResponse;
import com.plr.backend.model.Playlist;
import com.plr.backend.model.Song;
import com.plr.backend.model.User;
import com.plr.backend.repository.PlaylistRepository;
import com.plr.backend.repository.SongRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.ISongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SongServiceImpl implements ISongService {

    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public SongResponse addSong(Long playlistId, SongRequest request, String username) {
        Playlist playlist = findPlaylist(playlistId, username);
        int nextOrder = songRepository.findByPlaylistId(playlistId).size();
        Song song = new Song();
        song.setTitle(request.getTitle());
        song.setFilePath(request.getFilePath());
        song.setDurationSeconds(request.getDurationSeconds());
        song.setFileSize(request.getFileSize());
        song.setSortOrder(nextOrder);
        song.setPlaylist(playlist);
        Song saved = songRepository.save(song);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByPlaylist(Long playlistId, String username) {
        findPlaylist(playlistId, username);
        return songRepository.findByPlaylistId(playlistId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SongResponse getSongById(Long playlistId, Long songId, String username) {
        Song song = findSong(playlistId, songId, username);
        return toResponse(song);
    }

    @Override
    public SongResponse updateSong(Long playlistId, Long songId, SongRequest request, String username) {
        Song song = findSong(playlistId, songId, username);
        song.setTitle(request.getTitle());
        song.setFilePath(request.getFilePath());
        song.setDurationSeconds(request.getDurationSeconds());
        song.setFileSize(request.getFileSize());
        Song updated = songRepository.save(song);
        return toResponse(updated);
    }

    @Override
    public void deleteSong(Long playlistId, Long songId, String username) {
        Song song = findSong(playlistId, songId, username);
        songRepository.delete(song);
    }

    @Override
    public void reorderSongs(Long playlistId, List<Long> songIds, String username) {
        // Verify playlist ownership
        findPlaylist(playlistId, username);
        for (int i = 0; i < songIds.size(); i++) {
            Long sid = songIds.get(i);
            Song song = songRepository.findByIdAndPlaylistId(sid, playlistId)
                .orElseThrow(() -> new RuntimeException("Lagu tidak ditemukan: " + sid));
            song.setSortOrder(i);
            songRepository.save(song);
        }
    }

    private Song findSong(Long playlistId, Long songId, String username) {
        // First verify the playlist belongs to this user (security check)
        findPlaylist(playlistId, username);
        log.debug("findSong: songId={}, playlistId={}, username={}", songId, playlistId, username);
        // Then find the song by id+playlistId (simpler query, ownership already verified via playlist)
        return songRepository.findByIdAndPlaylistId(songId, playlistId)
            .orElseThrow(() -> new RuntimeException("Lagu tidak ditemukan dengan ID: " + songId + " di playlist: " + playlistId));
    }

    private Playlist findPlaylist(Long playlistId, String username) {
        User user = findUser(username);
        return playlistRepository.findByIdAndUser(playlistId, user)
            .orElseThrow(() -> new RuntimeException("Playlist tidak ditemukan dengan ID: " + playlistId));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    private SongResponse toResponse(Song song) {
        SongResponse response = new SongResponse();
        response.setId(song.getId());
        response.setTitle(song.getTitle());
        response.setFilePath(song.getFilePath());
        response.setDurationSeconds(song.getDurationSeconds());
        response.setFileSize(song.getFileSize());
        response.setSortOrder(song.getSortOrder());
        response.setPlaylistId(song.getPlaylist().getId());
        response.setPlaylistName(song.getPlaylist().getName());
        response.setCreatedAt(song.getCreatedAt());
        response.setUpdatedAt(song.getUpdatedAt());
        return response;
    }
}
