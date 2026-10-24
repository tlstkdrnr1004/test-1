package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;



/**
 * 장바구니 상품 엔티티 (CartItem)
 *
 * 비유: 마트 장바구니 안에 담긴 "개별 상품 한 종류"를 나타냅니다.
 *       예를 들어 장바구니에 사과 3개, 바나나 2개가 담겨 있다면
 *       CartItem은 2개가 생깁니다: (사과, 3개), (바나나, 2개)
 *
 * 관계:
 *   - Cart(장바구니) 1 : N CartItem(장바구니 상품) → 하나의 장바구니에 여러 상품을 담을 수 있음
 *   - Item(상품) 1 : N CartItem(장바구니 상품) → 하나의 상품이 여러 장바구니에 담길 수 있음
 *
 * DB 테이블: cart_item 테이블과 매핑됩니다.
 *   - cart_item_id (PK): 장바구니 상품 고유 번호
 *   - cart_id (FK): 어느 장바구니에 담겼는지
 *   - item_id (FK): 어떤 상품인지
 *   - count: 수량
 *
 * BaseEntity 상속: 등록일, 수정일, 등록자, 수정자 정보가 자동으로 관리됩니다.
 */
@Entity // 이 클래스가 JPA 엔티티임을 선언합니다. DB의 cart_item 테이블과 매핑됩니다.
@Getter @Setter // Lombok: 모든 필드에 대해 getter/setter 메서드를 자동 생성합니다
@Table(name="cart_item") // 매핑할 DB 테이블 이름을 "cart_item"으로 지정합니다
public class CartItem extends BaseEntity {

    @Id // 이 필드가 기본키(Primary Key)임을 선언합니다
    @GeneratedValue // 기본키 값을 자동으로 생성합니다
    @Column(name = "cart_item_id") // DB 컬럼명을 "cart_item_id"로 지정합니다
    private Long id; // 장바구니 상품의 고유 식별 번호

/*
주제 : 다대일 단방향 매핑하기

하나의 장바구니에 여러 개의 상품들이 추가될수 있습니다.
또한 같은 상품은 여러 개 주문할 수도 있으므로 몇 개의 상품을 담아 줄것인지도 설정해 줘야 합니다.


        Cart 테이블(엔티티)              cart_item 테이블(엔티티)               item 테이블(엔티티)
        --------------         -------------------------------          ----------------
        cart_id (PK)            cart_item_id (PK)                       item_id (PK)
        member_id (FK)          cart_id (Fk)                            item_nm
                                item_id (FK)                            price
                                count                                   stock_number
                                                                        item_detail
                                                                        item_sell_status

 */

    //하나의 장바구니(Cart 엔티티)에 여러 개의 상품(CartItem 엔티티)을 담을 수 있으므로
    //@ManyToOne 어노테이션을 이용하여
    // 다(여러개의 상품 CartItem)-대-일(하나의 장바구니 Cart엔티티) 관계로 매핑합니다.

    //@ManyToOne   //@ManyToOne(fetch = FetchType.EAGER)  // <--- 즉시 로딩  기본 디폴트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;


    //장바구니에 담을 상품(CartItem)의 정보를 알아야 하므로 상품정보(item 엔티티)를 매핑해 줍니다.
    //하나의 상품 정보(item엔티티)는  여러 장바구니의 장바구니에담을상품(CartItem)으로 담길수 있으므로
    //마찬가지로 @ManyToOne 어노테이션으로 다대일 관계로 매핑합니다.
    //@ManyToOne   //@ManyToOne(fetch = FetchType.EAGER)  // <--- 즉시 로딩  기본 디폴트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    /*
    장바구니 상품 엔티티 설계가 끝났으므로 애플리케이션을 재실행하여 콘솔창에 출력되는 쿼리문을 보겠습니다.
    cart_item 테이블에     @JoinColumn(name = "item_id") 로 설정한 컬럼 item_id로 생성되어 추가됩니다.

    Hibernate:
        create table cart_item (
            count integer not null,
            cart_id bigint,
            cart_item_id bigint not null,
            item_id bigint,
            primary key (cart_item_id)
        ) engine=InnoDB


   또한 아래 처럼 엔티티와 매핑되는 테이블에  @JoinColumn 어노테이션의 name속성으로 설정한 값이 foreign key로 추가되는 것도 볼수 있습니다.
   어떤 테이블에 어떤 컬럼이 추가되는지 햇갈릴수 있는데 @JoinColumn 어노테이션을 사용하는 엔티티에 컬럼이 추가된다고 생각하시면됩니다.

    Hibernate:
        alter table cart_item
           add constraint FK1uobyhgl1wvgt1jpccia8xxs3
           foreign key (cart_id)
           references cart (cart_id)
    Hibernate:
        alter table cart_item
           add constraint FKdljf497fwm1f8eb1h8t6n50u9
           foreign key (item_id)
           references item (item_id)

     */





    private int count; // 장바구니에 담은 수량 (같은 상품을 몇 개 담았는지)

    /**
     * 장바구니 상품을 새로 생성하는 정적 팩토리 메서드
     *
     * 비유: "이 장바구니에, 이 상품을, 3개 담아주세요"라는 요청을 처리합니다.
     *       새 CartItem 객체를 만들어서 장바구니, 상품 정보, 수량을 설정한 뒤 반환합니다.
     *
     * @param cart  상품을 담을 장바구니
     * @param item  담을 상품
     * @param count 담을 수량
     * @return 설정이 완료된 새 CartItem 객체
     */
    public static CartItem createCartItem(Cart cart, Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);
        return cartItem;
    }

    /**
     * 기존 수량에 추가하는 메서드
     *
     * 비유: 장바구니에 사과가 이미 2개 있는데 3개를 더 담으면 총 5개가 되는 것입니다.
     *       이미 장바구니에 같은 상품이 있을 때 수량만 더합니다.
     *
     * @param count 추가할 수량
     */
    public void addCount(int count){
        this.count += count;
    }

    /**
     * 수량을 새 값으로 변경하는 메서드
     *
     * 비유: "사과 5개를 3개로 바꿔주세요"처럼 수량을 완전히 새로운 값으로 교체합니다.
     *       addCount()는 "더하기"이고, updateCount()는 "덮어쓰기"입니다.
     *
     * @param count 변경할 수량 (기존 수량을 무시하고 이 값으로 설정)
     */
    public void updateCount(int count){
        this.count = count;
    }

}