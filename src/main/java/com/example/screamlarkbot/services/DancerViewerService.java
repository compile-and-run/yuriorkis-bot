package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.dancer.DancerViewer;
import com.example.screamlarkbot.repositories.DancerViewerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DancerViewerService {

    private final DancerViewerRepository repository;

    @Transactional
    public void incrementScore(String username) {
        repository.findByName(username)
                .ifPresentOrElse(
                        user -> user.setScore(user.getScore() + 1),
                        () -> repository.save(DancerViewer.builder()
                                .name(username)
                                .score(1)
                                .build())
                );
    }

    public List<DancerViewer> getTop() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "score"))
                .stream()
                .limit(5)
                .collect(Collectors.toList());
    }
}
