package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler;


import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, "SUCCESS", "로그아웃 되었습니다.");
    }
}