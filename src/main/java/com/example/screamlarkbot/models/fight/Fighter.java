package com.example.screamlarkbot.models.fight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Fighter {
    private final String username;
    private int hp = 100;
    private LocalDateTime lastPunch = LocalDateTime.now().minusMinutes(1);

    public void decreaseHp(int damage) {
        hp-=damage;
        if (hp < 0) {
            hp = 0;
        }
    }

    public void updateLastPunch() {
        lastPunch = LocalDateTime.now();
    }
}
