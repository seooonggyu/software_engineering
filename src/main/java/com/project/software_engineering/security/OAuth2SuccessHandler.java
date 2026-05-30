package com.project.software_engineering.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // 발급
        String accessToken = authService.createAccessToken(principalDetails.getUser().getId());
        String refreshToken = authService.createRefreshToken(principalDetails.getUser().getId());

        // React 프론트엔드 콜백 페이지로 리다이렉트
        String targetUrl = "http://localhost:5173/oauth2/callback";

        // Query Param 방식으로 전달 (또는 쿠키로 설정 가능)
        String redirectUrl = targetUrl + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
