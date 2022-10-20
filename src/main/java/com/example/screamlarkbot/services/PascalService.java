package com.example.screamlarkbot.services;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PascalService {

    private static final String TURBORIUM_ID = "407950106";
    private final TwitchClient twitchClient;

    public boolean checkByUsername(String username) {
        var list = twitchClient.getHelix().getUsers(null, null, List.of(username)).execute();
        if (list.getUsers().isEmpty()) {
            throw new NotFoundException();
        }
        var userId = list.getUsers().get(0).getId();
        return checkByUserId(userId);
    }

    public boolean checkByUserId(String userId) {
        if (TURBORIUM_ID.equals(userId)) return true;
        var list = twitchClient.getHelix().getFollowers(null, userId, TURBORIUM_ID, null, null).execute();
        return !list.getFollows().isEmpty();
    }
}
