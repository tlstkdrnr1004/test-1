package com.shop.repository;

import com.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ============================================
 * 주문 리포지토리 (OrderRepository)
 * ============================================
 * Order(주문) 엔티티를 데이터베이스에서 조회, 저장, 수정, 삭제하는 인터페이스입니다.
 *
 * 비유: 쇼핑몰 주문 관리 창구와 같습니다.
 * 고객이 "내 주문 내역 보여주세요"라고 하면 이 창구(리포지토리)가
 * 주문 장부(DB의 orders 테이블)에서 해당 고객의 주문 기록을 찾아줍니다.
 *
 * JpaRepository<Order, Long>의 의미:
 * - Order: 이 리포지토리가 관리하는 엔티티 타입 (주문 테이블)
 * - Long: 해당 엔티티의 기본 키(Primary Key) 타입 (order_id의 타입이 Long)
 *
 * 기본 제공 메서드 (JpaRepository 상속):
 * - save(), findById(), findAll(), delete(), count() 등
 *
 * 아래에는 기본 메서드로는 불가능한 커스텀 조회 메서드를 직접 정의하고 있습니다.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 특정 회원(이메일 기준)의 주문 목록을 최신순으로 조회합니다.
     *
     * 비유: "김철수 고객님의 주문 내역을 최근 것부터 보여주세요"라고 요청하는 것과 같습니다.
     * Pageable을 사용하여 한 번에 몇 건씩 볼지(페이징)도 지정할 수 있습니다.
     *
     * @Query 어노테이션: SQL이 아닌 JPQL(Java Persistence Query Language)로 쿼리를 직접 작성합니다.
     *   - "select o from Order o": Order 엔티티에서 데이터를 조회 (o는 별칭)
     *   - "where o.member.email = :email": 주문한 회원의 이메일이 파라미터와 일치하는 것만 필터링
     *   - "order by o.orderDate desc": 주문 날짜 기준 내림차순(최신순) 정렬
     *
     * @param email    - 조회할 회원의 이메일 주소
     * @param pageable - 페이징 정보 (몇 페이지의 몇 건을 가져올지)
     * @return 해당 회원의 주문 목록 (최신순, 페이지 단위)
     */
    @Query("select o from Order o " +
            "where o.member.email = :email " +
            "order by o.orderDate desc"
    )
    List<Order> findOrders(@Param("email") String email, Pageable pageable);

    /**
     * 특정 회원(이메일 기준)의 총 주문 건수를 조회합니다.
     *
     * 비유: "김철수 고객님이 지금까지 총 몇 번 주문했나요?"라고 물어보는 것과 같습니다.
     * 이 값은 주문 내역 페이징 처리 시 전체 페이지 수를 계산하는 데 사용됩니다.
     *
     * @param email - 조회할 회원의 이메일 주소
     * @return 해당 회원의 총 주문 건수
     */
    @Query("select count(o) from Order o " +
            "where o.member.email = :email"
    )
    Long countOrder(@Param("email") String email);
}
