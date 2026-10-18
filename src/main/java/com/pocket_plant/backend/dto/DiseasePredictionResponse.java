package com.pocket_plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DiseasePredictionResponse {
    @JsonProperty("disease_symptom")
    private String diseaseSymptom;
    private Double confidence;
    private String status;
    private String observation;
    @JsonProperty("source_label")
    private String sourceLabel;
    @JsonProperty("model_version")
    private String modelVersion;
    private String species;
    private String features;
    private String actions;
    @JsonProperty("analysis_text")
    private String analysisText;
    @JsonProperty("score_kind")
    private String scoreKind;
    @JsonProperty("normal_result_blocked")
    private Boolean normalResultBlocked;
    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private String name;
        private String features;
        private String actions;
        private String source;
        @JsonProperty("source_label")
        private String sourceLabel;
        @JsonProperty("comparison_score")
        private Double comparisonScore;
    }
}
