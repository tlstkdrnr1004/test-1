package com.shop.repository;

import com.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/*

    JpaRepository인터페이스를 상속받아 ItemRepository 인터페이스 리파지터리 생성

    extends  JpaRepository<Item, Long>
            - 제네릭 타입의 첫번째 Item은  JPA가 관리할 엔티티 클래스타입 넣어주고,
            - 제네릭 타입의 두번쨰 Long은  JPA가 관리할 엔티티 클래스의 기본키 변수의 타입을 넣어줍니다.
            - JpaRepository 인터페이스는 기본적인 CRUD 및 페이징 처리를 위한 메소드가 정의돼 있습니다.

            표.  JpaRepository 인터페이스에 정의된 지원하는 메소드 예시
                메소드                                     기능
                <S extends T> save(S entity)            엔티티 저장 및 수정
                void delete(T entity)                   엔티티 삭제
                count()                                 엔티티 총 개수 조회후  반환
                Iterable<T>  findAll()                  모든 엔티티 조회

----------------------------------------------------------------------------------------------------
    QueryDslPredicateExecutor인터페이스에 대한 설명...
    - Predicate란 QueryDSL의 "조건문"을 의미합니다.
    - 이 인터페이스는 Spring Data JPA와 QueryDSL을 함께 쓸 때 자동으로 동적 쿼리를 사용할 수 있게 메소드들을 제공해주는 인터페이스.
    - QuerydslPredicateExecutor는 QueryDSL의 Predicate(조건문)을 이용해 자동으로 검색 기능을 제공하는 Spring Data JPA의 인터페이스입니다.
    - 언제 쓰는지?
        JpaRepository에 **QueryDSL의 Predicate(검색 조건문)을 조합해서 자동 검색 기능을 붙이고 싶을 때 사용합니다.

    - 표. QueryDslPredicateExecutor인터페이스에 정의 되어 있는 메소드들
        메소드                                             기능
        long    count(Predicate)                조건에 맞는 데이터의 총 개수 반환
        boolean  exists(Predicate)              조건에 맞는 데이터 존재 여부 반환
        Iterable  findAll(Predicate)            조건에 맞는 모든 데이터 조회해서 반환
        Page<T>   findAll(Predicate, Pageable)  조건에 맞는 페이징 처리 데이터 반환
        Iterable  findAll(Predicate, Sort)      조건에 맞는 정렬된 모든 데이터 조회해서 반환
        T         findOne(Predicate)            조건에 맞는 데이터 1개 조회해서 반환


    QueryDslPredicateExecutor인터페이스 또한  다중 상속받아  ItemRepository 인터페이스 리파지터리 생성

    public interface ItemRepository extends JpaRepository<Item, Long>,
                                             QuerydslPredicateExecutor<Item>

            -  이렇게 작성하면, ItemRepository에서 QueryDSL의 조건(Predicate)을 인자로 넣는 방식으로
               다양한 쿼리를 사용할 수 있습니다.

 ------------------------------------------------------
  Predicate이란?
    - Predicate는 QueryDSL의 조건문을 의미합니다.

 간단한 예시 :
       순서1. QueryDsl 자바코드를 아래 처럼 이렇게  eq로 조건문으로 만들고나서
            Predicate predicate = QItem.item.name.eq("노트북");

       순서2. JpaRepository<Item, Long> 인터페이스에서 제공하는  findAll메소드에서 조건문(predicate)을 넣어서 사용할수 있음
              Iterable<Item> items = itemRepository.findAll(predicate);
-------------------------------------
 전체 흐름 예시
    예시 :
    순서1. QueryDsl 자바코드를 아래 처럼 이렇게  eq 메소드와 and 메소드로 조건문으로 만들고나서
          Predicate 객체(조건문이 저장된 객체)로 반환받습니다.

                QItem item = QItem.item;

                Predicate predicate = item.name.eq("노트북")
                                      .and(item.price.gt(50000));

    순서2. JpaRepository<Item, Long> 인터페이스에서 제공하는  findAll메소드에서 조건문(predicate)을 넣어서 SQL 생성

            List<Item> result = (List<Item>) itemRepository.findAll(predicate);

   결과 아래와 같은 전체 SQL을 만들수 있음

    SELECT * FROM item
    WHERE name = '노트북' AND price > 50000;

----------------------------------------------------------------------------------------------------

마지막으로 ItemRepository 인터페이스에서 개발자가 커스텀으로 만든 ItemRepositoryCustom 인터페이스를 상속합니다.

이제 ItemRepository 인터페이스 내부에서 Querydsl 문법으로 구현한

상품 관리 페이지 목록을 조회해서 불러오는  getAdminItemPage()메소드를 사용할수 있습니다.

 */

