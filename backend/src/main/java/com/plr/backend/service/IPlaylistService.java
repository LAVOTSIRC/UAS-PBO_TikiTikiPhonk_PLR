package com.plr.backend.service;

import com.plr.backend.dto.PlaylistRequest;
import com.plr.backend.dto.PlaylistResponse;

import java.util.List;

public interface IPlaylistService {
    PlaylistResponse createPlaylist(PlaylistRequest request, Long userId);
    List<PlaylistResponse> getAllPlaylists(Long userId);
    PlaylistResponse getPlaylistById(Long id, Long userId);
    PlaylistResponse updatePlaylist(Long id, PlaylistRequest request, Long userId);
    void deletePlaylist(Long id, Long userId);
}
