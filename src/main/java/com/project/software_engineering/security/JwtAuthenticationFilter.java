package com.project.software_engineering.security;

import com.project.software_engineering.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * "/api/login" 요청에서 자격을 검증하고 Refresh Token을 발급하는 필터
 */
@Transactional
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final ExternalProperties externalProperties;

    // 로그인 시도: username/password로 인증 처리
    @Transactional
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UserDto.LoginReqDto userLoginDto = objectMapper.readValue(request.getInputStream(), UserDto.LoginReqDto.class);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword());
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException | AuthenticationException e) {
            return null;
        }
    }

    // 로그인 성공 시 Refresh Token 발급
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        String refreshToken = authService.createRefreshToken(principalDetails.getUser().getId());
        response.addHeader(externalProperties.getRefreshKey(), externalProperties.getTokenPrefix() + refreshToken);
    }
}