package com.example.screamlarkbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Messages {

    public static String reply(String username, String message) {
        return "@" + username + " " + message;
    }
}
