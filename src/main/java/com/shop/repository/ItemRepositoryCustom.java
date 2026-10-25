package com.shop.repository;

import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.shop.dto.MainItemDto;

/*
 Querydsl을 Spring Data Jpa와 함께 사용하기 위해서는 사용자 정의 리포지토리 인터페이스를 정의 해야 합니다.
 총 3단계의 과정으로 구현 하면 됩니다.
 1. 사용자 정의 인터페이스 작성  interface ItemRepositoryCustom {}  작성
 2. 사용자 정의 인터페이스 구현  ItemRepositoryCustom 인터페이스를 구현하는 ItemRepositoryCustomImpl 클래스 작성
 3. Spring Data Jpa 리포지터리 인터페이스에서 사용자 정의 인터페이스 상속
 */

//1. 사용자 정의 인터페이스 작성
public interface ItemRepositoryCustom {

    //상품 조회 조건을 담고 있는 ItemSearchDto 객체와 페이징 정보를 담고 있는 Pageable 객체를 매개변수로 받는
    //getAdminItemPage 메소드 선언.
    //반환 데이터로 Page<Item> 객체를 반환합니다.
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);


    // 이 메서드는 메인 페이지(또는 특정 뷰)에 보여줄 상품 목록을 페이징 처리하여 조회하는 기능을 담당합니다.
    // 반환 타입은 Page<MainItemDto> 이며, 이는 Spring Data JPA에서 제공하는 페이징 결과를 담는 객체입니다.
    // Page 객체는 다음과 같은 페이징 정보를 포함하고 있습니다:
    // - 전체 데이터 수 (total elements)
    // - 총 페이지 수 (total pages)
    // - 현재 페이지 번호 (current page)
    // - 페이지에 포함된 실제 데이터(List<MainItemDto>)

    // 매개변수 설명:
    // itemSearchDto: 사용자가 입력한 검색 조건을 담는 DTO 객체입니다.
    // - 예: 상품명, 카테고리, 등록일, 판매 상태 등 다양한 검색 필드가 포함될 수 있습니다.

    // pageable: 페이징 정보를 담고 있는 객체입니다.
    // - 페이지 번호, 한 페이지에 몇 개의 데이터가 표시될지 등의 정보를 가지고 있습니다.
    // - PageRequest.of(page, size[, sort]) 로 생성된 객체가 들어옵니다.

    // 이 메서드는 일반적으로 커스텀 쿼리 또는 QueryDSL을 이용해 구현되며,
    // 검색 조건과 페이징 정보를 바탕으로 필터링된 상품 목록을 Page 형태로 반환합니다.
    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);


}