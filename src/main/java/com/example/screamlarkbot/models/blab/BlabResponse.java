package com.example.screamlarkbot.models.blab;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BlabResponse {
    private String query;
    private String text;
}
