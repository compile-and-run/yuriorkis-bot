package com.example.screamlarkbot.utils;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TwitchHelper {
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    public static LocalDateTime getCreatedAt(TwitchClient twitchClient, String userId) {
        try {
            UserList userList = twitchClient.getHelix().getUsers(null, List.of(userId), null).execute();
            User user = userList.getUsers().get(0);
            return LocalDateTime.ofInstant(user.getCreatedAt(), ZONE_ID);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
