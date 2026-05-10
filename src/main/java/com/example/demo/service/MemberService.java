package com.example.demo.service;

import com.example.demo.dto.MemberJoinDTO;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void save(MemberJoinDTO dto) {
        Member member = new Member();
        member.setEmail(dto.getEmail());

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        member.setPassword(encodedPassword);

        member.setName(dto.getName());
        member.setRole("ROLE_USER");

        memberRepository.save(member);
    }
}