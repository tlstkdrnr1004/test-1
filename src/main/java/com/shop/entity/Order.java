package com.shop.entity;

import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// =============================
// 🛒 Order 엔티티 클래스
// =============================
// 회원이 주문한 "주문 정보"를 저장하는 JPA 엔티티입니다.
@Entity
@Table(name = "orders") // 데이터베이스 테이블명을 "orders"로 지정함 ("order"는 SQL 예약어이기 때문에 사용을 피함)
@Getter @Setter
public class Order extends BaseEntity {

    // ----------------------------
    // ✅ 주문 ID (기본 키)
    // ----------------------------
    @Id
    @GeneratedValue // 자동 증가 전략
    @Column(name = "order_id")
    private Long id;



    /*
    주제 : 다대일 단방향 매핑하기

    orders 테이블(엔티티)<--다              Member 테이블(엔티티)   <-- 일
        --------------         -------------------------------
        order_id (PK)           member_id (PK)
        member_id (FK)          name
        order_date              email
        order_status            password
                                address
                                role
     */
    // ----------------------------
    // ✅ 주문자 (회원)
    // ----------------------------
    //@ManyToOne(fetch = FetchType.EAGER) // 주문 : 회원 = 다대일 관계 (여러 주문 → 한 회원)
    @ManyToOne(fetch = FetchType.LAZY)  //<--- 지연 로딩
    @JoinColumn(name = "member_id")     // FK 컬럼명: member_id
    private Member member;




    // ----------------------------
    // ✅ 주문 날짜
    // ----------------------------
    private LocalDateTime orderDate;

    // ----------------------------
    // ✅ 주문 상태 (ORDER, CANCEL)
    // ----------------------------
    @Enumerated(EnumType.STRING) // enum 값을 문자열로 저장 (ORDER, CANCEL)
    private OrderStatus orderStatus;


    /*
       Orders(주문 엔티티) 테이블  과    OrderItem(주문한 상품 엔티티) 테이블을  order_id컬럼을 외래키로 조인하면
       Orders(주문)에 속한  OrderItem(주문한 상품)이 어떤 상품들이 있는지 알수 있고,
       OrderItem(주문한 상품)은 어떤 Orders(주문)에 속하는지 알수 있습니다.
       즉, 테이블은 외래 키 하나로 양방향 조회가 가능합니다.


       Orders (주문한 정보가 저장되는 엔티티)테이블                 Order_item (주문한 상품정보가 저장되는 엔티티)테이블
           order_id (PK)                                           order_item_id (PK)
           member_id (FK)                                          order_id (FK)
           order_date                                              item_id (FK)
           order_status                                            order_price
                                                                   count

       엔티티는 테이블과 다릅니다.  엔티티를 양뱡향 연관 관계로  설정하면  객체의 참조는 둘인데 외래 키는 하나이므로
       둘 중 누가 외래키를 관리할지를 정해야 합니다.
           -   연관 관계의 주인은 외래 키가 있는 곳으로 설정
           -   연관 관계의 주인이 외래 키를 관리( 등록, 수정, 삭제 )
           -   주인이 아닌 쪽은 연관 관계 매핑 시 (mappedBy 속성의 값으로 연관 관계의 주인을 설정)
           -   주인이 아닌 쪽은 읽기만 가능
    */
    // ----------------------------
    // ✅ 주문 상품 목록
    // ----------------------------
    //주문 상품 Order_item 엔티티와  일대 다 매핑을 합니다.
    //외래키( order_id ) 가  order_idtem 테이블에 있으므로 연관 관계의 주인은 OrderItem 엔티티 입니다.
    //Orders(주문한 정보가 저장되는 엔티티)가 주인이 아니므로 아래 처럼 "mappedBy"속성으로 연관 관계의 주인을 설정합니다.
    //속성의 값으로 "order" 를 적어준 이유는 OrderItem에 있는 Order에 의해 관리 된다는 의미로 해석하시면 됩니다.
    //즉, 연관 관계의 주인의 필드인 order를 mappedBy의 값으로 세팅하면 됩니다.
    /*
    // -----------------OrderItem 클래스 내부에 작성된 변수(필드)
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;  // ✅ 주인!
    -------------------------------------------------------------
     */
    /*
    @OneToMany(mappedBy = "order")// ✅ 여기에 order 라고 써야 함!
    //하나의 주문이 여러 개의 주문 상품을 갖게 되므로 List배열에 담아 저장 합니다.
    private List<OrderItem> orderItems = new ArrayList<>();
    */

