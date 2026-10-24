package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 장바구니 상세 정보 DTO (CartDetailDto)
 *
 * 비유: 장바구니 안에 담긴 상품의 "가격표 + 사진"이라고 생각하면 됩니다.
 *       마트에서 장바구니를 열어보면 상품 이름, 가격, 몇 개 담았는지, 상품 사진이 보이는데
 *       이 정보를 하나로 묶어서 화면에 보여주기 위한 클래스입니다.
 *
 * DTO란? Data Transfer Object의 약자로, 데이터를 옮기는 "택배 상자"와 같습니다.
 *         DB에서 가져온 데이터를 화면에 전달할 때 필요한 정보만 담아서 보냅니다.
 *
 * 참고: 이 DTO는 CartItemRepository의 JPQL 쿼리에서 직접 생성됩니다.
 *       쿼리 결과가 이 클래스의 생성자를 호출하여 바로 객체를 만들어냅니다.
 */
@Getter @Setter // Lombok이 모든 필드에 대해 getter/setter 메서드를 자동으로 만들어줍니다
public class CartDetailDto {

    private Long cartItemId; // 장바구니 상품 고유 ID (어떤 장바구니 항목인지 식별하는 번호표)

    private String itemNm; // 상품명 (예: "나이키 운동화", "삼성 노트북")

    private int price; // 상품 금액 (단가. 예: 50000원)

    private int count; // 장바구니에 담은 수량 (예: 3개)

    private String imgUrl; // 상품 대표 이미지 경로 (화면에 상품 사진을 보여주기 위한 URL)

    /**
     * JPQL 쿼리에서 직접 DTO를 생성하기 위한 생성자
     *
     * CartItemRepository의 findCartDetailDtoList() 메서드에서
     * "select new com.shop.dto.CartDetailDto(...)" 형태로 이 생성자를 호출합니다.
     * 비유: 택배 상자에 상품 정보를 하나씩 채워 넣는 과정입니다.
     *
     * @param cartItemId 장바구니 상품 ID
     * @param itemNm     상품명
     * @param price      상품 금액
     * @param count      수량
     * @param imgUrl     상품 이미지 경로
     */
    public CartDetailDto(Long cartItemId, String itemNm, int price, int count, String imgUrl){
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
    }

}
