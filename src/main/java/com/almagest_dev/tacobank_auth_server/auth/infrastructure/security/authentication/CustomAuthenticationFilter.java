package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import com.almagest_dev.tacobank_auth_server.auth.application.dto.LoginRequestDTO;
import com.almagest_dev.tacobank_auth_server.common.exception.ExceptionResponseWriter;
import com.almagest_dev.tacobank_auth_server.common.exception.RedisSessionException;
import com.almagest_dev.tacobank_auth_server.common.util.RedisSessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final JwtProvider jwtProvider;
    private final RedisSessionUtil redisSessionUtil;

    private final String FAILURE_PREFIX = "login:failure";
    private final String LOCK_PREFIX = "login:lock";

    public CustomAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, JwtProvider jwtProvider, RedisSessionUtil redisSessionUtil) {
        super(defaultFilterProcessesUrl, authenticationManager);
        this.jwtProvider = jwtProvider;
        this.redisSessionUtil = redisSessionUtil;
        log.info("CustomAuthenticationFilter START");
    }

    /**
     * @param request  from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     *                 redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

        // 이메일과 비밀번호를 Request Attribute에 저장
        request.setAttribute("email", loginRequest.getEmail());
        request.setAttribute("password", loginRequest.getPassword());

        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {
            // Redis에서 계정 잠금 상태 확인
            String lockKey = LOCK_PREFIX + username;
            String lockStatus = redisSessionUtil.getValueIfExists(lockKey);
            if ("LOCKED".equals(lockStatus)) {
                ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "계정이 잠겨 있습니다. 잠금 해제까지 대기해 주세요.");
            }
        } catch (RedisSessionException ex) {
            log.warn("RedisSessionException - " + ex.getMessage());
            int httpStatus = ex.getHttpStatus().value();
            String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

            ExceptionResponseWriter.writeExceptionResponse(response, httpStatus, "FAILURE", message);
            return null;
        }

        log.info("CustomAuthenticationFilter::attemptAuthentication - username: " + username);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password); // 인증되지 않은 상태
        return getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 인증 성공시
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();

        try {
            // Redis에서 실패 횟수 초기화
            redisSessionUtil.cleanupRedisKeys("CustomAuthenticationFilter", username, FAILURE_PREFIX);
        }  catch (RedisSessionException ex) {
            log.warn("RedisSessionException - " + ex.getMessage());
            int httpStatus = ex.getHttpStatus().value();
            String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

            ExceptionResponseWriter.writeExceptionResponse(response, httpStatus, "FAILURE", message);
            return;
        }


        // SecurityContextHolder에 Authenticaton 객체 자동 세팅
        // 로그인 성공시 JWT 토큰 생성 & 쿠키 세팅
        String token = jwtProvider.createToken(authResult);
        log.info("CustomAuthenticationFilter::successfulAuthentication - token: " + token);

        Cookie authorizationCookie = new Cookie("Authorization", token);
        authorizationCookie.setHttpOnly(true);
        authorizationCookie.setMaxAge(60 * 10); // 10분
        authorizationCookie.setPath("/"); // 모든 경로에서 쿠키가 유효하도록 설정

        response.addCookie(authorizationCookie);
        response.addHeader("Authorization", "Bearer " + token);
    }

    /**
     * 인증 실패시
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("CustomAuthenticationFilter::unsuccessfulAuthentication - Authentication failed: " + failed.getMessage());

        // 비밀번호 실패
        if (failed instanceof BadCredentialsException) {
            // Request Attribute에서 email 가져옴
            String username = (String) request.getAttribute("email");
            String redisKey = FAILURE_PREFIX + username; // 실패 횟수 키
            String lockKey = LOCK_PREFIX + username;    // 계정 잠금 키

            try {
                // 실패 횟수 증가
                Long failureCount = redisSessionUtil.incrementAndSetExpire(redisKey, 1L, 10, TimeUnit.MINUTES); // TTL 10분 설정
                log.info("CustomAuthenticationFilter::unsuccessfulAuthentication - Failure count for {}: {}", username, failureCount);

                // 실패 횟수가 5회 이상일 경우 계정 잠금 처리
                if (failureCount >= 5) {
                    redisSessionUtil.storeSessionData(lockKey, "LOCKED", 10, TimeUnit.MINUTES); // 계정 잠금 상태 저장
                    log.warn("CustomAuthenticationFilter::unsuccessfulAuthentication - Account locked for {}", username);
                    ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_FORBIDDEN, "Account Locked", "비밀번호 입력이 5회 이상 실패하여 계정이 10분간 잠겼습니다. 10분 후 다시 시도해주세요.");
                    return;
                }

            } catch (RedisSessionException ex) {
                log.warn("RedisSessionException - " + ex.getMessage());
                int httpStatus = ex.getHttpStatus().value();
                String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

                ExceptionResponseWriter.writeExceptionResponse(response, httpStatus, "FAILURE", message);
                return;
            }
        }

        // 실패 메시지 반환
        ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "아이디 또는 비밀번호가 잘못되었습니다. 다시 시도해주세요.");
    }
}
