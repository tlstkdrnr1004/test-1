package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;


/**
 * ========================================================================
 * OrderTest - 주문(Order) 엔티티의 JPA 핵심 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * JPA의 핵심 개념 3가지가 올바르게 동작하는지 확인합니다:
 *   1) 영속성 전이(Cascade) - 부모를 저장하면 자식도 자동 저장
 *   2) 고아 객체 제거(Orphan Removal) - 부모와 관계가 끊어진 자식 자동 삭제
 *   3) 지연 로딩(Lazy Loading) - 필요할 때만 연관 데이터를 조회
 *
 * 일상 비유:
 *   - 영속성 전이: 가족이 이사할 때, 부모가 이사하면 자녀도 자동으로 따라가는 것
 *   - 고아 객체 제거: 가족에서 독립(분리)한 자녀의 기록이 가족 명부에서 자동 삭제되는 것
 *   - 지연 로딩: 백과사전에서 목차만 먼저 보고, 궁금한 항목을 클릭할 때만 내용을 불러오는 것
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * @Transactional - 테스트 후 DB 변경사항을 자동으로 롤백합니다.
 * ========================================================================
 */
@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
class OrderTest {

    @Autowired
    OrderRepository orderRepository;       // 주문 데이터를 DB에 저장/조회하는 도구

    @Autowired
    ItemRepository itemRepository;         // 상품 데이터를 DB에 저장/조회하는 도구

    /**
     * @PersistenceContext - JPA의 EntityManager를 주입받습니다.
     * EntityManager는 엔티티(데이터 객체)의 생명주기를 관리하는 핵심 매니저입니다.
     * 비유: 도서관의 사서처럼, 데이터를 등록, 조회, 삭제하는 역할을 합니다.
     */
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;     // 회원 데이터를 DB에 저장/조회하는 도구

    @Autowired
    OrderItemRepository orderItemRepository; // 주문상품 데이터를 DB에 저장/조회하는 도구

    /**
     * 테스트용 상품을 생성하는 헬퍼 메서드입니다.
     * 주문을 만들려면 먼저 주문할 "상품"이 있어야 합니다.
     */
    public Item createItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    /**
     * ========================================================================
     * [테스트 1] 영속성 전이(Cascade) 테스트
     * ========================================================================
     *
     * [테스트 시나리오]
     * 1. 주문(Order) 객체를 만듭니다. (부모)
     * 2. 상품 3개를 만들어 각각 주문상품(OrderItem)으로 만들고, 주문에 추가합니다. (자식 3개)
     * 3. 주문(부모)만 DB에 저장합니다. (orderRepository.saveAndFlush)
     * 4. 메모리(영속성 컨텍스트)를 비운 후, DB에서 주문을 다시 조회합니다.
     * 5. 주문에 포함된 주문상품 개수가 3개인지 확인합니다.
     *    -> 주문만 저장했는데 주문상품도 함께 저장되었다면 영속성 전이가 정상 동작한 것!
     *
     * 비유:
     *   이사할 때 "가족 대표(부모)"의 이사 신고만 하면,
     *   같은 가족(자녀)의 주소도 자동으로 변경되는 것과 같습니다.
     *   Order(주문)를 저장하면 OrderItem(주문상품)도 자동으로 함께 저장됩니다.
     *
     * [영속성 전이란?]
     *   CascadeType.ALL 설정이 되어 있으면,
     *   부모 엔티티를 저장/삭제할 때 자식 엔티티도 자동으로 저장/삭제됩니다.
     *   자식을 일일이 저장하지 않아도 되므로 코드가 간결해집니다.
     */
    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {

        // [Step 1] 주문(부모) 객체 생성 - 아직 DB에 저장되지 않은 상태
        Order order = new Order();

        // [Step 2] 상품 3개를 만들고, 각각 주문상품으로 만들어 주문에 추가
        for(int i = 0; i < 3; i++) {
            Item item = this.createItem();
            itemRepository.save(item);       // 상품은 개별적으로 DB에 저장

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);          // 어떤 상품인지 연결
            orderItem.setCount(10);           // 주문 수량
            orderItem.setOrderPrice(1000);    // 주문 가격
            orderItem.setOrder(order);        // 이 주문상품이 어떤 주문에 속하는지 연결

            order.getOrderItems().add(orderItem);  // 주문에 주문상품 추가
            // 이 시점에서 orderItem은 아직 DB에 저장되지 않았지만,
            // order 객체 내부의 리스트에 포함된 상태입니다.
        }

