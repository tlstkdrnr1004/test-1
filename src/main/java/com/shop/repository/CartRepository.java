package com.shop.repository;

import com.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 장바구니(Cart) 리포지토리 인터페이스
 *
 * 비유: 회원마다 하나씩 가지고 있는 장바구니를 보관하는 "물품 보관소"입니다.
 *       회원 번호를 알려주면 해당 회원의 장바구니를 찾아서 꺼내줍니다.
 *
 * JpaRepository<Cart, Long>를 상속받으면 기본 CRUD 메서드가 자동 제공됩니다.
 *   - save(cart): 장바구니 저장
 *   - findById(id): ID로 장바구니 조회
 *   - delete(cart): 장바구니 삭제
 *   등등...
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 회원 ID로 장바구니를 찾는 메서드
     *
     * 비유: "김철수 고객님의 장바구니 가져와주세요"라고 요청하면
     *       회원 번호로 해당 장바구니를 찾아주는 것입니다.
     *
     * Spring Data JPA의 메서드 이름 규칙에 따라 자동으로 쿼리가 생성됩니다.
     * findBy + MemberId → SELECT * FROM cart WHERE member_id = ?
     *
     * @param memberId 장바구니 소유자인 회원의 ID
     * @return 해당 회원의 장바구니. 없으면 null 반환 (아직 장바구니를 만든 적이 없는 회원)
     */
    Cart findByMemberId(Long memberId);

}
