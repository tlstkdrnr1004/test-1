package com.shop.dto;

import com.shop.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

/**
 * ============================================
 * 주문 상품 DTO (OrderItemDto)
 * ============================================
 * 주문 내역 화면에서 "주문한 개별 상품 하나"의 정보를 보여주기 위한 클래스입니다.
 *
 * 비유: 마트 영수증의 한 줄과 같습니다.
 * 영수증 한 줄에는 상품명, 수량, 금액이 적혀 있고,
 * 옆에 상품 사진(이미지 URL)까지 붙어있는 형태입니다.
 *
 * OrderHistDto(주문 이력)가 택배 송장이라면,
 * OrderItemDto는 송장 안에 적힌 "상품 1: 티셔츠 2개 30,000원" 같은 각 항목입니다.
 */
@Getter // Lombok: 모든 필드의 getter 메서드를 자동 생성 (예: getItemNm(), getCount() 등)
@Setter // Lombok: 모든 필드의 setter 메서드를 자동 생성 (예: setItemNm(), setCount() 등)
public class OrderItemDto {

    /**
     * 생성자: OrderItem(주문 상품 엔티티)과 상품 이미지 URL을 받아서 DTO로 변환합니다.
     * 비유: DB 창고에 보관된 주문 상품 원본 데이터에서 화면에 보여줄 정보만 골라 담는 작업입니다.
     *
     * @param orderItem - 주문 상품 엔티티 (DB에서 조회한 원본 데이터)
     * @param imgUrl    - 상품 대표 이미지의 경로 (예: "/images/item/shirt.jpg")
     */
    public OrderItemDto(OrderItem orderItem, String imgUrl){
        this.itemNm = orderItem.getItem().getItemNm();      // 주문 상품 엔티티 → 상품 엔티티 → 상품명 추출
        this.count = orderItem.getCount();                    // 주문한 수량
        this.orderPrice = orderItem.getOrderPrice();          // 주문 당시의 상품 가격 (할인 등이 반영된 실제 결제 단가)
        this.imgUrl = imgUrl;                                 // 상품 이미지 경로
    }

    private String itemNm;    // 상품명 (예: "나이키 운동화", "캐시미어 목도리")
    private int count;        // 주문 수량 (예: 2개, 3개)
    private int orderPrice;   // 주문 금액 (개당 가격, 총액이 아닌 단가)
    private String imgUrl;    // 상품 이미지 경로 (화면에서 상품 사진을 표시하기 위한 URL)

}
