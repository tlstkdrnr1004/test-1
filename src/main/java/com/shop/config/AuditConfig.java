package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;



/*
    Spring Data Jpa에서는 Auditing(오디팅) 기능 제공
   -  Auditing(오디팅)   사전적의미 :  감사, 감시, 기록 추적 작업
   -  즉, 엔티티의 생성과 수정을 감시하고 있는 기능을 말합니다.
   -  주로 JPA Auditing에서는 "엔티티가 생성되거나 수정될 때 자동으로 시간이나 사용자 정보를 DB에 기록"하는 기능을 의미합니다.
 */

/**
 * JPA Auditing(감사) 기능 활성화를 위한 설정 클래스입니다.
 */
@Configuration // 이 클래스가 Spring의 구성(Configuration) 정보를 담고 있음을 나타냅니다.
@EnableJpaAuditing // JPA Auditing 기능을 활성화합니다.
public class AuditConfig {

    /**
     * AuditorAware를 Spring의 Bean으로 등록합니다.
     * @return AuditorAware<String>의 구현체인 AuditorAwareImpl 객체
     */
    @Bean // 이 메소드가 반환하는 객체를 Spring 컨테이너가 관리하는 Bean으로 등록합니다.
    public AuditorAware<String> auditorProvider() {
        // AuditorAware<String>의 구현체로 AuditorAwareImpl을 사용하도록 지정합니다.
        // 이를 통해 Spring은 Auditing 시 현재 사용자를 조회하기 위해 AuditorAwareImpl을 사용하게 됩니다.
        return new AuditorAwareImpl();
    }
}