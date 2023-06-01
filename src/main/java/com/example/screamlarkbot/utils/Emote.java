package com.example.screamlarkbot.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Emote {
    FROG_WAVE("FrogWave"),
    LIZARD_PLS("lizardPls"),
    VERY_JAM("VeryJam"),
    FEELS_WEAK_MAN("FeelsWeakMan"),
    PEEPO_CLAP("peepoClap"),
    OOOO("OOOO"),
    FIGHT("fight"),
    FIGHT2("fight2"),
    STREAML_SMASH("streamlSmash"),
    VERY_POG("VeryPog"),
    STARE("Stare"),
    MADGE_KNIFE("Madgeknife"),
    PEEPO_SMART("peepoSmart"),
    NOOOO("NOOOO");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
