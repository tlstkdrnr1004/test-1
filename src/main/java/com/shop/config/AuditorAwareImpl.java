package com.shop.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

/*
    Spring Data Jpa에서는 Auditing(오디팅) 기능 제공
   -  Auditing(오디팅)   사전적의미 :  감사, 감시, 기록 추적 작업
   -  즉, 엔티티의 생성과 수정을 감시하고 있는 기능을 말합니다.
   -  주로 JPA Auditing에서는 "엔티티가 생성되거나 수정될 때 자동으로 시간이나 사용자 정보를 DB에 기록"하는 기능을 의미합니다.
 */

//현재 로그인 한 사용자의 정보를 등록자와 수정자로 지정하기 위해서 AuditorAware 인터페이스를 구현한 클래스 선언
/**
 * Spring Data JPA의 Auditing 기능에서 '누가'를 담당하는 구현 클래스입니다.
 * Auditing은 데이터의 생성, 수정 시 생성자, 수정자, 시간 등을 자동으로 기록하는 기능입니다.
 * 이 클래스는 현재 인증된 사용자의 ID를 조회하여 JPA에게 제공하는 역할을 수행합니다.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * 현재 사용자의 ID를 반환하여 생성자(@CreatedBy)와 수정자(@LastModifiedBy) 필드를 채웁니다.
     * 이 메소드가 반환하는 값은 Auditing 기능에 의해 엔티티의 해당 필드에 자동으로 삽입됩니다.
     * @return 현재 사용자 ID를 포함하는 Optional<String> 객체.
     */
    @Override
    public Optional<String> getCurrentAuditor() {

        // SecurityContextHolder는 Spring Security의 보안 컨텍스트를 관리하는 클래스입니다.
        // getContext()를 통해 현재 스레드의 SecurityContext를, getAuthentication()으로 인증 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // authentication 객체가 null이거나, 인증되지 않은 상태인 경우 처리합니다.
        // 이 경우, 현재 감사자(auditor)를 특정할 수 없음을 의미합니다.
        if (authentication == null || !authentication.isAuthenticated()) {
            // 사용자가 없는 경우이므로 비어있는 Optional을 반환합니다.
            return Optional.empty();
        }

        // authentication.getName() 메소드는 현재 로그인된 사용자의 이름을 반환합니다.
        // 일반적으로 Spring Security 설정에 따라 사용자의 고유 ID(username)가 됩니다.
        String userId = authentication.getName();

        // 조회된 사용자 ID를 Optional.of()를 사용하여 감싼 후 반환합니다.
        // Optional을 사용하면 NullPointerException(NPE)의 위험 없이 값을 안전하게 전달할 수 있습니다.
        return Optional.of(userId);
    }
}