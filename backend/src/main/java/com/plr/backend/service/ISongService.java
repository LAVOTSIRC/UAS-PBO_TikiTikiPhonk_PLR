package com.plr.backend.service;

import com.plr.backend.dto.SongRequest;
import com.plr.backend.dto.SongResponse;

import java.util.List;

public interface ISongService {
    SongResponse addSong(Long playlistId, SongRequest request, String username);
    List<SongResponse> getSongsByPlaylist(Long playlistId, String username);
    SongResponse getSongById(Long playlistId, Long songId, String username);
    SongResponse updateSong(Long playlistId, Long songId, SongRequest request, String username);
    void deleteSong(Long playlistId, Long songId, String username);
    void reorderSongs(Long playlistId, List<Long> songIds, String username);
}
