package com.example.screamlarkbot.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CoolDownException extends Exception {

    private final long minutesLeft;
}
