package com.plr.backend.service;

import com.plr.backend.dto.SongRequest;
import com.plr.backend.dto.SongResponse;

import java.util.List;

public interface ISongService {
    SongResponse addSong(Long playlistId, SongRequest request, Long userId);
    List<SongResponse> getSongsByPlaylist(Long playlistId, Long userId);
    SongResponse getSongById(Long playlistId, Long songId, Long userId);
    SongResponse updateSong(Long playlistId, Long songId, SongRequest request, Long userId);
    void deleteSong(Long playlistId, Long songId, Long userId);
    void reorderSongs(Long playlistId, List<Long> songIds, Long userId);
}
