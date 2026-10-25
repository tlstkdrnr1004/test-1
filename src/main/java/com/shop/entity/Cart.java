package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;

/**
 * 장바구니 엔티티 (Cart)
 *
 * 비유: 마트에 들어갈 때 입구에서 받는 "장바구니" 그 자체입니다.
 *       각 고객(회원)은 하나의 장바구니를 가지며, 이 안에 여러 상품(CartItem)을 담을 수 있습니다.
 *       장바구니는 회원과 1:1 관계이고, 장바구니 상품(CartItem)과는 1:N 관계입니다.
 *
 * DB 테이블: cart 테이블과 매핑됩니다.
 *   - cart_id (PK): 장바구니 고유 번호
 *   - member_id (FK): 이 장바구니의 소유자 회원 번호
 *
 * BaseEntity 상속: 등록일, 수정일, 등록자, 수정자 정보가 자동으로 관리됩니다.
 */
@Entity // 이 클래스가 JPA 엔티티임을 선언합니다. DB 테이블과 1:1로 매핑되는 클래스입니다.
@Table(name = "cart") // 매핑할 DB 테이블 이름을 "cart"로 지정합니다
@Getter @Setter // Lombok: 모든 필드에 대해 getter/setter 메서드를 자동 생성합니다
@ToString // Lombok: 객체를 문자열로 출력할 때 필드 값을 보여주는 toString() 메서드를 자동 생성합니다
public class Cart extends BaseEntity {

    @Id // 이 필드가 테이블의 기본키(Primary Key)임을 선언합니다. 비유: 장바구니의 고유 번호표
    @Column(name = "cart_id") // DB 테이블에서 이 필드가 매핑되는 컬럼명을 "cart_id"로 지정합니다
    @GeneratedValue(strategy = GenerationType.AUTO) // 기본키 값을 자동으로 생성합니다 (1, 2, 3... 순차적으로)
    private Long id; // 장바구니의 고유 식별 번호

    /*
    주제 : 일대일 단방향 매핑하기

        Cart 테이블(엔티티)              Member 테이블(엔티티)
        --------------         -------------------------------
        cart_id (PK)            member_id (PK)
        member_id (FK)          name
                                email
                                password
                                address
                                role

     설명.  소스코드를 아래와 같이 다 작성 한 후 회원(Member) 엔티티를 보면
            회원 엔티티에는 장바구니(Cart) 엔티티와 관련된 소스가 전혀 없다는 것을 확인 하셨을 겁니다.
            즉, 장바구니(Cart) 엔티티가 일방적으로 회원(Member)엔티티를 참조하고 있습니다.
            장바구니와 회원은 일대일 매핑돼 있으며, 장바구니 엔티티가 회원 엔티티를 참조하는 일대일 단방향 매핑 입니다.

    @OneToOne
    - @OneToOne 어노테이션을 이용해 회원 엔티티와 일대일 매핑을 합니다.
    - @OneToOne(fetch = FetchType.EAGER)  // <--- 즉시 로딩  기본 디폴트

| 조회 전략            | 의미                               | 사용 상황                 |
| ---------------    | -------------------------         | --------------------- |
| `EAGER` (즉시 로딩) | 연관된 엔티티를 즉시 같이 조회함          | 자주 함께 조회되는 연관 엔티티인 경우 |
| `LAZY` (지연 로딩)  | 실제로 사용할 때까지는 DB에서 조회하지 않음 | 불필요한 조인을 줄이고 성능 향상    |


    @JoinColumn(name="member_id")
    - @JoinColumn 어노테이션을 이용해 매핑할 왜래키를 지정합니다.
      name속성에는 매핑할 왜래케의 이름을 설정합니다.
       @JoinColumn 어노테이션의  name속성을 명시하지 않으면 JPA가 알아서 ID를 찾지만
       원하는 대로 생성되지 않을수 있기때문에 직접  @JoinColumn(name="member_id") 로 지정 합니다.
     */
    //@OneToOne   //@OneToOne(fetch = FetchType.EAGER)  // <--- 즉시 로딩  기본 디폴트
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;
    /*
        서버를 실행하면 콘솔창에 cart 테이블이 생성되는 쿼리문이 실행되는 것을 볼수 있습니다.

        Hibernate:
                create table cart (
                    cart_id bigint not null,
                    member_id bigint,
                    primary key (cart_id)
                );

        cart 테이블은  member_id  컬럼을 외래키(foregin key)로 갖습니다.
        테이블을 먼저 생성하는 쿼리 문이 실행되고 member_id를  외래키로 지정하는 쿼리 문이 실행 됩니다.

        Hibernate:
                alert  table cart
                add constraint FK
                foreign key (member_id)
                references member

       장바구니(Cart) 엔티티와 회원(Member) 엔티티의 매핑이 완료됐습니다. 이렇게 일대 일 매핑을 맺어 주면
       장바구니 엔티티를 조회하면서 회원 엔티티의 정보도 동시에 조회 해서 가져올 수 있는 장점이 있습니다.
     */

    /**
     * 회원의 장바구니를 새로 생성하는 정적 팩토리 메서드
     *
     * 비유: 마트에 처음 온 고객에게 새 장바구니를 하나 만들어서 건네주는 것입니다.
     *       장바구니에 "이건 김철수 고객님의 장바구니입니다"라는 이름표를 붙여줍니다.
     *
     * @param member 장바구니의 소유자가 될 회원 엔티티
     * @return 해당 회원 정보가 설정된 새 Cart 객체
     */
    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }

}