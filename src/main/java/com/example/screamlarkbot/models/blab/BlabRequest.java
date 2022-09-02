package com.example.screamlarkbot.models.blab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BlabRequest {
    @Builder.Default
    private int filter = 1;
    @Builder.Default
    private int intro = 0;
    private String query;
}
