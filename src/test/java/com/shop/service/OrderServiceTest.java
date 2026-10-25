package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.constant.OrderStatus;
import com.shop.dto.OrderDto;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ========================================================================
 * OrderServiceTest - 주문 서비스 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 쇼핑몰의 핵심 기능인 "주문하기"와 "주문 취소하기"가 올바르게 동작하는지 확인합니다.
 *
 * 일상 비유:
 *   배달 앱에서 음식을 주문했을 때,
 *   1) 주문 금액이 맞는지 확인하고 (주문 테스트)
 *   2) 주문 취소 시 상태가 "취소"로 바뀌고 재고가 원래대로 돌아오는지 확인하는 것 (주문 취소 테스트)
 *   과 같습니다.
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 * @Transactional - 테스트 후 DB 변경사항을 자동으로 롤백(원상복구)합니다.
 *   비유: 시뮬레이션 게임처럼, 실제 세계에 영향을 주지 않고 연습할 수 있습니다.
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;     // 주문 관련 비즈니스 로직을 처리하는 서비스

    @Autowired
    private OrderRepository orderRepository; // 주문 데이터를 DB에 저장/조회하는 도구

    @Autowired
    ItemRepository itemRepository;          // 상품 데이터를 DB에 저장/조회하는 도구

    @Autowired
    MemberRepository memberRepository;      // 회원 데이터를 DB에 저장/조회하는 도구

    /**
     * 테스트용 상품을 생성하여 DB에 저장하는 헬퍼 메서드입니다.
     * 주문을 하려면 먼저 주문할 "상품"이 있어야 합니다.
     * 비유: 배달 앱에서 주문하려면 메뉴판에 음식이 등록되어 있어야 하는 것과 같습니다.
     */
    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);                          // 상품 가격: 10,000원
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);   // 판매 상태: 판매중
        item.setStockNumber(100);                      // 재고 수량: 100개
        return itemRepository.save(item);
    }

    /**
     * 테스트용 회원을 생성하여 DB에 저장하는 헬퍼 메서드입니다.
     * 주문을 하려면 "누가" 주문하는지 알아야 합니다.
     */
    public Member saveMember(){
        Member member = new Member();
        member.setEmail("test@test.com");
        return memberRepository.save(member);

    }

    /**
     * 주문 기능 테스트
     *
     * [테스트 시나리오]
     * 1. 상품(10,000원, 재고 100개)과 회원을 미리 DB에 저장합니다.
     * 2. 해당 회원이 해당 상품을 10개 주문합니다.
     * 3. 주문 총 금액이 "단가(10,000원) x 수량(10개) = 100,000원"과 일치하는지 확인합니다.
     *
     * 비유:
     *   편의점에서 1,000원짜리 음료를 3개 샀을 때,
     *   영수증에 "3,000원"이라고 찍혀야 정상인 것을 확인하는 과정입니다.
     *
     * @Test - 이 메서드가 테스트 대상임을 JUnit에 알립니다.
     * @DisplayName - 테스트 결과 화면에 "주문 테스트"라고 표시됩니다.
     */
    @Test
    @DisplayName("주문 테스트")
    public void order(){
        // [준비] 테스트용 상품과 회원 생성
        Item item = saveItem();
        Member member = saveMember();

        // [준비] 주문 정보 설정 (어떤 상품을 몇 개 주문할지)
        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);                     // 10개 주문
        orderDto.setItemId(item.getId());          // 주문할 상품 지정

        // [실행] 주문 서비스 호출 - 실제 주문 처리
        Long orderId = orderService.order(orderDto, member.getEmail());

        // [검증 준비] DB에서 방금 생성된 주문을 다시 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);

        // 주문에 포함된 주문상품 목록을 가져옴
        List<OrderItem> orderItems = order.getOrderItems();

        // [검증] 예상 총 금액 계산: 주문수량(10) x 상품가격(10,000) = 100,000원
        int totalPrice = orderDto.getCount()*item.getPrice();

        // 실제 주문의 총 금액이 예상 금액과 일치하는지 확인
        assertEquals(totalPrice, order.getTotalPrice());
    }

    /**
     * 주문 취소 기능 테스트
     *
     * [테스트 시나리오]
     * 1. 상품(재고 100개)과 회원을 생성합니다.
     * 2. 해당 상품 10개를 주문합니다. (재고: 100 -> 90으로 감소 예상)
     * 3. 주문을 취소합니다.
     * 4. 주문 상태가 "CANCEL(취소)"로 변경되었는지 확인합니다.
     * 5. 재고가 원래대로 100개로 복구되었는지 확인합니다.
     *
     * 비유:
     *   온라인 쇼핑몰에서 옷을 주문했다가 마음이 바뀌어 취소했을 때,
     *   - 주문 내역에 "취소됨"이라고 표시되고
     *   - 매장 재고가 원래대로 돌아와야 하는 것을 확인하는 과정입니다.
     */
    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder(){
        // [준비] 테스트용 상품과 회원 생성
        Item item = saveItem();
        Member member = saveMember();

        // [준비] 주문 정보 설정
        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);                     // 10개 주문
        orderDto.setItemId(item.getId());

        // [실행 1] 주문 생성
        Long orderId = orderService.order(orderDto, member.getEmail());

        // [실행 2] 생성된 주문을 DB에서 조회한 후 취소
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        orderService.cancelOrder(orderId);

        // [검증 1] 주문 상태가 "CANCEL(취소)"로 변경되었는지 확인
        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());

        // [검증 2] 재고가 원래 수량(100개)으로 복구되었는지 확인
        // 주문 시 10개가 빠졌으므로, 취소 후 다시 100개로 돌아와야 합니다.
        assertEquals(100, item.getStockNumber());
    }

}
