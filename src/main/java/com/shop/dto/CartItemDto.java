package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니에 상품을 담을 때 사용하는 DTO (CartItemDto)
 *
 * 비유: 마트에서 "이 상품 몇 개 담아주세요"라고 적는 "주문서"와 같습니다.
 *       고객이 어떤 상품(itemId)을 몇 개(count) 장바구니에 넣고 싶은지 적어서
 *       직원(컨트롤러)에게 전달하는 용도입니다.
 *
 * 유효성 검증: 잘못된 주문서가 들어오지 않도록 @NotNull, @Min 어노테이션으로
 *             입력값을 검증합니다. (상품 ID는 필수, 수량은 최소 1개 이상)
 */
@Getter @Setter // Lombok이 모든 필드에 대해 getter/setter 메서드를 자동으로 만들어줍니다
public class CartItemDto {

    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.") // 상품 ID가 비어있으면(null) 검증 실패. 에러 메시지가 클라이언트에 전달됩니다.
    private Long itemId; // 장바구니에 담을 상품의 고유 ID (어떤 상품인지 식별)

    @Min(value = 1, message = "최소 1개 이상 담아주세요") // 수량이 1 미만이면 검증 실패. 0개나 음수는 불가능합니다.
    private int count; // 장바구니에 담을 수량 (몇 개를 담을 것인지)

}
