package com.pocket_plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
}
