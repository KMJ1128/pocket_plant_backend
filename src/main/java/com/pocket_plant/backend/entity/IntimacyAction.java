package com.pocket_plant.backend.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntimacyAction {
    DAILY_LOGIN(1, true),       // 매일 접속: +1점 (하루 1회)
    WATERING(3, false),         // 물 주기: +3점
    AI_CHAT(2, true),           // AI 대화: +2점 (하루 1회)
    DIARY_WRITE(3, false),      // 일지 작성: +3점
    CARE_ATTENTION(1, false),   // 식물 관심 주기: +1점
    DISEASE_PREDICT(4, false);  // 식물 질병 진단하기: +4점

    private final int score;
    private final boolean oncePerDay; // 하루 1회 제한 여부
}