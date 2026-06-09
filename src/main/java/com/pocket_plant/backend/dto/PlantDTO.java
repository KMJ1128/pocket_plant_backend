package com.pocket_plant.backend.dto;

import com.pocket_plant.backend.entity.Plant;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantDTO {
    private Long id;
    private Long character_id;
    private String name;
    private String species;
    private String adoptDate; // 프론트와 YYYY-MM-DD 문자열로 통신
    private Integer age;      // 숫자로 처리
    private String personality;
    private String imageUri;  // 프론트의 imageUri 변수명과 맞춤
    private Boolean bookmarked;


    // Entity -> DTO 변환 (조회 시 사용)
    public static PlantDTO fromEntity(Plant plant) {
        return PlantDTO.builder()
                .id(plant.getId())
                .character_id(plant.getCharacter_id())
                .name(plant.getName())
                .species(plant.getSpecies())
                // LocalDate를 YYYY-MM-DD 문자열로 변환
                .adoptDate(plant.getAdoptDate() != null ? plant.getAdoptDate().toString() : null)
                .age(plant.getAge())
                .personality(plant.getPersonality())
                .bookmarked(plant.isBookmarked())
                .build();
    }

    // DTO -> Entity 변환 (등록/수정 시 사용)
    // User 객체는 Service 계층에서 주입
    public Plant toEntity() {
        return Plant.builder()
                .character_id(this.character_id)
                .name(this.name)
                .species(this.species)
                // YYYY-MM-DD 문자열을 LocalDate로 변환
                .adoptDate(this.adoptDate != null ? LocalDate.parse(this.adoptDate, DateTimeFormatter.ISO_DATE) : null)
                .age(this.age)
                .personality(this.personality)
                // bookmarked는 등록 시 기본 false로 설정됨
                .build();
    }
}