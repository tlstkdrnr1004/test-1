package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ============================================
 * 상품 정보 DTO (ItemDto)
 * ============================================
 * Thymeleaf 템플릿 엔진을 사용하여 화면(HTML)에 상품 데이터를 출력할 때 사용하는 클래스입니다.
 *
 * 비유: 상품 카탈로그의 한 페이지와 같습니다.
 * 카탈로그에는 상품번호, 상품명, 가격, 상세설명, 판매상태 등이 적혀 있어서
 * 고객이 한눈에 상품 정보를 파악할 수 있습니다.
 *
 * DTO를 사용하는 이유:
 * - Item 엔티티(DB 테이블과 직접 연결된 객체)를 그대로 화면에 전달하면
 *   불필요한 DB 관련 정보까지 노출될 수 있습니다.
 * - 필요한 정보만 담아서 안전하게 화면으로 전달하기 위해 DTO를 사용합니다.
 * - 비유하면, 회사 내부 문서(엔티티) 대신 고객용 안내문(DTO)을 만들어 전달하는 것과 같습니다.
 */
@Getter  // Lombok: 모든 필드에 대해 getter 메서드를 자동 생성 (예: getId(), getItemNm() 등)
@Setter  // Lombok: 모든 필드에 대해 setter 메서드를 자동 생성 (예: setId(), setItemNm() 등)
public class ItemDto {

    private Long id;                    // 상품 고유 번호 (DB에서 각 상품을 구분하는 ID, 예: 1, 2, 3...)

    private String itemNm;              // 상품명 (예: "나이키 에어맥스", "삼성 갤럭시 버즈")

    private Integer price;              // 상품 가격 (원 단위, 예: 59000, 129000)

    private String itemDetail;          // 상품 상세 설명 (예: "편안한 착용감의 무선 이어폰입니다")

    private String sellStatCd;          // 판매 상태 코드 (예: "SELL" = 판매중, "SOLD_OUT" = 품절)

    private LocalDateTime regTime;      // 상품 등록 시간 (상품이 처음 등록된 날짜와 시간)

    private LocalDateTime updateTime;   // 상품 수정 시간 (상품 정보가 마지막으로 수정된 날짜와 시간)

}
