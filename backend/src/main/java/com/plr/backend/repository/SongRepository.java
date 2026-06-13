package com.plr.backend.repository;

import com.plr.backend.model.Playlist;
import com.plr.backend.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByPlaylist(Playlist playlist);
    List<Song> findByPlaylistId(Long playlistId);
    Optional<Song> findByIdAndPlaylistId(Long id, Long playlistId);
    Optional<Song> findByIdAndPlaylistIdAndPlaylistUser_Id(Long id, Long playlistId, Long userId);
}
