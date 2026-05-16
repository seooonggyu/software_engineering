package com.project.software_engineering.security;

import com.project.software_engineering.domain.RefreshToken;
import com.project.software_engineering.exception.InvalidTokenException;
import com.project.software_engineering.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

	private final ExternalProperties externalProperties;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthServiceImpl(
			ExternalProperties externalProperties
			, RefreshTokenRepository refreshTokenRepository
	) {
		this.externalProperties = externalProperties;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	private Key getSigningKey() {
		byte[] keyBytes = externalProperties.getTokenSecretKey().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Access Token 생성을 위한 함수.
	 */
	@Override
	public String createAccessToken(Long userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + externalProperties.getAccessTokenExpirationTime());

		return Jwts.builder()
				.setSubject("accessToken")
				.claim("id", userId)
				.setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS512)
				.compact();
	}

	/**
	 * 	Access Token 검증을 위한 함수
	 *
	 */
	@Override
	public Long verifyAccessToken(String accessToken) {
		try {
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(accessToken)
					.getBody();

			return claims.get("id", Number.class).longValue(); // "id" 값을 안전하게 추출
		} catch (JwtException e) {
			// 토큰 만료, 서명 오류 등 모든 JWT 관련 예외를 잡아서 커스텀 예외로 던짐
			throw new InvalidTokenException("유효하지 않은 Access Token입니다.");
		}
	}

	/**
	 * Refresh Token 생성을 위한 함수.
	 */
	@Override
	public String createRefreshToken(Long userId) {
		// 기존 리프레시 토큰 지우기
		revokeRefreshToken(userId);

		Calendar nowCal = Calendar.getInstance();
		nowCal.add(Calendar.YEAR, 1);
		Date expiryDate = new Date(nowCal.getTimeInMillis());

		String refreshToken = Jwts.builder()
				.setSubject("refreshToken")
				.claim("id", userId)
				.setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS512)
				.compact();

		// DB 저장
		refreshTokenRepository.save(RefreshToken.of(refreshToken, userId));

		return refreshToken;
	}

	/**
	 * 	Refresh Token 삭제 위한 함수.
	 *  user Id로 조회해서 모두 지운다.
	 */
	@Override
	public void revokeRefreshToken(Long userId) {
		refreshTokenRepository.deleteAll(refreshTokenRepository.findByUserId(userId));
	}

	/**
	 * Refresh Token 검증을 위한 함수.
	 */
	@Override
	public Long verifyRefreshToken(String refreshToken) {
		// 1. DB에 존재하는지 확인
		refreshTokenRepository.findByContent(refreshToken)
				.orElseThrow(() -> new InvalidTokenException("DB에 존재하지 않는 Refresh Token입니다."));

		// 2. JWT 서명 및 만료 검증
		try {
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(refreshToken)
					.getBody();

			return claims.get("id", Number.class).longValue();
		} catch (JwtException e) {
			throw new InvalidTokenException("유효하지 않거나 만료된 Refresh Token입니다.");
		}
	}
	
	/**
	 * 	Access Token 발급을 위한 함수.
	 *  Refresh Token이 유효하다면 Access Token 발급.
	 */
	@Override
	public String issueAccessToken(String refreshToken) {
		Long userId = verifyRefreshToken(refreshToken);
		return createAccessToken(userId);
	}

}