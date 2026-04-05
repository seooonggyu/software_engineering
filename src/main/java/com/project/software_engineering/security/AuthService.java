package com.project.software_engineering.security;

public interface AuthService {
	String createAccessToken(Long userId);
	Long verifyAccessToken(String accessToken);
	String createRefreshToken(Long userId);
	void revokeRefreshToken(Long userId);
	Long verifyRefreshToken(String refreshToken);
	String issueAccessToken(String refreshToken);
}