package com.shop.constant;

//enum 타입의 클래스를 사용하면 연관된 상수들을 모아 둘 수 있으며
//enum 타입의 클래스에 정의한 상수값을 가지고 컴파일시 체크할수 있다는 장점이 있습니다.

// 상품이 현재 "판매 중(SELL)" 인지, "품절(SOLD_OUT)" 상태인지 나타내는 enum 타입 클래스입니다.
public enum ItemSellStatus {

    // SELL : 상품이 판매 가능한 상태 (재고 있음)
    SELL,

    // SOLD_OUT : 상품이 품절된 상태 (재고 없음)
    SOLD_OUT
}