        // [Step 3] 주문(부모)만 저장 + 즉시 DB 반영
        //   Cascade 설정 덕분에 주문상품(자식) 3개도 함께 DB에 저장됩니다!
        orderRepository.saveAndFlush(order);

        // [Step 4] 메모리(영속성 컨텍스트) 비우기
        //   이후 조회 시 캐시가 아닌 실제 DB에서 데이터를 가져오게 됩니다.
        em.clear();

        // [Step 5 - 검증] DB에서 주문을 다시 조회하여 주문상품 개수 확인
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);

        // 주문만 저장했는데도 주문상품 3개가 함께 저장되어 있어야 테스트 통과!
        assertEquals(3, savedOrder.getOrderItems().size());
    }

    /**
     * 테스트용 주문을 생성하는 헬퍼 메서드입니다.
     *
     * [처리 과정]
     * 1. 빈 주문(Order) 객체를 만듭니다. (부모)
     * 2. 상품 3개를 만들어 DB에 저장합니다.
     * 3. 각 상품에 대한 주문상품(OrderItem)을 만들어 주문에 추가합니다. (자식 3개)
     * 4. 회원(Member)을 만들어 DB에 저장합니다.
     * 5. 주문에 회원을 연결합니다. (누가 주문했는지)
     * 6. 주문을 DB에 저장합니다. (Cascade로 주문상품도 함께 저장)
     *
     * [JPA 엔티티 상태 변화 설명]
     * - 비영속(new): 자바 메모리에만 존재하고 JPA가 모르는 상태
     * - 영속(managed): 영속성 컨텍스트에 등록되어 JPA가 관리하는 상태
     * - 비유: 학생이 학교에 "미등록" 상태 vs "등록 완료" 상태
     *
     * @return 저장된 주문(Order) 객체
     */
    public Order createOrder() {
        Order order = new Order();

        for (int i = 0; i < 3; i++) {
            Item item = createItem();
            itemRepository.save(item);              // 상품 DB 저장

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);                // 어떤 상품인지 연결
            orderItem.setCount(10);                 // 수량 설정
            orderItem.setOrderPrice(1000);          // 가격 설정
            orderItem.setOrder(order);              // 부모(주문)와 연결

            order.getOrderItems().add(orderItem);   // 주문에 주문상품 추가
        }

        Member member = new Member();
        memberRepository.save(member);              // 회원 DB 저장

        order.setMember(member);                    // 주문에 회원 연결
        orderRepository.save(order);                // 주문 DB 저장 (Cascade로 주문상품도 함께 저장)

        return order;
    }


    /**
     * ========================================================================
     * [테스트 2] 고아 객체 제거(Orphan Removal) 테스트
     * ========================================================================
     *
     * [테스트 시나리오]
     * 1. 주문상품 3개를 가진 주문을 생성합니다.
     * 2. 주문의 주문상품 리스트에서 첫 번째 항목을 제거합니다.
     *    -> 이 주문상품은 더 이상 어떤 주문에도 속하지 않는 "고아" 상태가 됩니다.
     * 3. em.flush()로 DB에 반영합니다.
     *    -> orphanRemoval = true 설정 때문에, 고아가 된 주문상품이 DB에서 자동 삭제됩니다.
     *
     * 비유:
     *   가족 명부에서 한 명을 빼면(관계를 끊으면),
     *   그 사람의 기록이 명부에서 자동으로 삭제되는 것과 같습니다.
     *
     * [고아 객체란?]
     *   부모 엔티티와의 관계가 끊어진 자식 엔티티를 말합니다.
     *   orphanRemoval = true로 설정하면, 부모의 리스트에서 자식을 제거할 때
     *   해당 자식이 DB에서도 자동으로 삭제됩니다.
     *
     * [cascade와 orphanRemoval의 조합]
     *   - cascade = ALL: 부모 저장 시 자식도 자동 저장
     *   - orphanRemoval = true: 부모와 관계가 끊어진 자식 자동 삭제
     *   - 둘을 함께 사용하면 자식의 생명주기를 부모가 완전히 관리하게 됩니다.
     */
    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest() {

        // [1단계] 주문상품 3개를 가진 주문 생성
        Order order = this.createOrder();

        // [2단계] 첫 번째 주문상품을 리스트에서 제거 -> "고아" 상태
        order.getOrderItems().remove(0);

        // [3단계] DB에 반영 -> 고아 객체가 DELETE 됨
        em.flush();
        // orphanRemoval = true이므로, 리스트에서 제거된 주문상품에 대해
        // DELETE 쿼리가 자동으로 실행됩니다.
    }


    /**
     * ========================================================================
     * [테스트 3] 지연 로딩(Lazy Loading) 테스트
     * ========================================================================
     *
     * [테스트 시나리오]
     * 1. 주문을 생성하고, 주문상품의 ID를 저장해둡니다.
     * 2. em.flush() + em.clear()로 메모리를 비웁니다.
     * 3. 주문상품(OrderItem)을 DB에서 조회합니다.
     * 4. 주문상품에 연결된 주문(Order)의 클래스 타입을 출력합니다.
     *    - 즉시 로딩일 때: com.shop.entity.Order (실제 객체)
     *    - 지연 로딩일 때: com.shop.entity.Order$HibernateProxy$xxx (프록시 객체)
     * 5. 주문의 주문일(orderDate)을 실제로 사용(접근)할 때 비로소 SELECT 쿼리가 실행됩니다.
     *
     * 비유:
     *   넷플릭스에서 영화 목록을 볼 때를 생각해보세요.
     *   - 즉시 로딩: 영화 목록을 열면 모든 영화의 전체 내용이 한꺼번에 다운로드됨 (느림, 비효율)
     *   - 지연 로딩: 영화 목록에서는 제목과 포스터만 보여주고,
     *     실제로 재생 버튼을 누를 때 해당 영화의 내용을 다운로드함 (빠름, 효율적)
     *
     * [지연 로딩이란?]
     *   연관된 엔티티를 즉시 가져오지 않고, 실제로 사용(getter 호출)할 때 DB에서 조회하는 방식입니다.
     *   @ManyToOne(fetch = FetchType.LAZY)로 설정합니다.
     *   불필요한 데이터 조회를 방지하여 성능을 향상시킵니다.
     *
     * [프록시(Proxy) 객체란?]
     *   지연 로딩 시 실제 엔티티 대신 "가짜 대리인" 객체가 들어갑니다.
     *   이 프록시 객체는 실제 데이터를 사용할 때 비로소 DB에서 진짜 데이터를 가져옵니다.
     *   비유: 택배 보관함에 "택배가 있습니다" 메모만 넣어두고,
     *         실제로 찾으러 갈 때 보관함에서 물건을 꺼내는 것과 같습니다.
     */
    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        // [1단계] 주문 생성 및 주문상품 ID 저장
        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();

        // [2단계] 메모리(영속성 컨텍스트) 비우기
        em.flush();
        em.clear();

        // [3단계] DB에서 주문상품(OrderItem)을 조회
        //   지연 로딩 설정 시: OrderItem만 조회하는 간단한 쿼리가 실행됨
        //   즉시 로딩 설정 시: OrderItem + Item + Order + Member 모두 JOIN하여 조회됨
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);

        // [4단계] 주문 객체의 클래스 타입 확인
        //   - 즉시 로딩: class com.shop.entity.Order (실제 객체가 이미 로딩됨)
        //   - 지연 로딩: class com.shop.entity.Order$HibernateProxy$xxx (프록시 객체)
        System.out.println("Order class : " + orderItem.getOrder().getClass());

        System.out.println("===========================");

        // [5단계] 주문의 주문일을 실제로 사용(접근)
        //   지연 로딩일 경우, 이 시점에 비로소 Order 테이블에 대한 SELECT 쿼리가 실행됩니다.
        //   비유: 프록시(대리인)에게 "진짜 정보 보여줘"라고 요청하는 순간
        orderItem.getOrder().getOrderDate();
        System.out.println("===========================");
    }

}
