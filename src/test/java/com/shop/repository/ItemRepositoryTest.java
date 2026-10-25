package com.shop.repository;

import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

/**
 * ========================================================================
 * ItemRepositoryTest - 상품(Item) 리포지토리 데이터 조회 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 상품(Item) 데이터를 DB에 저장하고, 다양한 방법으로 조회하는 기능이
 * 올바르게 동작하는지 확인합니다.
 *
 * 크게 4가지 조회 방법을 테스트합니다:
 *   1) Spring Data JPA 쿼리 메소드 - 메소드 이름만으로 자동 쿼리 생성
 *      (findByItemNm, findByPriceLessThan 등)
 *   2) @Query 어노테이션 - JPQL로 직접 쿼리 작성
 *   3) Native Query - 실제 SQL문을 직접 작성
 *   4) QueryDSL - 자바 코드로 타입 안전하게 쿼리 작성
 *
 * 일상 비유:
 *   도서관에서 책을 찾는 다양한 방법을 테스트하는 것과 같습니다.
 *   - 쿼리 메소드: "제목이 '자바의 정석'인 책 찾아주세요" (사서에게 간단히 요청)
 *   - @Query(JPQL): "IT 분류 중에서 2024년 이후 출판된 책을 가격순으로 정렬해주세요" (상세 요청)
 *   - Native Query: 사서를 거치지 않고 직접 서고에 들어가 SQL로 검색하는 것
 *   - QueryDSL: 검색 조건을 레고 블록처럼 조립하여, 복잡한 조건도 안전하게 검색하는 것
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 *   모든 빈(Bean)을 스프링 컨테이너에 등록하여 통합 테스트를 실행합니다.
 *
 * @TestPropertySource - 테스트 전용 설정 파일(application-test.properties)을 사용합니다.
 *   실제 운영 설정(application.properties)보다 높은 우선순위를 가집니다.
 * ========================================================================
 */
@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
class ItemRepositoryTest {

    /**
     * ItemRepository - 상품 테이블의 CRUD(생성/조회/수정/삭제) 작업을 수행하는 도구
     *
     * @Autowired - 스프링이 자동으로 ItemRepository 객체를 생성하여 주입합니다.
     * 비유: 도서관의 검색 시스템처럼, 상품 데이터를 저장하고 검색하는 역할을 합니다.
     */
    @Autowired
    ItemRepository itemRepository;

    /**
     * EntityManager - JPA의 핵심 관리자
     *
     * @PersistenceContext - JPA의 EntityManager를 주입받습니다.
     * 영속성 컨텍스트(1차 캐시 메모리)를 직접 제어할 때 사용합니다.
     * 비유: DB와 자바 프로그램 사이의 "중간 관리자"로,
     *       데이터를 메모리에 보관했다가 적절한 시점에 DB에 반영합니다.
     */
    @PersistenceContext
    EntityManager em;


