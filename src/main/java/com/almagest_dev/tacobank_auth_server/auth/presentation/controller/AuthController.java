package com.almagest_dev.tacobank_auth_server.auth.presentation.controller;

import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.DuplicateEmailRequestDto;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.SignupRequestDTO;
import com.almagest_dev.tacobank_auth_server.auth.application.service.AuthService;
import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/members")
    public ResponseEntity<?> register(@RequestBody @Valid SignupRequestDTO requestDTO) {
        authService.registerMember(requestDTO);
        return ResponseEntity.ok(new AuthResponseDto("SUCCESS", "회원가입이 성공적으로 완료되었습니다!"));
    }

    /**
     * 이메일 중복 확인
     */
    @PostMapping("/email")
    public ResponseEntity<?> checkDuplicateEmail(@RequestBody @Valid DuplicateEmailRequestDto requestDto) {
        authService.checkDuplicateEmail(requestDto);
        return ResponseEntity.ok(new AuthResponseDto("SUCCESS", "사용 가능한 이메일 입니다."));
    }

}
