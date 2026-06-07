package com.project.software_engineering.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 필터 체인에서 발생하는 JWT 예외를 잡아 401 응답을 반환하는 필터
 */
public class FilterExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "Expired Access Token");
        } catch (com.project.software_engineering.exception.InvalidTokenException | io.jsonwebtoken.JwtException | ClassCastException e) {
            writeUnauthorized(response, "Invalid Access Token");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.getWriter().write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}