    /**
     * [테스트 1] 상품 저장 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 객체를 만들어 정보(이름, 가격, 설명, 판매상태, 재고, 시간)를 설정합니다.
     * 2. itemRepository.save()로 DB에 저장합니다.
     * 3. 저장된 상품 정보를 콘솔에 출력하여 정상 저장 여부를 확인합니다.
     *
     * 비유: 쇼핑몰에 새 상품을 등록하고, 등록된 상품 정보를 확인하는 것입니다.
     *
     * @Test - JUnit에게 이 메서드가 테스트 대상임을 알립니다.
     * @DisplayName("상품 저장 테스트") - 테스트 결과 화면에 "상품 저장 테스트"라고 표시됩니다.
     */
    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        Item savedItem = itemRepository.save(item);
        System.out.println(savedItem.toString());
    }

    /**
     * 테스트용 상품 10개를 DB에 생성하는 헬퍼 메서드입니다.
     *
     * 조회 테스트를 하려면 먼저 "조회할 데이터"가 있어야 합니다.
     * 이 메서드는 "테스트 상품1" ~ "테스트 상품10"까지 10개의 상품을 DB에 INSERT합니다.
     * 가격은 10001원 ~ 10010원, 판매상태는 모두 SELL(판매중)입니다.
     *
     * 비유: 시험을 보기 전에 시험 문제지(데이터)를 미리 준비하는 것과 같습니다.
     *       빈 도서관에서 책 검색을 테스트하려면, 먼저 책을 진열해야 하는 것이죠.
     */
    public void createItemList(){
        for(int i=1;i<=10;i++){
            Item item = new Item();
            item.setItemNm("테스트 상품" + i);
            item.setPrice(10000 + i);
            item.setItemDetail("테스트 상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100); item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            Item savedItem = itemRepository.save(item);
        }
    }

    /**
     * [테스트 2] 상품명으로 조회하기 테스트 (Spring Data JPA 쿼리 메소드)
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다.
     * 2. 상품명이 "테스트 상품1"인 상품을 조회합니다.
     * 3. 조회 결과를 출력하여 올바르게 검색되었는지 확인합니다.
     *
     * 비유: 도서관에서 "자바의 정석"이라는 제목의 책을 검색하는 것과 같습니다.
     *       정확한 상품명을 입력하면 해당 상품만 조회됩니다.
     *
     * [쿼리 메소드란?]
     *   findByItemNm처럼 메소드 이름 규칙을 따르면,
     *   Spring Data JPA가 자동으로 SQL을 생성해줍니다.
     *   findBy + 필드명 → WHERE 필드명 = ? 쿼리가 자동 생성됩니다.
     */
    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNmTest(){
        // [준비] 조회할 대상 상품 10개를 DB에 생성
        this.createItemList();

        // [실행] 상품명이 "테스트 상품1"인 상품을 조회
        // 내부적으로 SELECT * FROM item WHERE item_nm = '테스트 상품1' 쿼리가 실행됩니다.
        List<Item> itemList = itemRepository.findByItemNm("테스트 상품1");

        // [검증] 조회된 상품 목록을 출력하여 확인
        for(Item item : itemList){
            System.out.println(item.toString());
        }
    }

    /**
     * [테스트 3] 상품명 OR 상품상세설명으로 조회하기 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다.
     * 2. 상품명이 "테스트 상품1" 이거나(OR) 상품상세설명이 "테스트 상품 상세 설명5"인 상품을 조회합니다.
     * 3. 조회 결과로 2개의 상품(상품1과 상품5)이 나오는지 확인합니다.
     *
     * 비유: 도서관에서 "제목이 '자바의 정석'이거나, 설명에 '초보자용'이 포함된 책"을
     *       동시에 검색하는 것과 같습니다. 둘 중 하나만 만족해도 결과에 포함됩니다.
     *
     * [OR 조건 쿼리 메소드]
     *   findBy + 필드명1 + Or + 필드명2 → WHERE 필드1 = ? OR 필드2 = ? 쿼리 자동 생성
     */
    @Test
    @DisplayName("상품명, 상품상세설명 or 테스트")
    public void findByItemNmOrItemDetailTest(){
        // [준비] 조회할 대상 상품 10개를 DB에 생성
        this.createItemList();

        // [실행] 상품명이 "테스트 상품1" OR 상품상세설명이 "테스트 상품 상세 설명5"인 상품 조회
        // 내부적으로 SELECT * FROM item WHERE item_nm = ? OR item_detail = ? 쿼리가 실행됩니다.
        List<Item> itemList = itemRepository.findByItemNmOrItemDetail("테스트 상품1", "테스트 상품 상세 설명5");

        //조회된 Item 엔티티 상품 2개를 List배열에서 꺼내와 반복해서 출력
        for(Item item : itemList){
            System.out.println(item.toString());
        }
/*
Hibernate:
    select
        i1_0.item_id,
        i1_0.created_by,
        i1_0.item_detail,
        i1_0.item_nm,
        i1_0.item_sell_status,
        i1_0.modified_by,
        i1_0.price,
        i1_0.reg_time,
        i1_0.stock_number,
        i1_0.update_time
    from
        item i1_0
    where
        i1_0.item_nm=?
        or i1_0.item_detail=?
    Item(id=1, itemNm=테스트 상품1, price=10001, stockNumber=100, itemDetail=테스트 상품 상세 설명1, itemSellStatus=SELL)
    Item(id=5, itemNm=테스트 상품5, price=10005, stockNumber=100, itemDetail=테스트 상품 상세 설명5, itemSellStatus=SELL)
         */
    }


    /**
     * [테스트 4] 가격이 특정 금액 미만인 상품 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다. (가격: 10001 ~ 10010원)
     * 2. 가격이 10005원 미만인 상품을 조회합니다.
     * 3. 조회 결과로 4개의 상품(10001 ~ 10004원)이 나오는지 확인합니다.
     *
     * 비유: 쇼핑몰에서 "10,005원 이하" 필터를 적용하여
     *       예산에 맞는 상품만 골라보는 것과 같습니다.
     *
     * [LessThan 쿼리 메소드]
     *   findBy + 필드명 + LessThan → WHERE 필드 < ? 쿼리 자동 생성
     *   비교: LessThan(<), LessThanEqual(<=), GreaterThan(>), GreaterThanEqual(>=)
     */
    @Test
    @DisplayName("가격 LessThan 테스트")
    public void findByPriceLessThanTest(){

        // [준비] 조회할 대상 상품 10개를 DB에 생성
        this.createItemList();

        // [실행] 가격이 10005원 미만인 상품 조회
        // 내부적으로 SELECT * FROM item WHERE price < 10005 쿼리가 실행됩니다.
        List<Item> itemList = itemRepository.findByPriceLessThan(10005);
        for(Item item : itemList){
            System.out.println(item.toString());
            /*
                select
                    i1_0.item_id,
                    i1_0.created_by,
                    i1_0.item_detail,
                    i1_0.item_nm,
                    i1_0.item_sell_status,
                    i1_0.modified_by,
                    i1_0.price,
                    i1_0.reg_time,
                    i1_0.stock_number,
                    i1_0.update_time
                from
                    item i1_0
                where
                    i1_0.price<?

            Item(id=1, itemNm=테스트 상품1, price=10001, stockNumber=100, itemDetail=테스트 상품 상세 설명1, itemSellStatus=SELL)
            Item(id=2, itemNm=테스트 상품2, price=10002, stockNumber=100, itemDetail=테스트 상품 상세 설명2, itemSellStatus=SELL)
            Item(id=3, itemNm=테스트 상품3, price=10003, stockNumber=100, itemDetail=테스트 상품 상세 설명3, itemSellStatus=SELL)
            Item(id=4, itemNm=테스트 상품4, price=10004, stockNumber=100, itemDetail=테스트 상품 상세 설명4, itemSellStatus=SELL)
             */

        }
    }

    /**
     * [테스트 5] 가격 미만 조건 + 가격 내림차순 정렬 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다. (가격: 10001 ~ 10010원)
     * 2. 가격이 10005원 미만인 상품을 가격이 높은 순서대로(내림차순) 조회합니다.
     * 3. 조회 결과가 10004 → 10003 → 10002 → 10001 순서로 나오는지 확인합니다.
     *
     * 비유: 예산 1만5원 이하의 상품 중에서 "비싼 것부터 먼저" 보여달라고 요청하는 것과 같습니다.
     *       쇼핑몰에서 필터(가격 10005원 미만) + 정렬(비싼 순)을 동시에 적용하는 것이죠.
     *
     * [OrderBy 쿼리 메소드]
     *   findBy + 조건 + OrderBy + 필드명 + Desc/Asc
     *   → WHERE 조건 ORDER BY 필드 DESC/ASC 쿼리가 자동 생성됩니다.
     */
    @Test
    @DisplayName("가격 내림차순 조회 테스트")
    public void findByPriceLessThanOrderByPriceDesc() {

        // [준비] 상품 10개 DB에 생성 (가격: 10001 ~ 10010원)
        this.createItemList();

        // ==========================================
        // [2단계] 쿼리 메소드 실행 - 가격 기준 조회 + 내림차순 정렬
        // ==========================================

        // ▶ 쿼리 메소드 설명:
        // - 조건: price < 10005
        // - 정렬: ORDER BY price DESC
        // ▶ 즉, 가격이 10005 미만인 상품들 중에서
        //     price가 높은 상품부터 조회

        List<Item> itemList = itemRepository.findByPriceLessThanOrderByPriceDesc(10005);

        // ==========================================
        // [3단계] 조회 결과 출력
        // ==========================================

        for (Item item : itemList) {

            // ▶ JPA는 SQL 결과를 Item 엔티티로 변환하여 리스트로 반환
            // ▶ 엔티티는 영속성 컨텍스트에서 관리됨
            // ▶ toString() 호출 시, 연관 엔티티(fetch = LAZY)는 미조회
            System.out.println(item.toString());

        /*
            ▶ 콘솔 출력 예시:
            Item(id=4, itemNm=테스트 상품4, price=10004, ...)
            Item(id=3, itemNm=테스트 상품3, price=10003, ...)
            Item(id=2, itemNm=테스트 상품2, price=10002, ...)
            Item(id=1, itemNm=테스트 상품1, price=10001, ...)
        */
        }

    /*
        ===============================
         JPA 내부 동작 텍스트 모델링
        ===============================

        1. 쿼리 메소드 이름 분석:
           → findBy + PriceLessThan + OrderByPriceDesc

        2. JPQL 생성:
           SELECT i FROM Item i WHERE i.price < :price ORDER BY i.price DESC

        3. SQL 변환 (Hibernate 로그로 확인 가능):
           SELECT * FROM item WHERE price < ? ORDER BY price DESC

        4. JDBC 통해 SQL 실행:
           → ? 에는 10005 바인딩됨

        5. 결과 ResultSet → Item 엔티티 매핑

        6. List<Item> 형태로 결과 반환 (영속성 컨텍스트 등록)

        7. for문 통해 각각의 Item 엔티티 출력 (toString 이용)

        ※ 연관 엔티티가 있을 경우, getter 호출 시점에 LAZY 로딩 쿼리가 발생할 수 있음.
           현재 toString()에는 연관 필드가 없으므로 추가 쿼리는 실행되지 않음.
    */

    /*
        ▶ Hibernate 로그:
        Hibernate:
            select
                i1_0.item_id,
                i1_0.created_by,
                i1_0.item_detail,
                i1_0.item_nm,
                i1_0.item_sell_status,
                i1_0.modified_by,
                i1_0.price,
                i1_0.reg_time,
                i1_0.stock_number,
                i1_0.update_time
            from
                item i1_0
            where
                i1_0.price<?
            order by
                i1_0.price desc
    */
    }


    /**
     * [테스트 6] @Query 어노테이션(JPQL)을 이용한 상품 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다.
     * 2. 상품 상세설명에 "테스트 상품 상세 설명"이 포함된 상품을 가격 내림차순으로 조회합니다.
     * 3. 10개 상품 모두 해당 문자열을 포함하므로, 가격이 높은 순서대로 10개가 조회되는지 확인합니다.
     *
     * 비유: 도서관에서 "설명에 '프로그래밍'이라는 단어가 포함된 책을
     *       가격이 비싼 순서대로 보여주세요"라고 상세하게 요청하는 것과 같습니다.
     *
     * [@Query 어노테이션이란?]
     *   Spring Data JPA의 쿼리 메소드로 표현하기 어려운 복잡한 조건일 때,
     *   JPQL(Java Persistence Query Language)로 직접 쿼리를 작성할 수 있습니다.
     *   JPQL은 SQL과 비슷하지만, 테이블명 대신 엔티티 클래스명을 사용합니다.
     *   비유: 쿼리 메소드가 "사서에게 간단히 요청"이라면,
     *         @Query는 "검색 조건을 상세하게 적어서 요청"하는 것입니다.
     */
    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetail("테스트 상품 상세 설명");
        for(Item item : itemList){
            System.out.println(item.toString());
        }
        /*
        Hibernate:
            select
                i1_0.item_id,
                i1_0.created_by,
                i1_0.item_detail,
                i1_0.item_nm,
                i1_0.item_sell_status,
                i1_0.modified_by,
                i1_0.price,
                i1_0.reg_time,
                i1_0.stock_number,
                i1_0.update_time
            from
                item i1_0
            where
                i1_0.item_detail like replace(?, '\\', '\\\\')
            order by
                i1_0.price desc
        Item(id=10, itemNm=테스트 상품10, price=10010, stockNumber=100, itemDetail=테스트 상품 상세 설명10, itemSellStatus=SELL)
        Item(id=9, itemNm=테스트 상품9, price=10009, stockNumber=100, itemDetail=테스트 상품 상세 설명9, itemSellStatus=SELL)
        Item(id=8, itemNm=테스트 상품8, price=10008, stockNumber=100, itemDetail=테스트 상품 상세 설명8, itemSellStatus=SELL)
        Item(id=7, itemNm=테스트 상품7, price=10007, stockNumber=100, itemDetail=테스트 상품 상세 설명7, itemSellStatus=SELL)
        Item(id=6, itemNm=테스트 상품6, price=10006, stockNumber=100, itemDetail=테스트 상품 상세 설명6, itemSellStatus=SELL)
        Item(id=5, itemNm=테스트 상품5, price=10005, stockNumber=100, itemDetail=테스트 상품 상세 설명5, itemSellStatus=SELL)
        Item(id=4, itemNm=테스트 상품4, price=10004, stockNumber=100, itemDetail=테스트 상품 상세 설명4, itemSellStatus=SELL)
        Item(id=3, itemNm=테스트 상품3, price=10003, stockNumber=100, itemDetail=테스트 상품 상세 설명3, itemSellStatus=SELL)
        Item(id=2, itemNm=테스트 상품2, price=10002, stockNumber=100, itemDetail=테스트 상품 상세 설명2, itemSellStatus=SELL)
        Item(id=1, itemNm=테스트 상품1, price=10001, stockNumber=100, itemDetail=테스트 상품 상세 설명1, itemSellStatus=SELL)
         */
    }//===== findByItemDetailTest()

    /**
     * [테스트 7] Native Query(네이티브 SQL)를 이용한 상품 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다.
     * 2. 상품 상세설명에 "테스트 상품 상세 설명"이 포함된 상품을 가격 내림차순으로 조회합니다.
     * 3. 결과가 @Query(JPQL) 테스트와 동일한지 확인합니다.
     *
     * 비유: JPQL이 "한국어로 된 요청서"라면, Native Query는 "영어 원문으로 된 요청서"입니다.
     *       JPQL은 자바 엔티티 기반이고, Native Query는 실제 DB 테이블/컬럼명을 직접 사용합니다.
     *
     * [Native Query란?]
     *   JPQL 대신 실제 데이터베이스의 SQL 문법을 직접 사용하는 방식입니다.
     *   @Query(nativeQuery = true) 옵션으로 설정합니다.
     *   장점: DB에 특화된 기능(MySQL 함수 등)을 사용할 수 있습니다.
     *   단점: DB 종류가 바뀌면(MySQL → Oracle) 쿼리도 수정해야 합니다.
     */
    @Test
    @DisplayName("nativeQuery 속성을 이용한 상품 조회 테스트")
    public void nativeQueryTest(){
        // 테스트용 상품 데이터를 생성하는 메서드 호출
        this.createItemList();

        // nativeQuery=true 옵션이 적용된 네이티브 SQL 쿼리로 상품 상세 설명에
        // '테스트 상품 상세 설명' 문자열이 포함된 상품들을 조회
        List<Item> itemList = itemRepository.findByItemDetailByNative("테스트 상품 상세 설명");

        // 조회된 상품 리스트를 반복문으로 하나씩 출력
        for(Item item : itemList){
            System.out.println(item.toString());
        }

    /*
    --- 실행 시 콘솔에 출력되는 Hibernate가 생성한 실제 SQL문 ---
    select
        *  -- 모든 컬럼을 선택
    from
        item i  -- item 테이블을 alias i로 지정
    where
        i.item_detail like ?  -- 상품 상세 설명 컬럼이 특정 패턴과 일치하는 데이터 조회 (바인딩 변수 ?로 처리됨)
    order by
        i.price desc  -- 가격을 기준으로 내림차순 정렬

    --- 출력된 결과 예시 ---
    Item(id=10, itemNm=테스트 상품10, price=10010, stockNumber=100, itemDetail=테스트 상품 상세 설명10, itemSellStatus=SELL)
    Item(id=9, itemNm=테스트 상품9, price=10009, stockNumber=100, itemDetail=테스트 상품 상세 설명9, itemSellStatus=SELL)
    Item(id=8, itemNm=테스트 상품8, price=10008, stockNumber=100, itemDetail=테스트 상품 상세 설명8, itemSellStatus=SELL)
    Item(id=7, itemNm=테스트 상품7, price=10007, stockNumber=100, itemDetail=테스트 상품 상세 설명7, itemSellStatus=SELL)
    Item(id=6, itemNm=테스트 상품6, price=10006, stockNumber=100, itemDetail=테스트 상품 상세 설명6, itemSellStatus=SELL)
    Item(id=5, itemNm=테스트 상품5, price=10005, stockNumber=100, itemDetail=테스트 상품 상세 설명5, itemSellStatus=SELL)
    Item(id=4, itemNm=테스트 상품4, price=10004, stockNumber=100, itemDetail=테스트 상품 상세 설명4, itemSellStatus=SELL)
    Item(id=3, itemNm=테스트 상품3, price=10003, stockNumber=100, itemDetail=테스트 상품 상세 설명3, itemSellStatus=SELL)
    Item(id=2, itemNm=테스트 상품2, price=10002, stockNumber=100, itemDetail=테스트 상품 상세 설명2, itemSellStatus=SELL)
    Item(id=1, itemNm=테스트 상품1, price=10001, stockNumber=100, itemDetail=테스트 상품 상세 설명1, itemSellStatus=SELL)

    --- 설명 ---
    1) nativeQuery = true 옵션을 주면 JPQL 대신 실제 데이터베이스에 맞는 네이티브 SQL 문법으로 쿼리가 실행됩니다.
    2) '?' 는 바인딩 파라미터로, findByItemDetailByNative 메서드의 @Param("itemDetail")에 전달된 값이 매핑됩니다.
    3) 네이티브 쿼리에서는 JPQL과 달리 엔티티가 아닌 데이터베이스 테이블 및 컬럼명을 직접 사용합니다.
    4) 이 예제에서는 상품 상세 설명에 특정 문자열이 포함된 레코드를 조회하며, 가격 내림차순으로 정렬해서 결과를 반환합니다.
    */
    } // ===  nativeQueryTest()

