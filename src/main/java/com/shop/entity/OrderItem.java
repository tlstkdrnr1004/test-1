package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * ============================================
 * 주문 상품 엔티티 (OrderItem)
 * ============================================
 * 회원이 주문한 "개별 상품 하나"의 정보를 저장하는 JPA 엔티티입니다.
 * DB의 order_item 테이블과 직접 매핑됩니다.
 *
 * 비유: 음식점에서 주문표의 "한 줄"과 같습니다.
 * 주문표(Order) 전체가 "3번 테이블 주문"이라면,
 * 주문표의 한 줄(OrderItem)은 "짜장면 2그릇 - 14,000원"처럼
 * 어떤 메뉴를 몇 개 시켰고, 얼마인지를 기록합니다.
 *
 * 하나의 주문(Order)에 여러 개의 주문 상품(OrderItem)이 포함될 수 있습니다.
 * 예: 주문 1건에 "티셔츠 2개 + 바지 1개" → OrderItem이 2개 생성됩니다.
 */
@Entity     // JPA: 이 클래스가 DB 테이블과 매핑되는 엔티티임을 선언합니다
@Getter     // Lombok: 모든 필드의 getter 메서드 자동 생성
@Setter     // Lombok: 모든 필드의 setter 메서드 자동 생성
public class OrderItem extends BaseEntity {
    // BaseEntity를 상속받아 등록일(regTime), 수정일(updateTime),
    // 등록자(createdBy), 수정자(modifiedBy) 필드를 자동으로 물려받습니다.

    /**
     * 주문 상품 고유 ID (Primary Key)
     * 비유: 주문표의 각 줄에 매겨진 일련번호입니다.
     */
    @Id                                  // JPA: 이 필드가 기본 키(PK)임을 선언
    @GeneratedValue                      // JPA: 기본 키 값을 DB가 자동으로 생성 (1, 2, 3... 순서대로 증가)
    @Column(name = "order_item_id")      // JPA: DB 테이블의 컬럼명을 "order_item_id"로 지정
    private Long id;

    /**
     * 주문한 상품(Item) 정보
     *
     * 비유: 주문표 한 줄에 "어떤 메뉴"를 시켰는지 적는 칸입니다.
     *
     * 관계 설명:
     * - 하나의 상품(Item)은 여러 주문 상품(OrderItem)에 포함될 수 있습니다.
     *   (예: "나이키 운동화"가 A고객 주문에도, B고객 주문에도 들어갈 수 있음)
     * - 따라서 OrderItem 입장에서 Item과의 관계는 "다대일(ManyToOne)"입니다.
     *
     * fetch = FetchType.LAZY (지연 로딩):
     * - 비유: 도서관에서 책 목록만 먼저 가져오고, 실제 책 내용은 필요할 때 꺼내보는 방식
     * - OrderItem을 조회할 때 Item 정보를 즉시 가져오지 않고, 실제로 사용할 때 DB에서 조회합니다.
     * - 불필요한 DB 조회를 줄여 성능을 향상시킵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)   // 다대일 관계 + 지연 로딩 설정
    @JoinColumn(name = "item_id")        // 외래 키(FK)로 item_id 컬럼 사용
    private Item item;

    /**
     * 이 주문 상품이 속한 주문(Order) 정보
     *
     * 비유: 이 주문 줄이 "어떤 주문표"에 속하는지 표시하는 것입니다.
     *
     * 관계 설명:
     * - 하나의 주문(Order)에 여러 주문 상품(OrderItem)이 포함될 수 있습니다.
     *   (예: 한 번 주문할 때 티셔츠, 바지, 모자를 함께 주문)
     * - 따라서 OrderItem 입장에서 Order와의 관계는 "다대일(ManyToOne)"입니다.
     *
     * 연관 관계의 주인 (FK 관리자):
     * - OrderItem이 order_id 외래 키를 가지고 있으므로, OrderItem이 연관 관계의 주인입니다.
     * - 주인이란: 외래 키를 직접 등록, 수정, 삭제할 수 있는 쪽을 의미합니다.
     * - 반대편 Order 엔티티에서는 mappedBy로 읽기만 가능하게 설정합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)   // 다대일 관계 + 지연 로딩 설정
    @JoinColumn(name = "order_id")       // 외래 키(FK)로 order_id 컬럼 사용
    private Order order;
    /*
        Orders(주문 엔티티) 테이블  과    OrderItem(주문한 상품 엔티티) 테이블을  order_id컬럼을 외래키로 조인하면
        Orders(주문)에 속한  OrderItem(주문한 상품)이 어떤 상품들이 있는지 알수 있고,
        OrderItem(주문한 상품)은 어떤 Orders(주문)에 속하는지 알수 있습니다.
        즉, 테이블은 외래 키 하나로 양방향 조회가 가능합니다.


        Oerders (주문한 정보가 저장되는 엔티티)테이블                 Order_item (주문한 상품정보가 저장되는 엔티티)테이블
            order_id (PK)                                           order_item_id (PK)
            member_id (FK)                                          order_id (FK)
            order_date                                              item_id (FK)
            order_status                                            order_price
                                                                    count

        엔티티는 테이블과 다릅니다.  엔티티를 양뱡향 연관 관계로  설정하면  객체의 참조는 둘인데 외래 키는 하나이므로
        둘 중 누가 외래키를 관리할지를 정해야 합니다.
            -   연관 관계의 주인은 외래 키가 있는 곳으로 설정
            -   연관 관계의 주인이 외래 키를 관리( 등록, 수정, 삭제 )
            -   주인이 아닌 쪽은 연관 관계 매핑 시 (mappedBy 속성의 값으로 연관 관계의 주인을 설정)
            -   주인이 아닌 쪽은 읽기만 가능
     */


