package com.shop.repository;

import com.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ============================================
 * 주문 상품 리포지토리 (OrderItemRepository)
 * ============================================
 * OrderItem(주문 상품) 엔티티를 데이터베이스에서 조회, 저장, 수정, 삭제하는 인터페이스입니다.
 *
 * 비유: 도서관의 사서와 같습니다.
 * "이 주문 상품 정보 좀 찾아주세요", "이 주문 상품 정보를 저장해주세요"라고 요청하면
 * 사서(리포지토리)가 책장(DB)에서 해당 데이터를 찾거나 보관해줍니다.
 *
 * JpaRepository<OrderItem, Long>의 의미:
 * - OrderItem: 이 리포지토리가 관리하는 엔티티 타입 (주문 상품 테이블)
 * - Long: 해당 엔티티의 기본 키(Primary Key) 타입 (order_item_id의 타입이 Long)
 *
 * JpaRepository를 상속받으면 별도 코드 없이도 다음 기능을 자동으로 사용할 수 있습니다:
 * - save()      : 주문 상품 저장 또는 수정
 * - findById()  : ID로 주문 상품 1건 조회
 * - findAll()   : 모든 주문 상품 조회
 * - delete()    : 주문 상품 삭제
 * - count()     : 전체 주문 상품 수 조회
 *
 * 현재는 기본 CRUD만 사용하므로 추가 메서드 정의가 없습니다.
 * 필요 시 메서드명 규칙(Query Method)이나 @Query 어노테이션으로 커스텀 조회를 추가할 수 있습니다.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
