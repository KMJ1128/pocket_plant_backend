package com.example.demo.controller;

import com.example.demo.dto.MemberJoinDTO;
import com.example.demo.service.MemberService;
import com.example.demo.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public String register(MemberJoinDTO memberJoinDTO) {


        memberService.save(memberJoinDTO);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "home"; // home.html을 보여줌
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("memberJoinDTO", new MemberJoinDTO());
        return "register";
    }
}