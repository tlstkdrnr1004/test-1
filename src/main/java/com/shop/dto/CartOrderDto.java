package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 장바구니 주문 DTO (CartOrderDto)
 *
 * 비유: 마트에서 장바구니에 담긴 여러 상품을 한꺼번에 "계산해주세요"라고 전달하는
 *       "계산 요청서"와 같습니다.
 *
 * 구조가 독특합니다:
 *   - cartItemId: 개별 장바구니 상품의 ID (계산 요청서의 한 줄)
 *   - cartOrderDtoList: 여러 장바구니 상품의 주문 요청 목록 (계산 요청서 묶음)
 *
 * 왜 이런 구조일까?
 *   클라이언트(화면)에서 여러 장바구니 상품을 선택해서 한 번에 주문할 때,
 *   바깥 CartOrderDto 객체의 cartOrderDtoList 안에 각 상품의 CartOrderDto가 담겨서 전달됩니다.
 *   즉, 하나의 "봉투(바깥 객체)" 안에 여러 개의 "계산 요청서(안쪽 객체 리스트)"가 들어있는 형태입니다.
 */
@Getter // Lombok이 모든 필드에 대해 getter 메서드를 자동 생성합니다
@Setter // Lombok이 모든 필드에 대해 setter 메서드를 자동 생성합니다
public class CartOrderDto {

    private Long cartItemId; // 주문할 장바구니 상품의 고유 ID (어떤 장바구니 항목을 주문할 것인지)

    private List<CartOrderDto> cartOrderDtoList; // 주문할 장바구니 상품들의 목록 (여러 상품을 한 번에 주문할 때 사용)

}
