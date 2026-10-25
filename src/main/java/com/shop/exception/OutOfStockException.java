package com.shop.exception;

/*
    =====================================================================
    OutOfStockException - 재고 부족 시 발생하는 커스텀(사용자 정의) 예외 클래스
    =====================================================================

    비유: 편의점에서 물건을 사려고 했는데, 직원이 "죄송합니다, 재고가 없습니다"라고
    알려주는 상황과 같습니다. 이 클래스는 그 "재고 없음 알림"을 프로그램 안에서
    표현하는 역할을 합니다.

    RuntimeException을 상속받았기 때문에:
    - "언체크 예외(Unchecked Exception)"로 분류됩니다.
    - 메서드 선언부에 throws를 명시하지 않아도 됩니다.
    - 주문 수량이 재고보다 많을 때 이 예외가 발생(throw)되어 주문이 중단됩니다.
 */

// RuntimeException: 자바에서 기본 제공하는 예외 클래스.
// 이를 상속받아 "재고 부족"이라는 특수한 상황을 표현하는 우리만의 예외를 만듭니다.
public class OutOfStockException extends RuntimeException {

    // 생성자: 예외가 발생할 때 구체적인 에러 메시지를 전달받습니다.
    // 예) new OutOfStockException("상품의 재고가 부족합니다. (현재 재고 수량: 5)")
    // super(message)를 호출하여 부모 클래스(RuntimeException)에 메시지를 전달합니다.
    public OutOfStockException(String message) {
        super(message);
    }

}