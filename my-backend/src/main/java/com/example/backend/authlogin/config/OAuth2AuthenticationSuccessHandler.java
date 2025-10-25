package com.example.backend.authlogin.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.authlogin.service.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(
            oAuth2User.getUserId(),
            oAuth2User.getEmail(),
            oAuth2User.getName(),
            oAuth2User.getProvider()
        );
        String provider = String.valueOf(oAuth2User.getProvider());

        log.info("OAuth2 인증 성공 - 사용자: {}, 토큰 생성됨", oAuth2User.getEmail());

        ResponseCookie authCookie = ResponseCookie
            .from("token", token)
            .maxAge(60 * 60)
            .sameSite("None")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

        // 소셜 로그인 성공 후 프론트의 로그인 화면으로 토큰 전달
        String serverName = request.getServerName();
        boolean isLocal = serverName == null
            || "localhost".equalsIgnoreCase(serverName)
            || "127.0.0.1".equals(serverName)
            || "0:0:0:0:0:0:0:1".equals(serverName)
            || "::1".equals(serverName);
        String targetBase = isLocal ? "http://localhost:5173" : "https://hwiyeong.shop";
        String targetUrl = targetBase + "/login?provider=" + provider;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}