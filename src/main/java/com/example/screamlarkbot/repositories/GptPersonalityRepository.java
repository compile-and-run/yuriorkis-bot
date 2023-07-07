package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.utils.Files;

import java.util.HashMap;

public class GptPersonalityRepository {
    private static final HashMap<String, String> personalitiesMap = new HashMap<>() {{
        put("саня", "sanya.txt");
        put("программист", "programmer.txt");
    }};

    public static boolean isValidPersonality(String value) {
        return personalitiesMap.containsKey(value);
    }

    public static String getPersonality(String key) {
        return personalitiesMap.get(key);
    }
}