    /**
     * 주문 시 가격 (개당 단가)
     * 비유: 주문표에 적힌 메뉴 하나의 가격입니다. (세트 할인 등이 반영된 실제 결제 단가)
     * 상품의 원래 가격이 아닌, 주문 당시의 가격을 별도로 저장합니다.
     * (가격이 나중에 변경되어도 과거 주문 기록은 그대로 유지하기 위함)
     */
    private int orderPrice;

    /**
     * 주문 수량
     * 비유: 주문표에서 "짜장면 x2"의 숫자 2에 해당합니다.
     */
    private int count;

    /**
     * 주문 상품 생성 메서드 (정적 팩토리 메서드)
     *
     * 비유: 주문표에 새로운 줄을 추가하는 것과 같습니다.
     * "이 상품을 몇 개 주문합니다"라는 정보를 받아서 OrderItem 객체를 생성하고,
     * 동시에 해당 상품의 재고도 주문 수량만큼 차감합니다.
     *
     * @param item  - 주문할 상품 엔티티
     * @param count - 주문 수량
     * @return 생성된 OrderItem 객체
     */
    public static OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);                   // 주문할 상품 설정
        orderItem.setCount(count);                 // 주문 수량 설정
        orderItem.setOrderPrice(item.getPrice());  // 상품의 현재 가격을 주문 가격으로 저장

        item.removeStock(count);                   // 주문한 만큼 상품 재고 차감
        return orderItem;
    }

    /**
     * 주문 상품의 총 가격을 계산합니다.
     * 비유: "짜장면 7,000원 x 2개 = 14,000원"처럼 단가와 수량을 곱하는 것입니다.
     *
     * @return 단가(orderPrice) * 수량(count) = 총 금액
     */
    public int getTotalPrice() {
        return orderPrice * count;
    }

    /**
     * 주문 취소 시 재고를 복구합니다.
     * 비유: 주문을 취소하면 창고에 상품을 다시 돌려놓는 것과 같습니다.
     * 주문했던 수량만큼 상품 재고를 다시 증가시킵니다.
     */
    public void cancel() {
        this.getItem().addStock(count); // 주문 수량만큼 재고 다시 추가
    }
}