    @OneToMany(
            // [1] 연관관계의 주인 설정
            mappedBy = "order",
            // → 이 필드는 'OrderItem' 클래스 안에 있는 'order' 필드에 의해 이 관계가 설정되었음을 뜻함.
            // → 즉, "외래키(FK)"는 'OrderItem' 테이블에 있음. (DB 기준으로 주인은 자식 엔티티임)
            // → 그래서 Order 엔티티는 '읽기 전용', 실제 DB 변경은 OrderItem 쪽에서 일어남

            // [2] 영속성 전이(Cascade) 설정
            cascade = CascadeType.ALL,
            // → Order를 저장하면, 안에 포함된 OrderItem들도 자동으로 같이 저장됨
            // → Order를 삭제하면, OrderItem도 자동으로 삭제됨
            // → Order를 수정하거나 병합하면, OrderItem도 같이 처리됨
            // ★ 개발자가 일일이 각각 저장하지 않아도 되게끔 자동으로 전이됨

            // [3] 고아 객체 자동 삭제 설정
            orphanRemoval = true,
            // → 부모인 Order에서 자식 OrderItem을 리스트에서 제거하면
            //    해당 OrderItem도 DB에서 자동으로 삭제됨
            // → 마치 자식이 가족관계에서 빠지면 주민등록도 삭제되는 느낌

            // [4] 지연 로딩(LAZY) 설정
            fetch = FetchType.LAZY
            // → Order를 조회할 때, 그 안의 OrderItem까지는 **바로 로딩하지 않음**
            // → 필요할 때 (getOrderItems() 같은 메서드 호출 시) DB에서 쿼리 실행됨
            // → 성능 향상 효과. 필요할 때만 DB 접근.
    )
    private List<OrderItem> orderItems = new ArrayList<>();
    // → 하나의 주문이 여러 개의 주문상품을 가질 수 있으므로 List 형태로 작성함
    /*
    🧩 마지막 Tip
        cascade = ALL → 부모 저장하면 자식도 저장

        orphanRemoval = true → 부모와 관계 끊기면 자식 자동 삭제

        둘 다 설정하면 거의 자동으로 삽입-삭제 관리됨
    */



    // ===================================================
    // ✅ 주문 상품 추가 메서드 (연관 관계 편의 메서드)
    // ===================================================
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);       // 리스트에 추가
        orderItem.setOrder(this);        // 양방향 관계 유지 (OrderItem 쪽에도 this 연결)
    }

    // ===================================================
    // ✅ 주문 생성 메서드 (정적 팩토리 메서드)
    // - 외부에서 Order 객체를 직접 new 하지 않고, 이 메서드를 통해 생성하도록 유도
    // - 주문 생성 로직을 캡슐화하여 코드 일관성과 가독성 향상
    // ===================================================
    public static Order createOrder(Member member, List<OrderItem> orderItemList) {

        Order order = new Order();               // ✅ 새로운 주문(Order) 객체 생성

        order.setMember(member);                 // ✅ 주문한 회원 정보 설정
        // - 주문자 정보를 Order 엔티티에 저장
        // - 주문자(Member)와 주문(Order)은 N:1 관계

        // ✅ 주문 상품(OrderItem) 리스트를 주문(Order)에 추가
        for (OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);       // - 개별 주문상품을 주문에 연결 (양방향 연관관계 설정 포함)
            // - addOrderItem 메서드는 orderItem.setOrder(this) 도 수행 → 연관관계 주입
            // - orderItemList는 보통 장바구니나 단일 상품을 기반으로 만들어짐
        }

        // ✅ 주문 상태를 'ORDER'(주문 완료)로 설정
        order.setOrderStatus(OrderStatus.ORDER); // - Enum 타입: ORDER, CANCEL 등 상태 구분

        // ✅ 주문 일시를 현재 시간으로 설정
        order.setOrderDate(LocalDateTime.now()); // - 주문 생성 시간을 기록 (DB 저장용)

        return order;                            // ✅ 초기화 완료된 주문 객체 반환
    }


    // ===================================================
    // ✅ 전체 주문 금액 계산
    // ===================================================
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice(); // 각 주문상품의 금액 합산
        }
        return totalPrice;
    }

    // ===================================================
    // ✅ 주문 취소 메서드
    // ===================================================
    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL; // 주문 상태 → 취소
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();                // 주문상품들도 각각 취소 처리
        }
    }
}
