package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.repositories.ToyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KinderService {

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
}
