package com.pocket_plant.backend.controller;

import org.springframework.stereotype.Controller; // 이 import가 필요합니다.
import org.springframework.web.bind.annotation.GetMapping;

@Controller // @RestController 대신 @Controller 사용
public class WebController {

    @GetMapping(value = "/{path:[^\\.]*}") // 모든 경로 요청을 받도록 수정
    public String redirect() {
        return "forward:/index.html";
    }
}