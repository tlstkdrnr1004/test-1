package com.shop.entity;

import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;


/**
 * ========================================================================
 * MemberTest - 회원 엔티티의 JPA Auditing(자동 감사) 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 회원(Member)을 DB에 저장할 때, 아래 4가지 정보가 자동으로 기록되는지 확인합니다:
 *   1) 등록 시간(regTime) - 언제 생성되었는지
 *   2) 수정 시간(updateTime) - 마지막으로 언제 수정되었는지
 *   3) 등록자(createdBy) - 누가 생성했는지
 *   4) 수정자(modifiedBy) - 누가 마지막으로 수정했는지
 *
 * 이 정보들은 개발자가 직접 코드로 입력하지 않아도,
 * JPA Auditing 기능이 자동으로 채워줍니다.
 *
 * 일상 비유:
 *   회사 문서에 자동으로 "작성자: 홍길동", "작성일: 2024-01-01",
 *   "최종 수정자: 김철수", "수정일: 2024-01-02"가 찍히는 것과 같습니다.
 *   사람이 일일이 기록하지 않아도 시스템이 알아서 남겨주는 것이죠.
 *
 * [Auditing 관련 어노테이션]
 *   - @CreatedDate: 엔티티가 처음 생성된 시간을 자동 기록
 *   - @LastModifiedDate: 엔티티가 마지막으로 수정된 시간을 자동 기록
 *   - @CreatedBy: 엔티티를 처음 생성한 사용자를 자동 기록
 *   - @LastModifiedBy: 엔티티를 마지막으로 수정한 사용자를 자동 기록
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 *
 * @Transactional - 테스트 실행 후 DB 변경사항을 자동으로 롤백합니다.
 *   비유: 연필로 쓴 후 지우개로 지우는 것처럼, 테스트 데이터가 실제 DB에 남지 않습니다.
 *
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberTest {

    /**
     * @Autowired - 스프링이 자동으로 MemberRepository 객체를 주입합니다.
     * 회원 데이터를 DB에 저장하고 조회하는 데 사용됩니다.
     */
    @Autowired
    MemberRepository memberRepository;

    /**
     * @PersistenceContext - JPA의 EntityManager를 주입받습니다.
     *
     * EntityManager는 엔티티(데이터 객체)를 관리하는 핵심 매니저입니다.
     * - em.flush(): 메모리에 있는 변경사항을 실제 DB에 반영합니다.
     *   비유: 워드에서 "저장" 버튼을 누르는 것
     * - em.clear(): 메모리(영속성 컨텍스트)를 비워서, 이후 조회 시 DB에서 새로 읽어오게 합니다.
     *   비유: 브라우저 캐시를 비우고 새로고침하는 것
     */
    @PersistenceContext
    EntityManager em;

    /**
     * JPA Auditing 기능 테스트
     *
     * [테스트 시나리오]
     * 1. "gildong"이라는 사용자가 로그인한 상태에서 (가짜 사용자 설정)
     * 2. 새 회원(Member) 객체를 만들어 DB에 저장합니다.
     * 3. 메모리를 비운 후, DB에서 회원을 다시 조회합니다.
     * 4. 조회한 회원에 등록 시간, 수정 시간, 등록자, 수정자가 자동으로 기록되어 있는지 확인합니다.
     *
     * 비유:
     *   회사에서 "홍길동" 직원이 새 문서를 작성하면,
     *   시스템이 자동으로 문서 하단에 "작성자: 홍길동, 작성일: 2024-01-01"을
     *   기록해주는 것과 같습니다. 직원이 일일이 적지 않아도 됩니다.
     *
     * @Test - JUnit에게 이 메서드가 테스트 대상임을 알립니다.
     * @DisplayName("Auditing 테스트") - 테스트 결과 화면에 표시될 이름입니다.
     *
     * @WithMockUser(username = "gildong", roles = "USER")
     *   - 테스트 실행 시, "gildong"이라는 사용자가 로그인한 상태를 설정합니다.
     *   - Auditing의 @CreatedBy, @LastModifiedBy가 이 사용자 이름("gildong")을 기록합니다.
     *   - 비유: 테스트를 위해 "홍길동"이라는 이름표를 달고 있는 상태입니다.
     */
    @Test
    @DisplayName("Auditing 테스트")
    @WithMockUser(username = "gildong", roles = "USER")
    public void auditingTest() {
        // [1단계] 새 회원 객체 생성 (아직 DB에 저장 전)
        Member newMember = new Member();

        // [2단계] DB에 저장 - 이 시점에 Auditing이 작동하여
        //   등록 시간, 수정 시간, 등록자(gildong), 수정자(gildong)가 자동으로 설정됩니다.
        memberRepository.save(newMember);

        // [3단계] 메모리의 변경사항을 실제 DB에 반영
        em.flush();

        // [4단계] 메모리(영속성 컨텍스트) 비우기
        //   이후 조회 시 캐시가 아닌 실제 DB에서 데이터를 가져오게 됩니다.
        em.clear();

        // [5단계] DB에서 회원을 다시 조회
        Member member = memberRepository.findById(newMember.getId())
                .orElseThrow(EntityNotFoundException::new);

        // [6단계] Auditing으로 자동 기록된 정보 확인 (콘솔 출력)
        System.out.println("register time : " + member.getRegTime());      // 등록 시간
        System.out.println("update time : " + member.getUpdateTime());     // 수정 시간
        System.out.println("create member : " + member.getCreatedBy());    // 등록자
        System.out.println("modify member : " + member.getModifiedBy());   // 수정자
    }

}
