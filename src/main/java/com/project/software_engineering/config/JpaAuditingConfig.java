package com.project.software_engineering.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 전용 설정 클래스.
 * @EnableJpaAuditing을 메인 애플리케이션 클래스에서 분리하여
 * @WebMvcTest 기반 컴포넌트 테스트에서 JPA 컨텍스트 없이도
 * 컨텍스트 로드가 가능하도록 합니다.
 *
 * @WebMvcTest는 @Configuration 클래스를 선택적으로 제외할 수 있습니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
