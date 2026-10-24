package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;

/*
엔티티란? 데이터베이스의 테이블에 대응하는 클래스.
        @Entity가 붙은 클래스는 JPA에서 관리하며 엔티티라고 합니다.

 [표] 엔티티 매핑 관련 어노테이션
     @Entity  -  클래스의 상단에 작성 해 놓으면  JAP에 엔티티 클래스라는 것을 알려주는 어노테이션
                 엔티티 클래스는 반드시 테이블의 기본키 제약조건이 설정된 컬럼과 대응되게 기본키 제약 조건을 @Id 어노테이션으로 설정해야함.

     @Table - 엔티티 클래스와 매핑(연결)할 테이블명 지정 하는 어노테이션
     @Id    - 테이블의 기본키 제약조건이 설정된 컬럼과 대응되게 기본키 제약 조건을 @Id 어노테이션으로 변수에 설정하는 어노테이션
     @GeneratedValue - 기본키 제약조건이 설정되는 컬럼의 값을 생성하는 전략 명시.
                       생성 전략 총 4가지
                       1. GenerationType.AUTO(default) : JPA 구현체 하이버네이트가 자동으로 생성 전략 결정
                       2. GenerationType.IDENTITY : 기본키 생성을 사용하는 DMBS종류의 DB에 위임
                                                    예) MySQL DMBS 경우  AUTO_INCREMENT를 사용하여
                                                        기본키 제약조건이 설정된 컬럼의 값을 자동으로 1씩 증가 생성
                       3. GenerationType.SEQUENCE : 오라클의 시퀀스 오브젝트를 이용한
                                                    기본키 의 초기값, 증가값, 최댓값, 최솟값 설정후
                                                    기본키 제약조건이 설정된 컬럼의 값을 자동으로 1씩 증가 생성
                                                    @SequenceGenerator를 사용하여 시퀀스 등록 필요.
                       4. GenerationType.TABLE : 키 생성용 테이블 사용. @TableGenerator 필요.

      @Column - 변수와 테이블의 컬럼 매핑(연결)
                속성                      설명                  설정하지 않으면 자동 설정되는 기본값
                name            변수와 매핑할 컬럼의 이름 설정       객체의 변수 이름
                unique(DDL)     유니크 제약 조건 설정
                insertable      insert 가능 여부 설정             true
                updatable       update 가능 여부 설정             true
                length          String 타입의 문자 길이 제약조건 설정    255
                nullable(DDL)   null 값의 허용 여부 설정,
                                false설정시 DDL 생성 시에 not null 제약조건 추가
                columnDefinition    데이터베이스 컬럼 정보 직접 기술
                                    예
                                         @Column(columnDefinition = "varchar(5) default '10' not null")
                precision, scale(DDL)
                                    BigDecimal 타입에서 사용(BigInteger 가능)
                                    percision은 소수점을 포함한 전체 자리수이고,
                                    scale은 소수점 자리수.
                                    Double과 float 타입에는 적용되지 않음

 */

//Item 클래스를 엔티티로 선언합니다.
@Entity
//@Table 어노테이션을 통해 어떤 테이블과 매핑할지를 지정합니다.
//item 테이블의 컬럼들과 연결(매핑) 되도록 name속성을 item테이블명으로 지정합니다.
@Table(name="item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity {//Item 상품 엔티티

    //item테이블의  기본키 제약조건이 item_id 컬럼에 설정되게
    // 연결(매핑)될 멤버변수  private Long id;  에 @Id 를 붙여줍니다
    @Id
    //@Column 를 통해  item 엔티티 클래스의 private Long id; 변수와
    //item 테이블의 item_id 컬럼이 연결(매핑)되도록 설정
    @Column(name="item_id")
    //GenerationType.AUTO를 사용해서 기본키 id 컬럼 값을 생성하면
    //벤드사 별 DBMS에 의존하지 않고 기본키 할당하는 방법으로, JPA 구현체가
    //GenerationType.IDENTITY, GenerationType.SEQUENCE, GenerationType.TABLE 기본키 id 컬럼 값 생성전략 중
    //하나를 자동으로 선택합니다. 따라서 DBMS 종류가 변경되더라도 코드를 수정할 필요가 없습니다.
    //-> 우리는 MySQL8 DBMS 이니 GenerationType.IDENTITY를 자동 선택후
    //-> AUTO_INCREMENT를 사용하여 기본키 제약조건이 설정된 컬럼의 값을 자동으로 1씩 증가 생성
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;       //상품 코드

    //@Column 어노테이션의 nullable속성을 이용해서 항상 값이 저장되어야 하는 변수는 not null 제약조건을 설정함
    //String 변수는 default 값으로 255가 설정돼 있습니다.
    //각 String 변수마다 필요한 길이를 length 속성에 default값을 셋팅합니다.
    @Column(nullable = false, length = 50)
    private String itemNm; //상품명

    @Column(name="price", nullable = false)
    private int price; //가격

    @Column(nullable = false)
    private int stockNumber; //재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; //상품 판매 상태


    // ✅ 상품 정보를 수정(변경, 업데이트)하는 메서드
    // - 화면(Form)에서 전달된 ItemFormDto 객체를 사용하여
    // - 현재 Item 엔티티의 필드 값을 갱신
    public void updateItem(ItemFormDto itemFormDto) {

        // ✅ 상품명 변경
        // - ItemFormDto에서 받은 새로운 상품명을 현재 Item 객체(this)의 itemNm에 대입
        this.itemNm = itemFormDto.getItemNm();

        // ✅ 상품 가격 변경
        // - 사용자가 입력한 새 가격으로 price 필드를 수정
        this.price = itemFormDto.getPrice();

        // ✅ 재고 수량 변경
        // - 새로운 재고 수를 stockNumber 필드에 반영
        this.stockNumber = itemFormDto.getStockNumber();

        // ✅ 상품 상세 설명 변경
        // - 상세 정보(description)도 갱신
        this.itemDetail = itemFormDto.getItemDetail();

        // ✅ 상품 판매 상태 변경 (SELL or SOLD_OUT)
        // - 판매 중/품절 여부를 사용자 입력 값으로 업데이트
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }


   //removeStock() 메서드는 상품의 재고를 감소시키는 역할을 합니다.
   // 즉, 사용자가 상품을 주문했을 때, 해당 주문 수량만큼 재고를 차감하는 핵심 로직입니다.
    public void removeStock(int stockNumber) {

        // ✅ 현재 재고(this.stockNumber)에서 차감할 수량(stockNumber)을 뺀 결과를 계산
        int restStock = this.stockNumber - stockNumber;

        // ❗ 재고를 뺀 결과가 음수이면, 즉 주문 수량이 현재 재고보다 많으면 예외 발생
        if (restStock < 0) {
            // ❌ 커스텀 예외를 던져 주문을 막고, 현재 재고 수량을 에러 메시지로 전달
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }

        // ✅ 재고가 충분할 경우 → 재고 수량을 차감한 값으로 갱신
        this.stockNumber = restStock;
    }

    //addStock() 메서드는 **상품의 재고를 추가(복구)**하는 역할을 합니다.
    public void addStock(int stockNumber){          // ✅ 외부에서 전달받은 수량만큼 재고를 증가시키는 public 메서드
        this.stockNumber += stockNumber;            // ✅ 현재 객체(this)의 재고 필드에 인자로 받은 수량을 더함
    }


}