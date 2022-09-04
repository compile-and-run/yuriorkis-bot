package com.example.screamlarkbot.models.stablediffusion;

import lombok.Data;

@Data
public class ReplicateResponse {

    private ReplicateUrls urls;
    private String[] output;

    @Data
    public static class ReplicateUrls {
        private String get;
        private String cancel;
    }
}
