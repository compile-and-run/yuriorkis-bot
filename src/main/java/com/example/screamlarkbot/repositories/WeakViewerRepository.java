package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.models.weak.WeakViewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeakViewerRepository extends JpaRepository<WeakViewer, Long> {
    Optional<WeakViewer> findByName(String username);
    void deleteByName(String username);
}
