package com.shop.dto;

import com.shop.constant.OrderStatus;
import com.shop.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================
 * 주문 이력 DTO (OrderHistDto)
 * ============================================
 * 쇼핑몰에서 "마이페이지 > 주문 내역"을 눌렀을 때 화면에 보여줄 데이터를 담는 클래스입니다.
 *
 * 비유: 택배 송장과 같습니다.
 * 송장에는 주문번호, 주문날짜, 배송상태가 적혀 있고,
 * 안에 어떤 상품들이 들어있는지(OrderItemDto 리스트)도 함께 확인할 수 있습니다.
 *
 * DTO(Data Transfer Object)란?
 * - 데이터를 화면(View)으로 전달하기 위한 "택배 상자" 같은 객체입니다.
 * - 엔티티(DB 테이블과 직접 매핑된 객체)를 그대로 화면에 노출하면
 *   불필요한 정보까지 전달되거나 보안 문제가 생길 수 있으므로,
 *   필요한 정보만 골라 담아서 전달합니다.
 */
@Getter // Lombok: 모든 필드에 대해 getter 메서드를 자동으로 생성해줍니다 (예: getOrderId(), getOrderDate() 등)
@Setter // Lombok: 모든 필드에 대해 setter 메서드를 자동으로 생성해줍니다 (예: setOrderId(), setOrderDate() 등)
public class OrderHistDto {

    /**
     * 생성자: Order(주문) 엔티티 객체를 받아서 DTO로 변환합니다.
     * 비유: DB 창고에서 꺼낸 원본 주문서(Order)를 고객에게 보여줄 깔끔한 요약본(DTO)으로 옮겨 적는 작업입니다.
     *
     * @param order - 주문 엔티티 (DB에서 조회한 원본 주문 데이터)
     */
    public OrderHistDto(Order order){
        this.orderId = order.getId();  // 주문 엔티티의 고유 ID를 가져와서 저장
        // 주문 날짜를 "2026-03-16 14:30" 같은 형식의 문자열로 변환하여 저장
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();  // 주문 상태(ORDER: 주문완료, CANCEL: 주문취소)를 저장
    }

    private Long orderId;              // 주문 고유 번호 (예: 1, 2, 3... 각 주문을 구분하는 ID)
    private String orderDate;          // 주문 날짜 (화면에 보여줄 용도로 문자열로 변환된 형태)
    private OrderStatus orderStatus;   // 주문 상태 (ORDER: 주문완료 / CANCEL: 주문취소)

    /**
     * 이 주문에 포함된 상품 목록을 담는 리스트입니다.
     * 비유: 택배 상자 안에 들어있는 각 상품들의 목록표입니다.
     * 하나의 주문에 여러 개의 상품이 포함될 수 있으므로 List로 관리합니다.
     */
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    /**
     * 주문 상품 DTO를 리스트에 추가하는 메서드입니다.
     * 비유: 택배 상자에 상품을 하나씩 넣는 동작입니다.
     *
     * @param orderItemDto - 추가할 주문 상품 정보
     */
    public void addOrderItemDto(OrderItemDto orderItemDto){
        orderItemDtoList.add(orderItemDto);
    }

}
