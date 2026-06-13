package com.plr.backend.service;

import com.plr.backend.dto.PlaylistRequest;
import com.plr.backend.dto.PlaylistResponse;

import java.util.List;

public interface IPlaylistService {
    PlaylistResponse createPlaylist(PlaylistRequest request, String username);
    List<PlaylistResponse> getAllPlaylists(String username);
    PlaylistResponse getPlaylistById(Long id, String username);
    PlaylistResponse updatePlaylist(Long id, PlaylistRequest request, String username);
    void deletePlaylist(Long id, String username);
}
