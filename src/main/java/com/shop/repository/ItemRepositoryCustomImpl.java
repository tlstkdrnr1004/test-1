package com.shop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;


/*
 Querydsl을 Spring Data Jpa와 함께 사용하기 위해서는 사용자 정의 리포지토리 인터페이스를 정의 해야 합니다.
 총 3단계의 과정으로 구현 하면 됩니다.

 1. 사용자 정의 인터페이스 작성: 예) interface ItemRepositoryCustom {}
 2. 사용자 정의 인터페이스 구현: ItemRepositoryCustom 인터페이스를 구현하는 ItemRepositoryCustomImpl 클래스 작성
 3. Spring Data Jpa 리포지터리 인터페이스에서 사용자 정의 인터페이스 상속
*/

// 2. 사용자 정의 인터페이스 구현: ItemRepositoryCustom 인터페이스를 구현하는 ItemRepositoryCustomImpl 클래스 작성
// 주의할 점 : 클래스명 끝에 반드시 Impl 을 붙여 주어야 Spring Data JPA가 인식해서 정상적으로 동작합니다.
// QueryDsl에서는 BooleanExpression 이라는 where절에서 사용할 수 있는 조건 표현식을 지원합니다.
// BooleanExpression을 반환하는 메서드를 정의해두면, 여러 쿼리에서 재사용 가능하여 중복 코드를 줄일 수 있습니다.
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    // Querydsl에서 쿼리를 작성하고 실행할 때 사용하는 핵심 객체입니다.
    // JPAQueryFactory는 JPQL을 타입 안정성(type safety)을 보장하면서 쉽게 작성할 수 있게 도와줍니다.
    //
    // ✅ Querydsl이란?
    // - SQL을 자바 코드로 표현할 수 있게 해주는 오픈소스 라이브러리입니다.
    // - 쿼리를 문자열로 직접 작성하지 않고, 자바 객체처럼 작성할 수 있어서
    //   컴파일 시점에 문법 오류를 체크할 수 있습니다.
    //
    // ✅ 왜 JPAQueryFactory를 사용하는가?
    // - 쿼리 생성, where 조건 결합, 페이징(offset/limit), 정렬 등 모든 Querydsl 기능을 지원합니다.
    // - 기존 JPA EntityManager로 JPQL을 직접 작성하는 방식보다 훨씬 안전하고 직관적입니다.
    //
    // ✅ final 키워드를 사용하는 이유?
    // - JPAQueryFactory는 생성자에서 주입받아 한 번만 초기화되며,
    //   그 이후에는 변경되지 않도록 하기 위해 final로 선언합니다.
    //
    // 🔽 예시로 이런 식으로 사용됩니다:
    //   queryFactory
    //     .selectFrom(QItem.item)
    //     .where(QItem.item.itemNm.like("%샘플%"))
    //     .fetch();
    //
    // 즉, 이 필드는 Querydsl 기반의 동적 쿼리 작성을 위한 핵심 역할을 합니다.
    private final JPAQueryFactory queryFactory;


    // ✅ 생성자(Constructor)
    // - ItemRepositoryCustomImpl 클래스의 객체가 생성될 때 호출됩니다.
    // - 생성자의 역할은 JPAQueryFactory 객체를 초기화하여,
    //   이후 이 클래스 내에서 Querydsl을 사용한 동적 쿼리 생성을 가능하게 만드는 것입니다.
    public ItemRepositoryCustomImpl(EntityManager em) {

        // ✅ EntityManager란?
        // - JPA(Java Persistence API)의 핵심 인터페이스입니다.
        // - 데이터베이스와의 연결을 관리하고,
        //   엔티티(Entity) 객체를 데이터베이스에 저장하거나 조회할 수 있게 해줍니다.
        //
        // ✔ 쉽게 말하면, EntityManager는 "데이터베이스와 자바 객체 간의 다리" 역할을 합니다.

        // ✅ JPAQueryFactory 생성
        // - Querydsl에서 제공하는 JPAQueryFactory는 EntityManager를 기반으로 동작합니다.
        // - 따라서 JPAQueryFactory 객체를 생성할 때 반드시 EntityManager를 전달해 주어야 합니다.
        //
        // 🧠 왜 이렇게 하냐면?
        // - Querydsl 내부에서 EntityManager를 사용하여 SQL 쿼리를 실행하거나 트랜잭션을 처리하기 때문입니다.
        //
        // ▶ 이 생성자에서는 전달받은 em(EntityManager)을 이용해서
        //   queryFactory를 생성하고, 해당 클래스를 통해 Querydsl 쿼리를 작성할 수 있는 준비를 마칩니다.
        this.queryFactory = new JPAQueryFactory(em);
    }


    // ✅ 상품 판매 상태 조건 생성 메서드
    // 이 메서드는 사용자가 선택한 상품 판매 상태(ItemSellStatus)에 따라
    // Querydsl의 BooleanExpression(조건절)을 동적으로 생성해주는 역할을 합니다.
    //
    // ✅ BooleanExpression이란?
    // - Querydsl에서 사용하는 where 조건 표현식입니다.
    // - SQL의 WHERE 절에서 "조건식"을 정의할 때 사용합니다.
    // - 예: QItem.item.itemSellStatus.eq(ItemSellStatus.SELL) → "where item_sell_status = 'SELL'" 조건 생성
    //
    // ✅ 동작 방식:
    // 1. 사용자가 아무 조건도 선택하지 않았다면 (즉, 판매 상태가 null이라면)
    //    → 전체 상품을 조회해야 하므로 null을 반환하여 조건을 무시하도록 합니다.
    // 2. 사용자가 판매 중(SELL)이나 품절(SOLD_OUT)과 같은 상태를 선택한 경우
    //    → 해당 상태와 일치하는 조건을 생성하여 반환합니다.
    //
    // ✅ Querydsl에서 where 절에 null을 넣으면?
    // - Querydsl은 내부적으로 null 조건을 무시합니다.
    // - 따라서 where(...) 안에 여러 조건이 들어가더라도,
    //   이 메서드가 null을 반환하면 해당 조건은 쿼리에 포함되지 않습니다.
    //   → 조건이 유연하게 적용됨 = 조건적 검색을 구현할 수 있음
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        // 판매 상태 조건이 null이 아니라면 "itemSellStatus = 선택한 상태" 조건을 반환하고,
        // null일 경우 조건을 적용하지 않도록 null을 반환합니다.
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
                                                // where "item_sell_status = 'SELL'" 조건 생성
    }


    // ✅ 날짜 필터 조건 생성 메서드
    // 사용자가 선택한 검색 날짜 범위(searchDateType)에 따라
    // 해당 기간 이후에 등록된 상품만 조회할 수 있도록 조건을 생성합니다.
    //
    // ✅ 이 메서드가 하는 일:
    // - "최근 1일, 1주, 1개월, 6개월 이내 등록된 상품"만 검색할 수 있도록
    //   기준 날짜(LocalDateTime)를 현재 시각 기준으로 계산합니다.
    // - 계산된 기준 날짜 이후에 등록된 상품만 조회되도록
    //   Querydsl의 BooleanExpression 조건(QItem.item.regTime.after(...))을 반환합니다.
    //
    // ✅ where 절에서 사용되는 BooleanExpression은:
    // - Querydsl의 조건 표현식 (SQL의 WHERE 절 역할)
    // - null을 반환하면 해당 조건은 쿼리에 포함되지 않음 (즉, 전체 조회)
    //
    // ✅ 입력값 searchDateType 의미:
    //   "all" 또는 null  → 전체 기간 조회 (날짜 조건 없음)
    //   "1d"             → 하루 전부터 현재까지 등록된 상품
    //   "1w"             → 일주일 전부터 현재까지 등록된 상품
    //   "1m"             → 한 달 전부터 현재까지 등록된 상품
    //   "6m"             → 6개월 전부터 현재까지 등록된 상품
    //
    // ✅ 예시:
    //   searchDateType = "1w" → dateTime = 현재 시각 - 1주일
    //                        → WHERE item.reg_time > dateTime
    private BooleanExpression regDtsAfter(String searchDateType) {

        // ✅ 현재 시점을 기준으로 검색 시작 기준 시간을 계산하기 위한 LocalDateTime 변수 생성
        // → 이후에 사용자가 선택한 날짜 필터(예: 최근 1일, 1주 등)에 따라 날짜를 감소시킴
        LocalDateTime dateTime = LocalDateTime.now();
        // 예: 현재가 2025-06-12 14:00이라면, dateTime은 "2025-06-12 14:00"부터 시작됨

        // ✅ 사용자가 "전체 기간 조회"를 선택했거나, 아무 값도 입력하지 않은 경우(null):
        // 날짜 조건을 적용하지 않기 위해 null을 반환 → where 절에서 필터링 조건 없이 모든 데이터를 조회
        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;  // Querydsl에서는 null 조건은 무시됨 → 전체 조회
        }

        // ✅ 사용자가 최근 1일(1d) 이내의 상품을 조회하고 싶을 경우:
        // 현재 시각으로부터 1일(24시간)을 빼서 기준 시각으로 설정
        else if (StringUtils.equals("1d", searchDateType)) {
            // 예: 2025-06-12 14:00 → 2025-06-11 14:00
            dateTime = dateTime.minusDays(1);  // 하루 전 시각 계산
        }

        // ✅ 사용자가 최근 1주(1w) 이내의 상품을 조회하고 싶을 경우:
        // 현재 시각으로부터 1주일(7일)을 빼서 기준 시각으로 설정
        else if (StringUtils.equals("1w", searchDateType)) {
            // 예: 2025-06-12 14:00 → 2025-06-05 14:00
            dateTime = dateTime.minusWeeks(1);  // 7일 전 시각 계산
        }

        // ✅ 사용자가 최근 1개월(1m) 이내의 상품을 조회하고 싶을 경우:
        // 현재 시각으로부터 1개월(30일 내외)을 빼서 기준 시각으로 설정
        else if (StringUtils.equals("1m", searchDateType)) {
            // 예: 2025-06-12 14:00 → 2025-05-12 14:00
            dateTime = dateTime.minusMonths(1);  // 한 달 전 시각 계산
        }

        // ✅ 사용자가 최근 6개월(6m) 이내의 상품을 조회하고 싶을 경우:
        // 현재 시각으로부터 6개월을 빼서 기준 시각으로 설정
        else if (StringUtils.equals("6m", searchDateType)) {
            // 예: 2025-06-12 14:00 → 2024-12-12 14:00
            dateTime = dateTime.minusMonths(6);  // 6개월 전 시각 계산
        }


        // 기준 시간 이후로 등록된 상품만 검색하도록 조건 생성
        // 즉, WHERE item.reg_time > dateTime 조건 생성.
        return QItem.item.regTime.after(dateTime);
    }


    /**
     * 사용자가 선택한 검색 기준(searchBy)에 따라
     * 해당 필드(itemNm 또는 createdBy)에 검색어(searchQuery)를 포함하는 조건을 생성합니다.
     * 이 조건은 Querydsl의 where 절에 사용되어 필터링된 데이터를 조회하는 데 쓰입니다.
     *
     * 예를 들어,
     *   - 검색 기준이 "itemNm"이고 검색어가 "노트북"이라면,
     *     item_nm LIKE '%노트북%' 조건이 생성되어 상품명에 '노트북'이 포함된 상품만 조회됩니다.
     *   - 검색 기준이 "createdBy"이고 검색어가 "admin"이라면,
     *     created_by LIKE '%admin%' 조건이 생성되어 'admin'이 작성한 상품만 조회됩니다.
     *
     * @param searchBy     검색 기준이 되는 필드명. 일반적으로 "itemNm" 또는 "createdBy"가 들어옵니다.
     * @param searchQuery  사용자가 입력한 검색어. 해당 텍스트가 필드에 포함되어 있는지를 검사합니다.
     * @return             BooleanExpression (Querydsl 조건 객체).
     *                     해당 조건을 where 절에 포함시켜서 원하는 필드에서 검색할 수 있게 합니다.
     *                     searchBy가 예상 외의 값이거나 null이면 null 반환 (조건 없이 전체 검색 허용).
     */
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {

        // 🔍 검색 기준이 "itemNm"일 경우: 상품명(itemNm)을 기준으로 검색
        // 문자열이 정확히 "itemNm"와 일치하는지 비교합니다.
        // 이때 Apache Commons Lang의 StringUtils.equals()는 null-safe 비교를 지원합니다.
        // 즉, null 값이 와도 NullPointerException이 발생하지 않음.
        if (StringUtils.equals("itemNm", searchBy)) {

            // QItem.item은 Querydsl이 자동으로 생성한 Q도메인 객체입니다.
            // itemNm은 Item 엔티티의 상품명 필드를 의미합니다.
            // like()는 SQL의 LIKE 연산자처럼 부분 일치를 검사합니다.
            // "%검색어%" 형태로 작성하면 해당 검색어가 문자열의 **어디에 포함되었는지**를 검사합니다.
            // 예: "%노트북%" → "게이밍노트북", "노트북가방", "중고노트북" 모두 검색됩니다.
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        }

        // 🧑 검색 기준이 "createdBy"일 경우: 등록한 사용자의 ID(createdBy)를 기준으로 검색
        // 작성자 정보는 일반적으로 문자열로 저장되며, 로그인된 사용자 ID일 가능성이 높습니다.
        else if (StringUtils.equals("createdBy", searchBy)) {

            // createdBy는 Item 엔티티의 작성자 ID 필드입니다.
            // like() 조건을 사용하여, 작성자 ID에 검색어가 포함된 항목을 찾습니다.
            // 예: "%admin%" → "admin", "admin001", "superadmin" 등 포함된 모든 경우 검색됩니다.
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        // ❗ searchBy 값이 예상한 값이 아닐 경우 (예: null, 오타 등)
        // 조건을 생성하지 않고 null을 반환합니다.
        // Querydsl의 where절에서 null 조건은 자동으로 무시되므로,
        // 아무 필터도 적용되지 않고 전체 데이터를 조회하게 됩니다.
        return null;
    }


    /**
     * 🛠 관리자 페이지에서 조건에 맞는 상품 목록을 검색하고, 페이징 처리하여 반환하는 메서드입니다.
     *
     * ✅ 이 메서드의 주요 역할
     * 1. 관리자 페이지에서 사용자가 입력한 검색 조건(등록일, 상태, 키워드 등)에 맞는 상품을 필터링하여 조회합니다.
     * 2. 결과 데이터를 페이지 단위로 나누어 가져오며, 현재 페이지에 해당하는 데이터만 조회합니다.
     * 3. 전체 조건에 맞는 상품 개수를 count(*)로 함께 조회하여, 페이징 UI를 구성할 수 있도록 합니다.
     * 4. 조회된 상품 리스트와 총 개수, 페이징 정보를 Page 객체로 포장하여 반환합니다.
     *
     * ✅ 기술 요약
     * - Querydsl: 타입 안전한 쿼리 작성이 가능한 자바 기반 SQL 빌더로, JPQL을 동적으로 생성합니다.
     * - JPAQueryFactory: Querydsl 쿼리의 시작점 역할을 하며, EntityManager를 기반으로 실행됩니다.
     * - Pageable: 스프링 데이터에서 제공하는 페이징 설정 정보 객체 (page, size, sort 포함)
     * - PageImpl: 조회 결과와 페이징 정보를 스프링 표준 Page 객체로 묶어주는 구현체
     *
     * ⚙ SQL 생성 흐름
     *  - queryFactory로 작성된 Querydsl 문법 → JPQL로 변환 → Hibernate가 SQL로 변환 → DB 실행
     *
     * @param itemSearchDto 검색 조건 DTO (등록일 범위, 판매 상태, 검색 키워드 등)
     * @param pageable 페이지 정보 (페이지 번호, 페이지 크기, 정렬 조건 등)
     * @return Page<Item> 조건에 맞는 상품을 페이지 단위로 포함한 결과 객체
     */
    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // ✅ [1단계] 조건에 맞는 상품 목록 데이터 조회
        // Querydsl은 where절에 null을 전달하면 해당 조건을 자동으로 무시함 → 매우 유연한 동적 쿼리 작성 가능

        List<Item> content = queryFactory
                .selectFrom(QItem.item)  // FROM item (Q타입 사용으로 컴파일 시점에 컬럼 타입 체크 가능)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),              // ⏳ 등록일 필터링 (최근 1일, 1주 등)
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),     // 📦 판매 상태 필터링 (SELL, SOLD_OUT 등)
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()) // 🔍 상품명 or 작성자 검색
                )
                .orderBy(QItem.item.id.desc())      // 최신 상품이 먼저 나오도록 ID 기준 내림차순 정렬
                .offset(pageable.getOffset())       // 조회 시작 index 행 위치 계산: (페이지 번호 - 1) * 페이지 크기
                .limit(pageable.getPageSize())      // 조회 될 행 수 (한 페이지에 보여줄 상품 수)
                .fetch();                           // 📌 실제 SQL 실행 및 결과 조회
        //
        // ⛳ 예시로 생성되는 SQL 형태:
       /*
            SELECT item.*
            FROM item
            ORDER BY item.id DESC
            LIMIT 0, 5;
        */
        // SELECT * FROM item
        // WHERE created_date >= '2024-06-01'
        //   AND sell_status = 'SELL'
        //   AND item_nm LIKE '%키보드%'
        // ORDER BY id DESC
        // LIMIT 10 OFFSET 20;

        // ✅ [2단계] 조건에 맞는 전체 상품 수 조회 (페이징을 위한 전체 개수 필수)
        long total = queryFactory
                .select(Wildcard.count)             // SELECT COUNT(*)
                .from(QItem.item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),              // 위와 동일한 조건 반복
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                )
                .fetchOne();                        // 단일 숫자(count) 조회

        // ⛳ 이때 생성되는 SQL 예시:
        /*
        Hibernate:
                select
                    count(*)
                from
                    item
         */
        // SELECT COUNT(*) FROM item
        // WHERE created_date >= '2024-06-01'
        //   AND sell_status = 'SELL'
        //   AND item_nm LIKE '%키보드%'

        // ✅ [3단계] 결과를 Page 객체로 감싸서 반환
        // - PageImpl(List content, Pageable pageable, long total)
        //   → 조회 결과 + 페이징 설정 + 전체 개수 모두 포함
        return new PageImpl<>(content, pageable, total);
                             //List<Item>
    }



    /**
     * 🔍 상품명(itemNm) 필드를 기준으로 LIKE 검색 조건을 생성하는 메서드입니다.
     *
     * ✅ 동작 방식 설명:
     * - 사용자가 검색창에 입력한 키워드(searchQuery)를 상품명(itemNm)에 포함된 데이터와 비교하여 필터링할 수 있도록 합니다.
     * - 예를 들어, 사용자가 "키보드"를 입력하면 → SQL 조건: WHERE item_nm LIKE '%키보드%'
     *
     * ✅ null 처리 중요 포인트:
     * - Querydsl에서는 where 조건에 null을 넣으면 해당 조건은 **자동으로 무시**됩니다.
     *   → 따라서 검색어가 비어 있으면 조건 자체를 만들지 않음(null 반환)
     *   → 결과적으로 조건 없는 전체 상품 조회처럼 동작하게 됨
     *
     * ✅ 사용 예:
     * .where(itemNmLike("키보드"))  → WHERE item_nm LIKE '%키보드%'
     * .where(itemNmLike(""))        → 조건 생략 (null 반환)
     *
     * @param searchQuery 검색창에서 입력된 키워드 (예: "키보드", "마우스")
     * @return BooleanExpression - Querydsl의 WHERE 절에 들어갈 조건. null이면 조건 생략됨.
     */
    private BooleanExpression itemNmLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery)
                ? null  // 검색어가 비어 있으면 조건 생략 (Querydsl에서 자동으로 무시)
                : QItem.item.itemNm.like("%" + searchQuery + "%"); // 검색어를 포함하는 상품명 조건 생성
    }


    /**
     * 🛍️ 메인 페이지에 보여질 상품 리스트를 조회하는 메서드입니다.
     *
     * ✅ 주요 기능 요약
     * - 메인에서 보여줄 상품 목록을 조건에 맞게 조회합니다.
     * - 대표 이미지가 있는 상품만 대상으로 하며, 검색어가 있으면 상품명 기준으로 필터링합니다.
     * - 페이징 처리를 적용하여, 한 번에 너무 많은 상품을 가져오지 않도록 합니다.
     * - 결과는 MainItemDto 형태로 변환하여 반환합니다 (클라이언트에 필요한 데이터만 추려서 전달).
     *
     * ✅ 사용 기술
     * - Querydsl: SQL을 자바 코드로 안전하게 작성할 수 있는 도구
     * - DTO 프로젝션: 원하는 필드만 뽑아서 DTO로 변환 (select new QMainItemDto(...))
     * - PageImpl: 결과 리스트와 전체 개수를 포함한 페이징 객체
     *
     * @param itemSearchDto 검색어 정보가 담긴 DTO 객체
     * @param pageable 페이지 번호, 크기 등을 담은 객체
     * @return Page<MainItemDto> 메인 상품 리스트 페이징 결과
     */
    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // Q타입 객체들: Querydsl로 테이블을 코드처럼 다루기 위한 클래스
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        // ✅ [1단계] 실제 상품 목록 데이터 조회
        // - from(itemImg): itemImg 테이블(이미지 테이블)을 기준으로 시작
        // - join(itemImg.item, item): 이미지 테이블과 상품 테이블(item)을 조인
        // - where(itemImg.repimgYn.eq("Y")): 대표 이미지인 경우만 필터링
        // - where(itemNmLike(...)): 검색어가 있으면 상품명 LIKE 조건 추가
        // - select(new QMainItemDto(...)): 원하는 필드만 DTO로 변환하여 조회
        // - offset/limit: 페이징 처리
        List<MainItemDto> content = queryFactory
                .select(new QMainItemDto(
                        item.id,           // 상품 ID
                        item.itemNm,       // 상품명
                        item.itemDetail,   // 상품 상세 설명
                        itemImg.imgUrl,    // 대표 이미지 URL
                        item.price         // 가격
                ))
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repimgYn.eq("Y"))  // 대표 이미지 조건만 가져오기
                .where(itemNmLike(itemSearchDto.getSearchQuery())) // 검색어가 있을 경우 조건 추가
                .orderBy(item.id.desc())           // 최신 상품부터 조회
                .offset(pageable.getOffset())      // 시작 위치
                .limit(pageable.getPageSize())     // 한 페이지에 가져올 상품 수
                .fetch();                          // 실제 쿼리 실행
        /*
        Hibernate:
                select
                    ii1_0.item_id,
                    i1_0.item_nm,
                    i1_0.item_detail,
                    ii1_0.img_url,
                    i1_0.price
                from
                    item_img ii1_0
                join
                    item i1_0
                        on i1_0.item_id=ii1_0.item_id
                where
                    ii1_0.repimg_yn=?
                order by
                    i1_0.item_id desc
                limit
                    ?, ?
         */


        // ✅ [2단계] 전체 데이터 개수 조회 (총 페이지 수 계산용)
        // - 위와 동일한 조건으로 count(*) 쿼리 실행
        long total = queryFactory
                .select(Wildcard.count)               // SELECT COUNT(*)
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repimgYn.eq("Y"))      // 대표 이미지 조건
                .where(itemNmLike(itemSearchDto.getSearchQuery())) // 검색어 조건
                .fetchOne();                          // 개수 1개만 가져오기
    /*
        Hibernate:
            select
                count(*)
            from
                item_img ii1_0
            join
                item i1_0
                    on i1_0.item_id=ii1_0.item_id
            where
                ii1_0.repimg_yn=?
    */

        // ✅ [3단계] Page 객체로 결과 감싸기
        // - content: 실제 조회된 상품 리스트
        // - pageable: 요청한 페이지 정보
        // - total: 전체 개수
        return new PageImpl<>(content, pageable, total);
    }

}