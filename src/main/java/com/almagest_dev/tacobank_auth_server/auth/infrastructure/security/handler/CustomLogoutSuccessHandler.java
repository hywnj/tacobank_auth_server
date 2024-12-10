package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler;


import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication == null) {
            ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, new AuthResponseDto<>("FAILURE", "이미 로그아웃된 상태입니다."));
            log.info("CustomLogoutSuccessHandler::onLogoutSuccess 이미 로그아웃 상태");
        } else {
            ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, new AuthResponseDto<>("SUCCESS", "로그아웃 되었습니다."));
            log.info("CustomLogoutSuccessHandler::onLogoutSuccess 로그아웃 성공");
        }
    }
}