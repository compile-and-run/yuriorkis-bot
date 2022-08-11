package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.repositories.ToyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KinderService {

    private final ToyRepository toyRepository;

    public void addToy(Toy toy) {
        toyRepository.save(toy);
    }

    public Toy getRandomToy() {
        return toyRepository.getRandomToy();
    }
}
