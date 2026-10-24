package com.shop.entity;

import com.shop.dto.MemberFormDto;
import com.shop.repository.CartRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ========================================================================
 * CartTest - 장바구니(Cart)와 회원(Member) 엔티티 매핑 관계 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 장바구니(Cart)와 회원(Member)이 일대일(@OneToOne) 관계로 올바르게 매핑되어 있는지 확인합니다.
 * 즉, DB에서 장바구니를 조회할 때 연결된 회원 정보도 함께 가져오는지를 검증합니다.
 *
 * 일상 비유:
 *   마트에서 각 고객에게 전용 장바구니가 하나씩 배정된다고 생각해보세요.
 *   장바구니 번호만 알면 "이 장바구니의 주인이 누구인지"도 바로 알 수 있어야 합니다.
 *   이 테스트는 장바구니로부터 주인(회원) 정보를 올바르게 찾아올 수 있는지 확인합니다.
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 * @Transactional - 테스트 후 DB 변경사항을 자동으로 롤백합니다.
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")

class CartTest {

    @Autowired
    CartRepository cartRepository;       // 장바구니 데이터를 DB에 저장/조회하는 도구

    @Autowired
    MemberRepository memberRepository;   // 회원 데이터를 DB에 저장/조회하는 도구

    @Autowired
    PasswordEncoder passwordEncoder;     // 비밀번호 암호화 도구

    /**
     * @PersistenceContext - JPA의 EntityManager를 주입받는 어노테이션입니다.
     *
     * EntityManager는 JPA에서 엔티티(데이터 객체)를 관리하는 핵심 매니저입니다.
     * 비유: 도서관 사서가 책(엔티티)을 정리하고, 찾아주고, 반납처리하듯,
     *       EntityManager는 데이터를 DB에 저장, 조회, 삭제하는 역할을 합니다.
     *
     * 여기서 em.flush()와 em.clear()를 통해
     * - flush(): 메모리에 있는 변경사항을 실제 DB에 반영 (비유: 문서를 저장 버튼 누르기)
     * - clear(): 메모리(영속성 컨텍스트)를 비움 (비유: 캐시를 지워서 DB에서 새로 읽어오게 하기)
     */
    @PersistenceContext
    EntityManager em;

    /**
     * 테스트용 회원을 생성하는 헬퍼 메서드입니다.
     * 장바구니를 만들려면 먼저 "주인"이 될 회원이 있어야 합니다.
     */
    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    /**
     * 장바구니-회원 엔티티 매핑 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 회원을 생성하여 DB에 저장합니다.
     * 2. 장바구니를 생성하고, 해당 회원을 장바구니의 주인으로 설정합니다.
     * 3. 장바구니를 DB에 저장합니다.
     * 4. em.flush()로 메모리의 데이터를 실제 DB에 반영합니다.
     * 5. em.clear()로 메모리(영속성 컨텍스트)를 비웁니다.
     *    -> 이후 조회 시 캐시가 아닌 실제 DB에서 데이터를 읽어옵니다.
     * 6. DB에서 장바구니를 다시 조회합니다.
     * 7. 조회된 장바구니에 연결된 회원 ID가 원래 회원 ID와 일치하는지 확인합니다.
     *
     * 비유:
     *   1) 마트에 "홍길동" 고객을 회원으로 등록합니다.
     *   2) 홍길동 전용 장바구니를 만들어 배정합니다.
     *   3) 나중에 장바구니 번호만 가지고 조회했을 때,
     *      "이 장바구니의 주인은 홍길동입니다"라는 정보가 함께 나오는지 확인합니다.
     *
     * [JPA 즉시 로딩(Eager Loading) 개념]
     *   Cart와 Member는 @OneToOne(일대일) 관계로 매핑되어 있습니다.
     *   일대일, 다대일 관계는 기본적으로 "즉시 로딩"으로 설정됩니다.
     *   즉시 로딩이란: 장바구니를 조회할 때, 연관된 회원 정보도 한 번에 함께 가져오는 방식입니다.
     *   비유: 택배 상자를 열면 안에 "보증서"가 같이 들어있는 것처럼,
     *         장바구니를 조회하면 회원 정보가 자동으로 같이 따라옵니다.
     */
    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest(){
        // [1단계] 회원 생성 및 DB 저장
        Member member = createMember();
        memberRepository.save(member);

        // [2단계] 장바구니 생성 및 회원 연결 후 DB 저장
        Cart cart = new Cart();
        cart.setMember(member);        // 장바구니의 주인을 설정
        cartRepository.save(cart);

        // [3단계] 메모리 -> DB 동기화 (INSERT 쿼리 실행)
        em.flush();

        // [4단계] 메모리(영속성 컨텍스트) 비우기
        //   이후 조회 시 캐시가 아닌 실제 DB에서 데이터를 가져오게 됨
        em.clear();

        // [5단계] DB에서 장바구니를 다시 조회
        //   이때 Cart와 Member가 JOIN되어 한 번에 조회됩니다. (즉시 로딩)
        Cart savedCart = cartRepository.findById(cart.getId())
                         .orElseThrow(EntityNotFoundException::new);

        // [6단계 - 검증] 장바구니에 연결된 회원 ID가 원래 회원 ID와 같은지 확인
        assertEquals(savedCart.getMember().getId(), member.getId());
    }

}
