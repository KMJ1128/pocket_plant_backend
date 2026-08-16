package com.pocket_plant.backend.dto;

import com.pocket_plant.backend.entity.Plant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantDTO {

    private Long id;
    private Long character_id;
    private String name;
    private String species;
    private String adoptDate;
    private Integer age;
    private String personality;
    private String imageUri;

    private String macAddress;
    private Boolean bookmarked;

    public static PlantDTO fromEntity(Plant plant) {
        return PlantDTO.builder()
                .id(plant.getId())
                .character_id(plant.getCharacter_id())
                .name(plant.getName())
                .species(plant.getSpecies())
                .adoptDate(
                        plant.getAdoptDate() == null
                                ? null
                                : plant.getAdoptDate().toString()
                )
                .age(plant.getAge())
                .personality(plant.getPersonality())
                .imageUri(plant.getImageUrl())
                .macAddress(plant.getMacAddress())
                .bookmarked(plant.isBookmarked())
                .build();
    }

    public Plant toEntity() {
        return Plant.builder()
                .character_id(character_id)
                .name(name)
                .species(species)
                .adoptDate(parseAdoptDate())
                .age(age)
                .personality(personality)
                .imageUrl(emptyToNull(imageUri))
                .macAddress(emptyToNull(macAddress))
                .bookmarked(false)
                .build();
    }

    public LocalDate parseAdoptDate() {
        if (adoptDate == null || adoptDate.isBlank()) {
            return null;
        }

        return LocalDate.parse(
                adoptDate,
                DateTimeFormatter.ISO_LOCAL_DATE
        );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}