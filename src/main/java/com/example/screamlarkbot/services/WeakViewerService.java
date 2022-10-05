package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.weak.WeakViewer;
import com.example.screamlarkbot.repositories.WeakViewerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeakViewerService {

    private final WeakViewerRepository repository;

    @Transactional
    public void incrementScore(String username) {
        repository.findByName(username)
                .ifPresentOrElse(
                        user -> user.setScore(user.getScore() + 1),
                        () -> repository.save(WeakViewer.builder()
                                .name(username)
                                .score(1)
                                .build())
                );
    }

    @Transactional
    public void setScore(String username, Integer score) {
        repository.findByName(username)
                .ifPresentOrElse(
                        user -> user.setScore(score),
                        () -> repository.save(WeakViewer.builder()
                                .name(username)
                                .score(score)
                                .build())
                );
    }

    @Transactional
    public void delete(String username) {
        repository.findByName(username)
                .ifPresentOrElse(
                        user -> repository.delete(user),
                        () -> {}
                );
    }

    public List<WeakViewer> getTop() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "score"))
                .stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    public Optional<WeakViewer> findByName(String username) {
        return repository.findByName(username);
    }
}
