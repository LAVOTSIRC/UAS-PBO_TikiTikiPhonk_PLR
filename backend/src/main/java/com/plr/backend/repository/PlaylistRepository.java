package com.plr.backend.repository;

import com.plr.backend.model.Playlist;
import com.plr.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
    Optional<Playlist> findByIdAndUser(Long id, User user);
}
