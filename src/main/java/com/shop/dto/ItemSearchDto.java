package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 📦 ItemSearchDto 클래스
 * - 관리자 페이지 등에서 상품 목록을 검색할 때,
 *   사용자가 선택한 "검색 조건"을 담아 전달하는 DTO(Data Transfer Object)입니다.
 * - 이 클래스는 검색 조건을 담는 용도로만 사용되며, 실제 DB와 연결되는 엔티티가 아닙니다.
 */
@Getter
@Setter
public class ItemSearchDto {

    /**
     * [1] 등록일 기준 검색 조건
     * ----------------------------------------
     * 사용자가 선택한 날짜 조건에 따라, '상품 등록일'이 현재 시간 기준으로 얼마 전인지를 필터링합니다.
     * 프론트엔드에서 드롭다운이나 버튼 등을 통해 선택된 값이 이 변수에 저장됩니다.
     *
     * 예시:
     * - "all" → 전체 상품 보기 (날짜 조건 없음)
     * - "1d"  → 최근 1일 이내에 등록된 상품만 조회
     * - "1w"  → 최근 1주일 이내
     * - "1m"  → 최근 1개월 이내
     * - "6m"  → 최근 6개월 이내
     *
     * Querydsl 동적 조건에서 사용되며, 사용자가 선택한 조건에 따라 SQL where절에 날짜 비교 조건이 추가됩니다.
     */
    private String searchDateType;

    /**
     * [2] 판매 상태 기준 필터
     * ----------------------------------------
     * 상품의 판매 상태를 기준으로 데이터를 필터링할 때 사용됩니다.
     * ItemSellStatus는 Enum 클래스이며, 대표적으로 다음과 같은 값이 정의되어 있을 수 있습니다:
     * - SELL     : 판매 중인 상품
     * - SOLD_OUT : 품절된 상품
     *
     * 이 값은 관리자가 상품을 상태별로 구분해서 보고 싶을 때 사용됩니다.
     * 예: "판매 중" 상태만 보고 싶을 때는 SELL만 조회
     */
    private ItemSellStatus searchSellStatus;

    /**
     * [3] 검색 유형 (어떤 컬럼으로 검색할 것인지 지정)
     * ----------------------------------------
     * 사용자가 상품명을 검색할지, 등록자 ID로 검색할지를 선택한 값을 저장합니다.
     * 이 값에 따라 SQL의 WHERE절이 달라지며, 검색할 컬럼이 동적으로 결정됩니다.
     *
     * 가능한 값:
     * - "itemNm"     : 상품명 기준으로 검색 (예: '노트북'이라는 이름이 포함된 상품 찾기)
     * - "createdBy"  : 상품 등록자 ID 기준 검색 (예: 'admin'이 등록한 상품 조회)
     */
    private String searchBy;

    /**
     * [4] 검색어
     * ----------------------------------------
     * 사용자가 입력한 검색 텍스트를 저장합니다.
     * - 이 검색어는 위의 searchBy(검색 유형)에 따라 적용 위치가 달라집니다.
     *   예: searchBy가 "itemNm"이면 → itemNm LIKE %검색어%
     *       searchBy가 "createdBy"이면 → createdBy LIKE %검색어%
     *
     * 기본값은 빈 문자열("")로 설정되어 있으며, 입력값이 없으면 검색 조건에서 제외됩니다.
     */
    private String searchQuery = "";
}
