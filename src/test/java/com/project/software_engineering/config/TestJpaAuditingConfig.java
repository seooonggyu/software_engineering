package com.project.software_engineering.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * ====================================================================
 * [Test Config] JPA Auditing 테스트 우회 설정
 * ====================================================================
 * 문제: @WebMvcTest는 JPA 컨텍스트를 로드하지 않으나,
 *       SoftwareEngineeringApplication에 @EnableJpaAuditing이 설정되어
 *       jpaMappingContext 빈 생성 실패로 컨텍스트 로드가 실패함.
 *
 * 해결: @WebMvcTest에서만 사용되는 별도 JPA Auditing 설정을 제공하여
 *       @EnableJpaAuditing 관련 빈이 없어도 컨텍스트가 정상 로드되도록 함.
 *
 * 사용: @WebMvcTest 기반 컴포넌트 테스트에서
 *       @Import(TestJpaAuditingConfig.class) 추가
 * ====================================================================
 */
@TestConfiguration
@EnableJpaAuditing
public class TestJpaAuditingConfig {

    /**
     * JPA Auditing에서 현재 감사자(auditor)를 제공하는 빈.
     * 테스트 환경에서는 "test-auditor"를 고정값으로 반환.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test-auditor");
    }
}
