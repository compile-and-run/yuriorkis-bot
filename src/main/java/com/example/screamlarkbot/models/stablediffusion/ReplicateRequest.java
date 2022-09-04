package com.example.screamlarkbot.models.stablediffusion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@AllArgsConstructor
@Data
public class ReplicateRequest {
    private String version;
    private ReplicateInput input;

    @Builder
    @Data
    public static class ReplicateInput {
        private String prompt;
        @Builder.Default
        private int width = 512;
        @Builder.Default
        private int height = 512;
        @Builder.Default
        private long seed = Instant.now().toEpochMilli();
        @Builder.Default
        @JsonProperty("num_outputs")
        private String numOutputs = "1";
        @Builder.Default
        @JsonProperty("num_inference_steps")
        private int numInferenceSteps = 50;
        @Builder.Default
        @JsonProperty("guidance_scale")
        private double guidanceScale = 7.5;
        @Builder.Default
        @JsonProperty("prompt_strength")
        private double promptStrength = 0.8;
    }
}
