package com.pocket_plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiseasePredictionResponse {
    @JsonProperty("disease_symptom")
    private String diseaseSymptom;
    private Double confidence;
}