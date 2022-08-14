package com.example.screamlarkbot.models.fight;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(exclude = "createdAt")
@RequiredArgsConstructor
public class DuelRequest {
    private final String requester;
    private final String opponent;
    private final LocalDateTime createdAt = LocalDateTime.now();
}
