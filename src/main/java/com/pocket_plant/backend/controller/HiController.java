package com.pocket_plant.backend.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HiController {

    //이것은 아무의미없음 그냥 서버가 잘 돌아가는지 확인하기 위한 테스트용
    @GetMapping("/")
    public String hi() {
        return "hi";
    }
}
