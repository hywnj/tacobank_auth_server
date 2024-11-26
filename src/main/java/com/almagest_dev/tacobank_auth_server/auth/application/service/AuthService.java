package com.almagest_dev.tacobank_auth_server.auth.application.service;

import com.almagest_dev.tacobank_auth_server.auth.application.dto.SignupRequestDTO;
import com.almagest_dev.tacobank_auth_server.auth.domain.model.Member;
import com.almagest_dev.tacobank_auth_server.auth.domain.model.Role;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.MemberRepository;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String ALLOWED_SPECIAL_CHARACTERS = "!@_";

    public void registerMember(SignupRequestDTO requestDTO) {
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        if (memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")) { // 삭제된 계정이 아닌데 동일 전화번호가 있는 경우
            throw new IllegalArgumentException("해당 전화번호로 이미 등록된 계정이 있습니다. 로그인 해주세요.");
        }

        // 비밀번호 유효성 검증
        validatePassword(requestDTO.getPassword(), 8, requestDTO.getBirth(), requestDTO.getTel());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        // Role 세팅
        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("시스템 오류가 발생했습니다. 관리자에게 문의하세요."));

        // 멤버 저장
        Member member = Member.createMember(requestDTO.getEmail(), encodedPassword, requestDTO.getName(), requestDTO.getBirth(), requestDTO.getTel(), role);
        memberRepository.save(member);
    }

    /**
     * 비밀번호 규칙 검사
     */
    public void validatePassword(String password, int minLen, String birth, String tel) {
        // 비밀번호가 null이거나 최소 길이 미만인 경우
        if (password == null || password.length() < minLen) {
            throw new IllegalArgumentException("비밀번호는 최소 " + minLen + "자 이상이어야 합니다.");
        }

        // 비밀번호에 허용되지 않은 문자가 포함되었으면 false
        if (!password.matches("^[a-zA-Z0-9" + ALLOWED_SPECIAL_CHARACTERS + "]+$")) {
            throw new IllegalArgumentException("비밀번호는 영대소문자와 숫자, 허용된 특수문자(!, @, _)만 포함해야 합니다.");
        }

        // 비밀번호에 생년월일이나 전화번호가 포함되어 있는 경우
        if (containsSensitiveInfo(password, birth, tel)) {
            throw new IllegalArgumentException("비밀번호에 생년월일 또는 전화번호를 포함할 수 없습니다.");
        }

        // 비밀번호에 영문자, 숫자, 특수문자가 모두 포함되어 있지 않으면 false
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*") || !password.matches(".*[" + ALLOWED_SPECIAL_CHARACTERS + "].*")) {
            throw new IllegalArgumentException("비밀번호에는 영대소문자, 숫자, 특수문자가 모두 포함되어야 합니다");

        }

        // 비밀번호에 동일한 문자가 3개 이상 반복되면 false
        if (password.matches(".*(.)\\1{2,}.*")) {
            throw new IllegalArgumentException("비밀번호에 동일한 문자가 3번 이상 반복될 수 없습니다.");
        }

        // 비밀번호에 연속된 숫자가 3개 이상 포함되면 false
        for (int i = 0; i < password.length() - 2; i++) {
            char first = password.charAt(i);
            char second = password.charAt(i + 1);
            char third = password.charAt(i + 2);

            if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
                int diff1 = second - first;
                int diff2 = third - second;

                if (diff1 == diff2 && Math.abs(diff1) == 1) {
                    throw new IllegalArgumentException("비밀번호에 연속된 숫자가 포함될 수 없습니다.");
                }
            }
        }
    }

    private boolean containsSensitiveInfo(String password, String birth, String tel) {
        for (String value : new String[]{birth, tel}) {
            if (value != null && !value.trim().isEmpty()) {
                // 원본 값 확인
                if (containsSanitizedSubstring(password, value)) {
                    return true;
                }
                // 생년월일의 마지막 4자리 확인 (예: 970418 -> 0418)
                if (value.length() >= 6) {
                    String lastFour = value.substring(value.length() - 4); // 뒤에서 4자리 추출
                    if (containsSanitizedSubstring(password, lastFour)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 문자열 내에 숫자로 변환된 특정 값이 포함되어 있는지 확인
     */
    private boolean containsSanitizedSubstring(String password, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false; // 입력 값이 null이거나 공백 문자열인 경우
        }

        // 숫자가 아닌 문자를 제거한 뒤 비밀번호에 포함 여부 검사
        String sanitizedValue = value.replaceAll("[^0-9]", "");
        return !sanitizedValue.isEmpty() && password.contains(sanitizedValue);
    }
}