/*
좋습니다! 지금부터 **QueryDSL 문법을 처음부터 끝까지 차근차근** 설명드리겠습니다.
비전공자도 이해할 수 있도록 **SQL과 비교**, 예제 위주로 **하나도 빠짐없이** 설명드릴게요.

---

## 1. QueryDSL이란?

### 한 줄 정의

> QueryDSL은 자바 코드로 SQL처럼 쿼리를 작성할 수 있게 해주는 라이브러리입니다.

* 자바 코드로 안전하게 쿼리를 작성할 수 있어 **문법 오류를 줄이고**,
* **동적 조건 추가도 쉽게** 할 수 있어 실무에서 많이 사용합니다.

---

## 2. 기본 구조

### QueryDSL의 핵심 구성

| 용어                | 설명                               |
| ----------------- | -------------------------------- |
| `Q엔티티`            | 엔티티 기반으로 자동 생성된 클래스 (예: `QItem`) |
| `JPAQueryFactory` | 쿼리를 생성하는 객체                      |
| `fetch()`         | 쿼리를 실행하고 결과를 가져오는 메서드            |

---

## 3. 기본 쿼리 구조 예시

###  상품 테이블(Item)에서 모든 상품 가져오기

```java

JPAQueryFactory queryFactory = new JPAQueryFactory(em); // EntityManager 주입

QItem qItem = QItem.item; // Q클래스 (자동 생성됨)

List<Item> result = queryFactory
                    .selectFrom(qItem) // SELECT * FROM item
                    .fetch();          // 쿼리 실행
```

---

##  4. WHERE 조건

### 1) 같은 값 비교: `.eq()`

```java
.where(qItem.name.eq("노트북"))
```

→ `WHERE name = '노트북'`

---

### 2) 포함 여부: `.like()`

```java
.where(qItem.name.like("%노트북%"))
```

→ `WHERE name LIKE '%노트북%'`

---

### 3) 크거나 같음, 작음

```java
.where(qItem.price.goe(10000)) // 가격이 10000 이상
.where(qItem.price.loe(50000)) // 가격이 50000 이하
.where(qItem.price.between(10000, 50000)) // 가격이 1만~5만 사이
```

---

### 4) 여러 조건 (AND, OR)

```java
.where(
    qItem.name.like("%노트북%")
    .and(qItem.price.goe(10000))
)
```

→ `WHERE name LIKE '%노트북%' AND price >= 10000`

```java
.where(
    qItem.name.like("%노트북%")
    .or(qItem.price.goe(10000))
)
```

→ `OR` 조건도 가능

---

##  5. 정렬 (ORDER BY)

```java
.orderBy(qItem.price.asc())  // 가격 오름차순
.orderBy(qItem.price.desc()) // 가격 내림차순
```

---

##  6. 페이징 (LIMIT, OFFSET)

```java
.offset(0)    // 시작 위치 (0부터 시작)
.limit(10)    // 몇 개 가져올지
```

→ SQL의 `LIMIT 10 OFFSET 0`과 같음

---

##  7. SELECT 컬럼 지정

```java
.select(qItem.name) // name 컬럼만 가져오기
.fetch(); // 결과는 List<String>
```

---

##  8. BooleanBuilder (동적 조건 처리)

```java
BooleanBuilder builder = new BooleanBuilder();

if (name != null) {
    builder.and(qItem.name.eq(name));
}

if (price != null) {
    builder.and(qItem.price.goe(price));
}

List<Item> result = queryFactory
                    .selectFrom(qItem)
                    .where(builder)
                    .fetch();
```

→ 조건이 있을 때만 쿼리에 포함시킬 수 있어서 매우 실무적입니다.

---

##  9. JOIN (조인)

예: 상품(Item)과 카테고리(Category)를 조인

```java
QCategory qCategory = QCategory.category;

List<Item> result = queryFactory
    .selectFrom(qItem)
    .join(qItem.category, qCategory) // 내부 조인
    .where(qCategory.name.eq("전자제품"))
    .fetch();
```

---

##  10. Group by, Having, Subquery 등도 가능

예: 상품 수량을 기준으로 그룹핑

```java
queryFactory
    .select(qItem.name, qItem.count())
    .from(qItem)
    .groupBy(qItem.name)
    .having(qItem.count().goe(2))
    .fetch();
```

---

##  11. Q 클래스는 어떻게 생기나요?

엔티티 `Item`이 있으면, QueryDSL은 아래와 같은 Q클래스를 자동으로 만들어줍니다.

```java
QItem item = QItem.item;

item.name  // name 필드 접근
item.price // price 필드 접근
```

> 이 클래스는 컴파일 시 `target/generated-sources` 폴더에 생성됩니다.

---

##  전체 예제 (한 눈에 보기)

```java
JPAQueryFactory queryFactory = new JPAQueryFactory(em);
QItem qItem = QItem.item;

List<Item> result = queryFactory
    .selectFrom(qItem)
    .where(
        qItem.name.like("%테스트%"),
        qItem.itemSellStatus.eq(ItemSellStatus.SELL)
    )
    .orderBy(qItem.price.desc())
    .offset(0)
    .limit(10)
    .fetch();
```

---

##  정리

| 기능  | 메서드                                    |
| --- | -------------------------------------- |
| 선택  | `.selectFrom()`                        |
| 조건  | `.where()`                             |
| 비교  | `.eq()`, `.like()`, `.gt()`, `.lt()` 등 |
| 정렬  | `.orderBy()`                           |
| 페이징 | `.offset()`, `.limit()`                |
| 실행  | `.fetch()`                             |

---

## 현업에서의 일반적인 패턴
1. 간단한 CRUD, 조회 → Spring Data JPA
2. 조건 있는 복잡한 검색 → QueryDSL
3. 성능 최적화 또는 복잡한 SQL → Native Query (JPA 또는 MyBatis 병행)

# 결론 요약
| 요약         | 설명
| ----------   | ----------------------------------------------------
| QueryDSL     | 복잡하거나 동적인 쿼리 작성에 최적, 타입 안전성, 유지보수 우수
| JPQL         | 간단한 쿼리나 일회성 사용엔 간편
| ⚙현업에서는   | QueryDSL 기반 구조가 대세, 특히 Spring Boot + QueryDSL 조합이 많음

 */

    /**
     * [테스트 8] QueryDSL을 이용한 상품 조회 테스트 1 - 기본 조회
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 미리 생성합니다. (모두 판매중/SELL 상태)
     * 2. QueryDSL을 사용하여 다음 조건으로 조회합니다:
     *    - 판매 상태가 SELL(판매중)인 상품
     *    - 상세설명에 "테스트 상품 상세 설명"이 포함된 상품
     *    - 가격 내림차순 정렬
     * 3. 조회 결과를 출력하여 10개 상품이 가격 높은 순으로 나오는지 확인합니다.
     *
     * 비유: 쇼핑몰에서 여러 필터를 동시에 적용하는 것과 같습니다.
     *       "판매중인 상품 중에서, 설명에 '테스트'가 포함된 것만, 비싼 순으로 보여주세요"
     *       이런 복합 검색을 자바 코드로 안전하게 작성하는 것이 QueryDSL의 강점입니다.
     *
     * [QueryDSL이란?]
     *   자바 코드로 SQL처럼 쿼리를 작성할 수 있는 라이브러리입니다.
     *   비유: SQL을 "문자열"로 쓰는 것이 메모에 주소를 적는 것이라면,
     *         QueryDSL은 "네비게이션에 주소를 입력하는 것"입니다.
     *         오타가 있으면 컴파일 시점에 바로 잡아주고, 자동완성도 지원합니다.
     *
     * [QItem이란?]
     *   QueryDSL이 Item 엔티티를 기반으로 자동 생성한 클래스입니다.
     *   이를 통해 item.itemNm, item.price 같은 방식으로 필드에 안전하게 접근할 수 있습니다.
     */
    @Test
    @DisplayName("Querydsl 조회 테스트1")
    public void queryDslTest() {

        // 테스트 데이터를 넣기 위한 메서드 호출입니다.
        // 실제 DB에 상품(Item) 10개를 임시로 추가하는 코드입니다.
        this.createItemList();

        // QueryDSL을 사용하기 위한 객체 생성입니다.
        // JPAQueryFactory는 QueryDSL에서 쿼리를 만들 때 사용하는 핵심 도구입니다.
        // 생성자에 전달된 em(EntityManager)은 JPA에서 DB와 연결해주는 객체입니다.
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        // QItem은 QueryDSL이 자동 생성한 클래스입니다.
        // 실제로는 Item 엔티티를 기반으로 만들어진 클래스이며,
        // 데이터베이스의 item 테이블과 연결된다고 보면 됩니다.
        QItem qItem = QItem.item;

        // 아래는
        // 데이터베이스에 있는 item 테이블에서 조건에 맞는 상품들을 검색해서, 가격이 높은 순으로 정렬 해서 조회 하겠다는
        // QueryDSL로 쿼리를 만드는 과정입니다.
        // SELECT * FROM item WHERE item_sell_status = 'SELL'
        // AND item_detail LIKE '%테스트 상품 상세 설명%'
        // ORDER BY price DESC 와 같은 SQL을 자바 코드로 작성한 것입니다.
        JPAQuery<Item> query  = queryFactory
                .selectFrom(qItem) // item 테이블에서 데이터를 조회하겠다.
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)) // 판매 상태가 'SELL'인 데이터만 가져온다.
                .where(qItem.itemDetail.like("%" + "테스트 상품 상세 설명" + "%")) // 상세 설명에 해당 문구가 포함된 데이터만
                .orderBy(qItem.price.desc()); // 가격이 높은 순서대로 정렬
        /*
        1. queryFactory.selectFrom(qItem)  코드 설명
            자바 코드
                .selectFrom(qItem)
            SQL로 표현하면
                 SELECT * FROM item
            의미 : item 테이블에서 모든 컬럼(*) 데이터를 조회해서 가져오겠다는 뜻
                 여기서 qItem은 Item 엔티티를 기반으로 QueryDSL이 자동으로 만들어준 클래스입니다.
                 즉, 자바에서는 QItem이 item 테이블 대신 한다고 보면 됩니다.

        2. .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)) 설명
            자바코드
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
             SQL로 표현하면
                WHERE item_sell_status = 'SELL'
             의미 : 판매 상태(item_sell_status)가 "SELL"인 상품만 조회합니다.
                    eq()는 equals, 즉 "같다"는 조건입니다.

         3. .where(qItem.itemDetail.like("%테스트 상품 상세 설명%")) 설명
             자바코드
                .where(qItem.itemDetail.like("%" + "테스트 상품 상세 설명" + "%"))
             SQL로 표현하면
                AND item_detail LIKE '%테스트 상품 상세 설명%'
             의미: item_detail 컬럼에 "테스트 상품 상세 설명"이라는 문장이 포함된 상품만 찾겠다는 조건입니다.

             주의: QueryDSL에서 .where()를 연달아 여러 번 쓰면 AND 조건으로 자동 처리됩니다.

          4. .orderBy(qItem.price.desc())  설명
             자바코드
                .orderBy(qItem.price.desc())
             SQL로 표현하면
                ORDER BY price DESC
             의미: 가격(price)이 높은 순서(내림차순, DESC)로 결과를 정렬합니다.

          5. 최종 결과: SQL 버전
            SELECT *
            FROM item
            WHERE item_sell_status = 'SELL'
              AND item_detail LIKE '%테스트 상품 상세 설명%'
            ORDER BY price DESC;


        6. 왜 QueryDSL을 쓰나요?
        | 비교 항목 | JPQL (문자열 방식)    | QueryDSL (타입 안전 방식) |
        | -------  | ----------------    | ----------------------  |
        | 쿼리 작성 | 문자열로 직접 입력     | 자바 코드로 안전하게 작성    |
        | 오류 확인 | 실행 시 오류 발생 가능 | 컴파일 시 오류 체크 가능     |
        | 자동완성  | IDE 지원 적음        | IDE 자동완성 지원 뛰어남     |
        | 유지보수  | 문자열 수정 불편      | 코드 수정처럼 간편함         |

         */

        // JPAQuery<Item>객체의 fetch() 메서드를 통해 실제로 쿼리를 실행하고 결과를 리스트로 받아옵니다.
        List<Item> itemList = query.fetch();
