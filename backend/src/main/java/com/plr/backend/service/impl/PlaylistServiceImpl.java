package com.plr.backend.service.impl;

import com.plr.backend.dto.PlaylistRequest;
import com.plr.backend.dto.PlaylistResponse;
import com.plr.backend.dto.SongResponse;
import com.plr.backend.model.Playlist;
import com.plr.backend.model.User;
import com.plr.backend.repository.PlaylistRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.IPlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistServiceImpl implements IPlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PlaylistResponse createPlaylist(PlaylistRequest request, String username) {
        User user = findUser(username);
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setUser(user);
        Playlist saved = playlistRepository.save(playlist);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getAllPlaylists(String username) {
        User user = findUser(username);
        return playlistRepository.findByUser(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long id, String username) {
        Playlist playlist = findPlaylist(id, username);
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse updatePlaylist(Long id, PlaylistRequest request, String username) {
        Playlist playlist = findPlaylist(id, username);
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        Playlist updated = playlistRepository.save(playlist);
        return toResponse(updated);
    }

    @Override
    public void deletePlaylist(Long id, String username) {
        Playlist playlist = findPlaylist(id, username);
        playlistRepository.delete(playlist);
    }

    private Playlist findPlaylist(Long id, String username) {
        User user = findUser(username);
        return playlistRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Playlist tidak ditemukan dengan ID: " + id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();
        response.setId(playlist.getId());
        response.setName(playlist.getName());
        response.setDescription(playlist.getDescription());
        response.setSongCount(playlist.getSongs().size());
        response.setSongs(playlist.getSongs().stream().map(song -> {
            SongResponse sr = new SongResponse();
            sr.setId(song.getId());
            sr.setTitle(song.getTitle());
            sr.setFilePath(song.getFilePath());
            sr.setDurationSeconds(song.getDurationSeconds());
            sr.setFileSize(song.getFileSize());
            sr.setPlaylistId(playlist.getId());
            sr.setPlaylistName(playlist.getName());
            sr.setCreatedAt(song.getCreatedAt());
            sr.setUpdatedAt(song.getUpdatedAt());
            return sr;
        }).collect(Collectors.toList()));
        response.setCreatedAt(playlist.getCreatedAt());
        response.setUpdatedAt(playlist.getUpdatedAt());
        return response;
    }
}
