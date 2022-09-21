package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.models.dancer.DancerViewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DancerViewerRepository extends JpaRepository<DancerViewer, Long> {
    Optional<DancerViewer> findByName(String username);
}
