package com.project.software_engineering.config;

import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
	
	private final UserRepository userRepository;
	private final CorsFilterConfiguration corsFilterConfiguration;
	private final ObjectMapper objectMapper;
	private final AuthService authService;
	private final ExternalProperties externalProperties;

	public SecurityConfig(UserRepository userRepository, CorsFilterConfiguration corsFilterConfiguration, ObjectMapper objectMapper, AuthService authService
			, ExternalProperties externalProperties) {
		this.userRepository = userRepository;
		this.corsFilterConfiguration = corsFilterConfiguration;
		this.objectMapper = objectMapper;
		this.authService = authService;
		this.externalProperties = externalProperties;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
    /**
	 *  Spring Security 권한 설정.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

		// 1. 기본 보안 설정 (CSRF, 폼로그인 비활성화, 세션 STATELESS 설정)
		http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
				.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

		// 2. 커스텀 필터 인스턴스 생성 및 세팅
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, objectMapper, authService, externalProperties);
		jwtAuthenticationFilter.setFilterProcessesUrl("/api/login"); // 로그인 엔드포인트 지정

		JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(authenticationManager, userRepository, authService, externalProperties);
		FilterExceptionHandlerFilter exceptionHandlerFilter = new FilterExceptionHandlerFilter();

		// 3. 필터 체인에 차례대로 조립 (addFilter 사용)
		http
				.addFilter(corsFilterConfiguration.corsFilter())
				.addFilter(jwtAuthenticationFilter)
				.addFilter(jwtAuthorizationFilter)
				.addFilterBefore(exceptionHandlerFilter, BasicAuthenticationFilter.class);

		return http.build();
	}
}