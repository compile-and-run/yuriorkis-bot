package com.example.screamlarkbot.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Emote {
    FROG_WAVE("FrogWave"),
    LIZARD_PLS("lizardPls"),
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
    NOOOO("NOOOO"),
    VERY_JAM("VeryJam"),
    FORSEN_PLS("forsenPls"),
    CRAB_PLS("crabPls");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
