package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter::doFilterInternal");
        // 토큰 추출
        String token = getTokenFromCookies(request.getCookies());
        log.info("JwtAuthenticationFilter::doFilterInternal - token: " + token);
        if (token != null && jwtProvider.validateToken(token)) {
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JwtAuthenticationFilter::doFilterInternal - getAuthentication : " + SecurityContextHolder.getContext().getAuthentication());
        }
        // log.info("JwtAuthenticationFilter::doFilterInternal - " + SecurityContextHolder.getContext().getAuthentication().getName() + " | " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        filterChain.doFilter(request, response);
    }

    /**
     * Cookie 에서 토큰 추출
     */
    private String getTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("Authorization".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
