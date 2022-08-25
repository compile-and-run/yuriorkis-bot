package com.example.screamlarkbot.services;

import com.example.screamlarkbot.exception.CoolDownException;
import com.example.screamlarkbot.exception.ToyExistsException;
import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.repositories.ToyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KinderService {

    private static final int MAX_TOY_NUMBER = 100;
    private static final int ADDING_COOL_DOWN = 15;
    private static final int GETTING_COOL_DOWN = 5;
    private final ToyRepository toyRepository;

    private final Map<String, LocalDateTime> addingCoolDowns = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> gettingCoolDowns = new ConcurrentHashMap<>();

    public void addToy(Toy toy) throws CoolDownException, ToyExistsException {
        try {
            var username = toy.getOwner();
            var lastAdding = addingCoolDowns.getOrDefault(username, LocalDateTime.now().minusDays(1));
            var duration = Duration.between(lastAdding, LocalDateTime.now()).toMinutes();
            if (duration < ADDING_COOL_DOWN) {
                throw new CoolDownException(ADDING_COOL_DOWN - duration);
            }
            toyRepository.save(toy);
            addingCoolDowns.put(toy.getOwner(), toy.getCreatedAt());

        } catch (DataIntegrityViolationException e) {
            throw new ToyExistsException();
        }
    }

    public Toy getRandomToy(String username) throws CoolDownException {
        var lastGetting = gettingCoolDowns.getOrDefault(username, LocalDateTime.now().minusDays(1));
        var duration = Duration.between(lastGetting, LocalDateTime.now()).toMinutes();
        if (duration < GETTING_COOL_DOWN) {
            throw new CoolDownException(GETTING_COOL_DOWN - duration);
        }
        gettingCoolDowns.put(username, LocalDateTime.now());
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
