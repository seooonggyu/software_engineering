package com.project.software_engineering.security;

import com.project.software_engineering.domain.User;
import com.project.software_engineering.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

/**
 * JWT Access Token을 검증하고 유효하면 SecurityContextHolder에 Authentication을 등록하는 필터
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ExternalProperties externalProperties;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository,
                                   AuthService authService, ExternalProperties externalProperties) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.authService = authService;
        this.externalProperties = externalProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String jwtHeader = request.getHeader(externalProperties.getAccessKey());

        // 토큰 없으면 그냥 다음 필터로
        if (jwtHeader == null || !jwtHeader.startsWith(externalProperties.getTokenPrefix())) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = jwtHeader.substring(externalProperties.getTokenPrefix().length());
        Long userId = authService.verifyAccessToken(accessToken);

        User user = userRepository.findById(userId).orElse(null);
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // Authentication 생성 후 SecurityContextHolder에 등록
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}