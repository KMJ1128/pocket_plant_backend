package com.pocket_plant.backend.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MsgEntity {
    private String message;
    private Object data; // 응답 본문에 담길 실제 데이터
}
