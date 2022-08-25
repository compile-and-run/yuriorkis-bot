package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.repositories.ToyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KinderService {

    private static final int MAX_TOY_NUMBER = 100;
    private final ToyRepository toyRepository;

    public boolean addToy(Toy toy) {
        try {
            toyRepository.save(toy);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public Toy getRandomToy() {
        return toyRepository.getRandomToy();
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public synchronized void removeOldToys() {
        long count = toyRepository.count();
        if (count > MAX_TOY_NUMBER) {
            long limit = count - MAX_TOY_NUMBER;
            log.info("Deleting old toys. Size={}", limit);
            toyRepository.deleteOldToys(limit);
        }
    }
}