/*
| 반환 메서드             | 반환 타입                              | 설명                                       |
| ------------------ | ---------------------------------- | ---------------------------------------- |
| `fetch()`          | `List<T>`                          | 전체 조회 결과 리스트 반환                                |
| `fetchOne()`       | `T`                                | 단일 조회 결과 반환(여러 개면 `NonUniqueResultException`) |
| `fetchFirst()`     | `T`                                | 조회 대상중 첫 번째 레코드만 조회 결과로 반환                              |
| `fetchCount()`     | `long`                             | 결과 개수 조회후 반환 (`SELECT COUNT(*)`)                |
| `fetchResults()`   | `QueryResults<T>` **(❌ v5부터 제거됨)** | 페이징과 count 포함된 결과                        |
| `select()` + 여러 필드 | `Tuple`, `DTO`                     | 다중 컬럼 조회 시 사용                            |

 */


        // 가져온 결과(상품 목록)를 하나씩 출력합니다.
        for(Item item : itemList){
            System.out.println(item.toString()); // 각각의 상품 객체 정보를 콘솔에 출력
        }
    /*
    select
        i1_0.item_id,
        i1_0.created_by,
        i1_0.item_detail,
        i1_0.item_nm,
        i1_0.item_sell_status,
        i1_0.modified_by,
        i1_0.price,
        i1_0.reg_time,
        i1_0.stock_number,
        i1_0.update_time
    from
        item i1_0
    where
        i1_0.item_sell_status=?
        and i1_0.item_detail like ? escape '!'
    order by
        i1_0.price desc

    Item(id=10, itemNm=테스트 상품10, price=10010, stockNumber=100, itemDetail=테스트 상품 상세 설명10, itemSellStatus=SELL)
    Item(id=9, itemNm=테스트 상품9, price=10009, stockNumber=100, itemDetail=테스트 상품 상세 설명9, itemSellStatus=SELL)
    Item(id=8, itemNm=테스트 상품8, price=10008, stockNumber=100, itemDetail=테스트 상품 상세 설명8, itemSellStatus=SELL)
    Item(id=7, itemNm=테스트 상품7, price=10007, stockNumber=100, itemDetail=테스트 상품 상세 설명7, itemSellStatus=SELL)
    Item(id=6, itemNm=테스트 상품6, price=10006, stockNumber=100, itemDetail=테스트 상품 상세 설명6, itemSellStatus=SELL)
    Item(id=5, itemNm=테스트 상품5, price=10005, stockNumber=100, itemDetail=테스트 상품 상세 설명5, itemSellStatus=SELL)
    Item(id=4, itemNm=테스트 상품4, price=10004, stockNumber=100, itemDetail=테스트 상품 상세 설명4, itemSellStatus=SELL)
    Item(id=3, itemNm=테스트 상품3, price=10003, stockNumber=100, itemDetail=테스트 상품 상세 설명3, itemSellStatus=SELL)
    Item(id=2, itemNm=테스트 상품2, price=10002, stockNumber=100, itemDetail=테스트 상품 상세 설명2, itemSellStatus=SELL)
    Item(id=1, itemNm=테스트 상품1, price=10001, stockNumber=100, itemDetail=테스트 상품 상세 설명1, itemSellStatus=SELL)
     */


    }

    /**
     * 테스트용 상품 10개를 DB에 생성하는 두 번째 헬퍼 메서드입니다.
     *
     * createItemList()과 다른 점:
     * - 상품 1~5번: 판매 상태가 SELL(판매중), 재고 100개
     * - 상품 6~10번: 판매 상태가 SOLD_OUT(품절), 재고 0개
     *
     * 이렇게 판매 상태가 섞인 데이터를 만들어야,
     * "판매중인 상품만 조회"하는 동적 조건 검색을 테스트할 수 있습니다.
     *
     * 비유: 마트에서 일부 상품은 진열대에 있고(판매중),
     *       일부 상품은 "품절" 표시가 붙어있는 상황을 만드는 것입니다.
     */
    public void createItemList2(){
        for(int i=1;i<=5;i++){
            Item item = new Item();
            item.setItemNm("테스트 상품" + i);
            item.setPrice(10000 + i);
            item.setItemDetail("테스트 상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }

        for(int i=6;i<=10;i++){
            Item item = new Item();
            item.setItemNm("테스트 상품" + i);
            item.setPrice(10000 + i);
            item.setItemDetail("테스트 상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
            item.setStockNumber(0);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }
    }


    /**
     * [테스트 9] QueryDSL + BooleanBuilder를 이용한 동적 조건 조회 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 10개를 DB에 생성합니다. (5개는 판매중, 5개는 품절)
     * 2. BooleanBuilder로 다음 조건을 동적으로 조립합니다:
     *    - 상세설명에 "테스트 상품 상세 설명" 포함 (LIKE 검색)
     *    - 가격이 10003원 초과 (price > 10003)
     *    - 판매 상태가 SELL인 경우에만 추가 조건 적용
     * 3. 페이징 처리하여 첫 번째 페이지(5개)만 조회합니다.
     * 4. 조건에 맞는 상품 2개(상품4, 상품5)가 조회되는지 확인합니다.
     *
     * 비유: 쇼핑몰에서 사용자가 선택한 필터에 따라 검색 조건이 달라지는 상황입니다.
     *       예를 들어, "판매중" 체크박스를 체크하면 판매중 상품만 보여주고,
     *       체크하지 않으면 모든 상품을 보여주는 것처럼,
     *       BooleanBuilder는 조건을 레고 블록처럼 필요한 것만 조립할 수 있습니다.
     *
     * [BooleanBuilder란?]
     *   QueryDSL에서 WHERE 조건을 동적으로 만드는 빌더(조립기) 객체입니다.
     *   .and()로 조건을 추가하면 AND 조건으로 연결됩니다.
     *   if문과 함께 사용하면 "조건이 있을 때만 쿼리에 포함"시킬 수 있어 매우 실용적입니다.
     *
     * [QuerydslPredicateExecutor란?]
     *   Spring Data JPA의 Repository에 QueryDSL 조건을 직접 전달할 수 있게 해주는 인터페이스입니다.
     *   findAll(Predicate, Pageable) 메서드로 조건 + 페이징을 한 번에 처리할 수 있습니다.
     *
     * [변환 흐름]
     *   자바 코드(QueryDSL) -> JPQL 자동 생성 -> SQL 변환 -> DB에서 실행
     */
    @Test
    @DisplayName("상품 Querydsl 조회 테스트 2")
    public void queryDslTest2(){

        // 테스트 실행 전에, Item 데이터를 10개 정도 DB에 미리 생성해 넣습니다.
        this.createItemList2();

        // =====================[1. 조건을 만드는 객체 생성]=====================
        //BooleanBuilder객체는 쿼리에 들어갈 조건문을 만들어주는 빌더객체라고 생각하면됩니다.
        //Predicate(조건문이 저장되는 클래스)를 구현하고 있으며 메소드 체인 형식으로 사용할 수 있습니다.
        // 즉, SQL의 WHERE 조건을 만드는 QueryDSL의  객체라고 보면 됩니다.
        //그럼 BooleanBuilder객체가 Predicate(조건문이 저장되는 객체)라고 생각하고 사용하면 되겠군!!!!!
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // =====================[2. Q타입 객체 생성]=====================
        // QItem은 QueryDSL이 자동 생성한 Item 엔티티 전용 쿼리 객체입니다.
        // 이를 통해 자바 코드로 SQL 문법을 사용할 수 있습니다.
        QItem item = QItem.item;

        // =====================[3. 검색 조건용 변수들 선언]=====================
        String itemDetail = "테스트 상품 상세 설명"; // itemDetail 필드에 이 문자열이 포함된 상품만 조회
        int price = 10003; // 가격이 이 숫자보다 큰 상품만 조회
        String itemSellStat = "SELL"; // 판매 상태가 SELL일 경우에만 조건에 추가

        // =====================[4. 조건 조립 (BooleanBuilder 사용)]=====================
        //필요한 상품을 조회하는데 필요한 "and" 조건을 추가하고 있습니다.
        //아래 코드에서 상품의 판매상태가 SELL일 때 만 booleanBuilder에 판매상태 조건을 동적으로 추가하는 것을 볼수 잇습니다.

        // 조건 1: 상품 설명에 특정 문구가 포함되어야 합니다 (LIKE 검색)
        booleanBuilder.and(item.itemDetail.like("%" + itemDetail + "%"));

        // 조건 2: 가격이 10003보다 커야 합니다 (price > 10003)
        booleanBuilder.and(item.price.gt(price));

        // 조건 3: 판매 상태가 "SELL"일 때만, 해당 조건을 추가합니다
        // 즉, itemSellStat이 SELL일 때만 AND 조건으로 판매 상태도 검사합니다.
        if(StringUtils.equals(itemSellStat, ItemSellStatus.SELL)){

            // ItemSellStatus는 Enum으로, 값이 SELL인지 확인하고 EQ(=) 조건으로 추가
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        // =====================[5. 페이징 설정]=====================
        // 페이지 번호 0번 (첫 번째 페이지), 페이지당 최대 5개의 레코드를 조회
            //데이터를 페이징해 조회하도록  PageRequest.of() 메소드를 이용해 Pageble객체를 생성해 반환받습니다.
            //첫번째 인자는 조회할 페이지의 번호(클릭한 페이지번호), 두번째 인자는 한 페이지당 DB에서 조회할 레코드 갯수를 넣어줍니다.
        Pageable pageable = PageRequest.of(0, 5);


        // =====================[6. Repository에서 조건 기반 조회]=====================
        // QuerydslPredicateExecutor 인터페이스의 findAll() 메서드를 사용
        // 조건(booleanBuilder)과 페이징(pageable)을 전달하여 해당 조건에 맞는 데이터만 조회
            //ItemRepository리파지터리 인터페이스에 상속시킨 QuerydslPredicateExecutor<Item>인터페이스에서 제공하는
            //Page<T>   findAll(Predicate, Pageable)  메소드를 호출해
            // 조건문에 맞는 글 레코드(Item엔티티)들을 조회해서 Page객체에 담고 Page객체 자체를 반환 받습니다.
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable);

        // =====================[7. 전체 조회된 데이터 수 출력]=====================
        // 위에서 지정한 조건에 맞는 총 레코드 수를 출력 (총 몇 개가 조회됐는지 확인)
        System.out.println("total elements : " + itemPagingResult. getTotalElements ());

        // =====================[8. 조회된 실제 데이터 꺼내기]=====================
        // 조회된 5개의 데이터(Item 객체)를 리스트 형태로 꺼냅니다
        List<Item> resultItemList = itemPagingResult.getContent();

        // =====================[9. 하나씩 출력해보기]=====================
        // 조회된 상품 리스트를 반복문으로 하나씩 꺼내서 출력합니다
        for(Item resultItem: resultItemList){
            System.out.println(resultItem.toString());
        }
        /*

        `QueryDSL`을 사용하면 자바 코드로 조건을 정의하게 되는데,
        이 조건들이 내부적으로 JPQL (Java Persistence Query Language)로 변환되고,
        결국 SQL 쿼리로 실행됩니다.

        ----------------------------------------

        ## 1. 전체 흐름 요약
        ```
        자바 코드 (QueryDSL) → JPQL → SQL → DB 조회
        ```

        ----------------------------------------
        ####################### 예제 전체 코드

        QItem item = QItem.item;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(item.itemDetail.like("%테스트 상품 상세 설명%"));
        builder.and(item.price.gt(10003));
        builder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));

        Pageable pageable = PageRequest.of(0, 5);

        Page<Item> result = itemRepository.findAll(builder, pageable);
        ----------------------------------------

        ## 내부적으로 어떻게 동작하나?

        ### 1. QueryDSL 자바 코드

        위 코드는 자바 객체를 이용해 조건(Predicate)을 조립하고 있습니다.

                    builder.and(item.itemDetail.like("%테스트 상품 상세 설명%"));

                    -> 이 조건은 내부적으로 **Predicate 객체**로 저장됩니다.

        ----------------------------------------

        ### 2. Spring Data + QueryDSL이 JPQL로 변환

        이 조건들을 기반으로 Spring Data가 다음과 같은 **JPQL**을 생성합니다:

        **JPQL** 생성
                    SELECT i FROM Item i
                    WHERE i.itemDetail LIKE '%테스트 상품 상세 설명%'
                    AND i.price > 10003
                    AND i.itemSellStatus = 'SELL'

        ----------------------------------------

        ### 3. Hibernate가 SQL로 변환

        Spring Data JPA는 위 JPQL을 다시 다음과 같은 **SQL**로 변환해 DB에 실행합니다:

        **변환된 sql**
                    SELECT * FROM item
                    WHERE item_detail LIKE '%테스트 상품 상세 설명%'
                    AND price > 10003
                    AND item_sell_status = 'SELL'
                    LIMIT 5 OFFSET 0

                    > LIMIT/OFFSET은 페이징을 위해 추가된 SQL 문법입니다.

        ----------------------------------------
        ##  실행 순서 도식

        QueryDSL 자바 코드
          ↓
        BooleanBuilder로 조건 생성
          ↓
        QuerydslPredicateExecutor에서 findAll(builder, pageable)
          ↓
        Spring Data가 조건을 JPQL로 변환
          ↓
        Hibernate가 JPQL을 SQL로 번역
          ↓
        DB에 SQL 쿼리 실행

        ----------------------------------------
        ##  요약

        | 단계      | 설명                   |
        | ------- | -------------------- |
        | 자바 코드   | QueryDSL로 조건 정의      |
        | JPQL 변환 | Spring Data가 JPQL 생성 |
        | SQL 변환  | Hibernate가 SQL로 번역   |
        | DB 실행   | 실제 DB에서 쿼리 수행        |

        ---
        */
    }

}