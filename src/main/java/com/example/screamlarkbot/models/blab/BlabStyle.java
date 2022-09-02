package com.example.screamlarkbot.models.blab;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlabStyle {
    NO_STYLE(0),
    INSTRUCTION(24),
    RECIPE(25),
    WISDOM(11),
    STORY(6),
    WIKI(8),
    SYNOPSIS(9);

    private final int intro;
}