//Item 테이블 관리  - ItemRepository 리파지터리
public interface ItemRepository extends JpaRepository<Item, Long>,
        QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {
/*
 쿼리 메소드  : - Spring DATA JPA 모듈에서 제공하는 핵심 기능 중 하나로 리파지터리 인터페이스에
                 간단한 네이밍 규칙을 이용하여 만드는 메소드.
                - 이 쿼리 메소드를 개발자가 직접 만들어 놓고 호출해서 실행하면 원하는 SQL문을 실행할 수 있습니다.

 쿼리 메소드 만드는 문법  find 사용

    find + 엔티티 이름 + By  +  변수이름

 */
//Item 엔티티 클래스 내부의 private String itemNm; 변수 를 살펴 보고!
// 상품명으로 데이터를 조회하기 위한  List<Item> findByItemNm(String itemNm); 메소드를 만들어 줍시다.
    //상품명으로 데이터를 조회하기 위해서  By 뒤에 변수명인 itemNm을 메소드의 이름에 붙여 줍니다.
    //엔티티 이름은 생략 가능 하므로 findItemByItemNm 대신 findByItemNm으로 메소드 명을 만들어 줍니다.
    //매개변수로는 검색할 때 사용할 상품명을 받을 매개변수 String itemNm으로 만들어 줍니다.
    List<Item> findByItemNm(String itemNm);
/*
    여러 개의 검색 조건 값을 이용하여 상품 검색시 키워드 OR 그리고 쿼리 메소드 만드는 문법  find 사용

    find + 엔티티 이름 + By  +  변수이름 + Or + 변수이름

            private String itemNm; //상품명
        private String itemDetail; //상품 상세 설명

*/
    //여러 개의 조건 값을 이용하여 상품을 검색할때 키워드(OR)과 변수이름을 메소드명으로 이어서 나열해서 쿼리 메소드를 만들면 된다.
    //상품을 상품명과 상품 상세 설명을 OR 조건을 이용하여 조회하는 쿼리 메소드 만들기
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);


    //참고. LessThan 키워드 의미  :
    //키워드           	의미	                SQL 조건
    // LessThan	        보다 작은 값 조회	        < 연산자

    //findBy뒤에 작성한 price컬럼에 저장된 가격이 매개변수 Integer price로 넘어온 상품 가격 보다 값이 작은 상품 데이터를 조회하는 쿼리 메소드 만들기
    List<Item> findByPriceLessThan(Integer price);

    //참고. 조회 결과를 OrderBy 키워드 의미 :
    //     오름 차순( OrderBy + 변수명 + Asc  )  또는 내림차순 정렬( OrderBy + 변수명 + Desc  ) 해서 조회 할수 있음
    //
    //findBy뒤에 작성한 price컬럼에 저장된 상품의 가격이
    // 매개변수 Integer price로 넘어온 상품 가격 보다 가격이 작은 상품 데이터를
    // 삼품의 가격이 높은 순으로 정렬해서 조회하는 쿼리 메소드 만들기
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    /*
    JPQL

        Spring Data JPA에서 제공하는 @Query 어노테이션을 이용하여 SQL과 유사한 JPQL(자바 객체 지향 쿼리 언어)
        이라는 객체지향 쿼리 언어를 통해 복잡한 쿼리도 처리가 가능합니다.
        SQL과 문법 자체가 유사 하기 때문에 기존에 SQL을 사용하셨던 분들은 쉽게 JPQL을 배울수 있습니다.

        SQL의 경우 데이터베이스의 테이블을 대상으로 쿼리를 수행하고,
        JPQL은 엔티티 객체를 대상으로 쿼리를 수행 합니다.
        즉! JPQL은 테이블이 아닌 엔티티 객체를 대상으로 DB의 테이블의 데이터를 검색하는 객체지향 쿼리 입니다.

        JPQL은  SQL을 추상화해서 사용하기 때문에 특정 DBMS SQL 문법에 의존하지 않습니다.
        즉! JPQL로 작성을 했다면 DBMS 종류가 변경되어도 애플리케이션이 영향을 받지 않습니다.
    */


    //@Query 어노테이션을 이용하여 상품 데이터를 조회하는 예제를 진행해 보겠습니다.
    //상품 상세 설명 내용을 매개변수로 받아 해당 상품 상세 내용을 상품 상세 설명에 포함하고 있는 데이터를 조회하며,
    //정렬 순서는 상품 가격이 높은 순으로 정렬해서 조회합니다.    
        //@Query 어노테이션 ( )괄호 안에 JPQL문법으로 작성한 쿼리문을 넣어줍니다.
        //form 뒤에는 조회할 Item테이블과 연결된 엔티티 클래스 Item을 작성해 주었고,
        //Item 엔티티 클래스로 부터 데이터를 select 조회 하겠다는 것을 의미 합니다.
        @Query("select i from Item i where i.itemDetail like %:itemDetail% " +
                "order by i.price desc")
        //@Param 어노테이션을 이용하여 매개변수로 전달받은 조회 열의 조건값인 상품 상세 설명 내용이 포함된 상품을 조회
        // 현재는 String itemDetail 매개변수를  like %  % 사이에  :itemDetail 로 상품 상세 설명 내용 조건값이 들어가도록 설정했음
        List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);





    /*
     주제 : nativeQuery = true를 설정하면 MySQL을 포함한 특정 DBMS의 SQL 문법을 그대로 사용할 수 있다

        [nativeQuery (특정 DBMS의 SQL)를 사용하는 이유와 주의할 점]

        - 일반적으로 @Query 안에는 JPQL(JPA 전용 쿼리 언어)을 사용합니다.
          JPQL은 자바의 엔티티(예: Item 클래스)를 대상으로 작성하는 쿼리이기 때문에,
          DBMS에 독립적이며, 객체 지향적으로 코드를 구성할 수 있습니다.

        - 하지만, 기존에 사용하던 **SQL 문법을 그대로 쓰고 싶을 때**가 있습니다.
          예를 들어, 복잡한 조인, DBMS의 함수 사용 등에서 기존 SQL을 그대로 쓰고자 할 수 있습니다.

        - 이럴 때는  nativeQuery = true를 설정하면 MySQL을 포함한 특정 DBMS의 SQL 문법을 그대로 사용할 수 있다는 의미입니다.

        - 단점은 해당 쿼리가 특정 DBMS(예: MySQL, Oracle 등)에 종속되므로
          DB가 바뀌면 쿼리도 함께 수정해야 하는 경우가 많아진다는 점입니다.

        - value 속성에는 기존 SQL 문법으로 작성한 쿼리를 입력합니다.
          nativeQuery 속성은 true로 설정해야 실제 SQL로 인식됩니다.
    */
    @Query(
            value = "SELECT * FROM item i " + // item 테이블에서 모든 컬럼(*)을 선택함. 테이블 별칭 i 사용
                    "WHERE i.item_detail LIKE %:itemDetail% " + // item_detail 컬럼에 사용자가 입력한 문자열이 포함된 행을 조회
                    "ORDER BY i.price DESC", // 조회된 결과를 가격(price) 기준으로 내림차순 정렬
            nativeQuery = true // JPQL이 아닌, 진짜 SQL 문법을 사용한다는 의미
    )
    List<Item> findByItemDetailByNative(
            @Param("itemDetail") String itemDetail
    /*
        @Param 어노테이션은 :itemDetail 자리(바인딩 파라미터)에
        매개변수 itemDetail 값을 바인딩해주는 역할을 합니다.

        예를 들어 itemDetail이 "테스트"라면,
        WHERE i.item_detail LIKE '%테스트%' 와 같은 SQL이 실행됩니다.
    */
    );


}

