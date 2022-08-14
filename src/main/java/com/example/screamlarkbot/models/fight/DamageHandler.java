package com.example.screamlarkbot.models.fight;

@FunctionalInterface
public interface DamageHandler {
    void onDamage(Fighter puncher, Fighter fighter, int damage);
}
