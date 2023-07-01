package com.example.screamlarkbot.models.dancer;

import com.example.screamlarkbot.utils.Emote;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DanceEmotes {
    public static List<Emote> get() {
        return Arrays.asList(
            Emote.LIZARD_PLS,
            Emote.VERY_JAM,
            Emote.FORSEN_PLS,
            Emote.CRAB_PLS
        );
    }

    public static List<String> asStringList() {
        return get().stream().map(Emote::toString).collect(Collectors.toList());
    }
}