/*
 ===============================================================
 🌟 Spring Data JPA - 쿼리 메소드(Query Method) 총정리
 ===============================================================

 💡 1. 쿼리 메소드란?
 ---------------------------------------------------------------
 - Spring Data JPA가 제공하는 기능으로, SQL을 직접 작성하지 않고도
   "메소드 이름만으로" 원하는 데이터를 조회할 수 있는 방법입니다.

 - 즉, Repository 인터페이스에 메소드만 선언해 놓으면,
   Spring이 내부적으로 SQL을 자동 생성해서 실행해 줍니다.

 예:
    User findByUsername(String username);
    → 자동으로 SQL 생성: SELECT * FROM user WHERE username = ?;

 ---------------------------------------------------------------

 💡 2. 쿼리 메소드 사용 이유
 ---------------------------------------------------------------
 - SQL 작성 없이 간편하게 데이터 조회 가능
 - 코드가 간결해짐
 - 생산성 향상
 - 실수를 줄이고 유지보수가 쉬움

 ---------------------------------------------------------------

 💡 3. 쿼리 메소드 작성 문법
 ---------------------------------------------------------------
 ✅ 기본 구조:
    find + By + (엔티티 필드명) + (조건 키워드)

 ✅ find 대신 사용 가능한 키워드:
    find, get, read, query, search, stream 등 → 일반적으로는 "find"를 사용함

 ✅ 필드명이 2개 이상일 경우:
    And, Or 키워드로 연결

 ✅ 엔티티 이름은 생략 가능:
    예: findUserByUsername → 가능하지만, 보통 findByUsername 으로 작성

 ---------------------------------------------------------------

 💡 4. 쿼리 메소드 네이밍 규칙 키워드
 ---------------------------------------------------------------
 ⬇ 조건 키워드 목록 (주요 키워드)

    - And                     → 여러 조건을 연결
    - Or                      → 조건 중 하나 이상 만족
    - Between                 → 범위 검색 (BETWEEN A AND B)
    - LessThan               → 작은 값 조회 (<)
    - LessThanEqual          → 작거나 같은 (<=)
    - GreaterThan            → 큰 값 조회 (>)
    - GreaterThanEqual       → 크거나 같은 (>=)
    - After, Before          → 날짜에 주로 사용
    - IsNull, IsNotNull      → 값이 없는/있는 경우
    - Like                   → 문자열 패턴 검색
    - NotLike                → 패턴이 아닌 경우
    - StartingWith           → 시작하는 문자열 (LIKE ‘xxx%’)
    - EndingWith             → 끝나는 문자열 (LIKE ‘%xxx’)
    - Containing             → 포함하는 문자열 (LIKE ‘%xxx%’)
    - In                     → 리스트 안에 포함되는 값 (IN)
    - NotIn                  → 리스트에 포함되지 않은 값
    - True, False            → 불리언(boolean) 필드에 사용
    - IgnoreCase             → 대소문자 구분 없이 검색
    - OrderBy                → 정렬 조건

 예시:
    findByAgeGreaterThan(int age)
    findByUsernameAndAge(String name, int age)
    findByEmailContaining(String keyword)

 ---------------------------------------------------------------

 💡 5. 리턴 타입 지정
 ---------------------------------------------------------------
 ✅ 단일 객체 조회:
    User findByUsername(String username);

 ✅ 여러 개의 결과 리스트 조회:
    List<User> findByAge(int age);

 ✅ Optional로 감싸서 null 방지:
    Optional<User> findByEmail(String email);

 ✅ Stream 리턴도 가능:
    Stream<User> findByUsernameContaining(String name);

 ✅ Boolean 존재 여부 확인용:
    boolean existsByUsername(String username);

 ✅ 카운트 쿼리:
    long countByAgeGreaterThan(int age);

 ---------------------------------------------------------------

 💡 6. 정렬(Sorting)과 페이징(Paging)
 ---------------------------------------------------------------
 ✅ 정렬이 필요한 경우: 메소드 이름에 `OrderBy` 사용

 예:
    findByAgeGreaterThanOrderByUsernameAsc(int age);
    → 나이 큰 사용자 중에서 이름 오름차순 정렬

 ✅ 페이징 기능: Pageable 사용

 예:
    Page<User> findByAgeGreaterThan(int age, Pageable pageable);

    → 사용 예:
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username").ascending());
        userRepository.findByAgeGreaterThan(20, pageable);

 ---------------------------------------------------------------

 💡 7. 복잡한 쿼리는? → @Query 사용
 ---------------------------------------------------------------
 - 쿼리 메소드로 표현하기 어려운 경우, @Query 어노테이션으로 JPQL 또는 native SQL을 작성할 수 있습니다.

 예:
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.age > :age")
    List<User> findUserByCustomQuery(@Param("username") String username, @Param("age") int age);

 ---------------------------------------------------------------

 💡 8. 실전 예제 - 전체 흐름
 ---------------------------------------------------------------

 🔹 [엔티티 클래스]
    @Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;
        private String email;
        private int age;

        // 생성자, getter, setter 생략
    }

 🔹 [리포지토리 인터페이스]
    public interface UserRepository extends JpaRepository<User, Long> {

        // 이름으로 조회
        User findByUsername(String username);

        // 이름과 나이로 조회
        User findByUsernameAndAge(String username, int age);

        // 나이가 20 이상인 사용자 리스트
        List<User> findByAgeGreaterThanEqual(int age);

        // 이메일에 특정 문자열이 포함된 사용자
        List<User> findByEmailContaining(String keyword);

        // 존재 여부 확인
        boolean existsByUsername(String username);

        // 전체 사용자 수
        long countByAgeGreaterThan(int age);

        // 정렬된 리스트
        List<User> findByAgeGreaterThanOrderByUsernameDesc(int age);

        // 페이징 처리된 사용자
        Page<User> findByAge(int age, Pageable pageable);
    }

 ---------------------------------------------------------------

 ✅ 요약 정리
 ---------------------------------------------------------------
 - 쿼리 메소드는 메소드 이름만으로 데이터 조회 기능을 자동 생성
 - SQL 없이도 간단하고 빠르게 개발 가능
 - 단순한 조회에 매우 유용하지만,
   복잡한 조건, 조인 등은 @Query 또는 QueryDSL로 처리

 ===============================================================
*/









