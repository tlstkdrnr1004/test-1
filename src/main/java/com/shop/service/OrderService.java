package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.repository.ItemImgRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.thymeleaf.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    private final OrderRepository orderRepository;

    private final ItemImgRepository itemImgRepository;




    //주문 생성
//        ✅ 요약 흐름:
//        주문할 상품 조회
//
//        주문하는 회원 조회
//
//        상품 + 수량으로 OrderItem 생성
//
//        회원 + OrderItem → Order 생성
//
//        주문을 DB에 저장
//
//        생성된 주문의 ID 반환
    public Long order(OrderDto orderDto, String email) {
        // ✅ 주문하려는 상품의 ID를 기반으로 상품 엔티티 조회
        // - orderDto.getItemId()는 클라이언트가 선택한 상품의 ID를 의미
        // - findById()는 Optional을 반환하므로, 존재하지 않으면 예외 발생
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);  // ❗상품이 없으면 EntityNotFoundException 발생 → 예외 전파로 트랜잭션 롤백 유도

        // ✅ 현재 로그인한 사용자의 이메일을 기반으로 회원(Member) 정보 조회
        // - 이메일은 Spring Security에서 principal.getName()으로 가져온 로그인 사용자의 식별자
        // - 해당 이메일을 가진 사용자가 DB에 존재하지 않으면 null 반환될 수 있음 (주의 필요)
        Member member = memberRepository.findByEmail(email);

        // ✅ 주문 항목(OrderItem)들을 담기 위한 리스트 생성
        // - 한 주문 안에 여러 개의 상품을 담을 수 있으므로 List 사용
        List<OrderItem> orderItemList = new ArrayList<>();

        // ✅ 주문할 상품(item)과 수량(orderDto.getCount())을 기반으로 주문 항목 생성
        // - 정적 팩토리 메서드 사용: OrderItem.createOrderItem(...) → 캡슐화를 통해 생성 규칙을 통제
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());

        // ✅ 생성한 주문 항목을 리스트에 추가
        orderItemList.add(orderItem);

        // ✅ 회원 정보(member)와 주문 항목 리스트를 이용해 최종 주문(Order) 엔티티 생성
        // - 정적 팩토리 메서드 Order.createOrder(...)를 통해 비즈니스 로직 내재화
        Order order = Order.createOrder(member, orderItemList);

        // ✅ 생성한 주문을 DB에 저장
        // - JPA의 save() 메서드 사용하여 영속성 컨텍스트에 등록 → 트랜잭션 커밋 시 DB 반영
        orderRepository.save(order);

        // ✅ 저장된 주문의 ID를 반환
        // - 컨트롤러에서는 이 ID를 클라이언트에게 전달하여 주문 결과 확인 등에 활용 가능
        return order.getId();
    }


    /**
     * 주문 내역 목록을 페이징 처리하여 조회합니다.
     *
     * 비유: 온라인 쇼핑몰의 "마이페이지 > 주문 내역" 화면입니다.
     * 내가 주문한 모든 내역을 한 번에 다 보여주면 스크롤이 끝이 없으니,
     * 한 페이지에 몇 건씩 나눠서 보여주는 것(페이징)입니다.
     * 마치 도서관에서 대출 기록을 10건씩 끊어서 보여주는 것과 같습니다.
     *
     * @param email    - 현재 로그인한 사용자의 이메일 (누구의 주문 내역인지 식별)
     * @param pageable - 페이징 정보 (몇 번째 페이지, 한 페이지에 몇 건)
     *                   비유: "2페이지를 보여주세요, 한 페이지에 10건씩"이라는 요청서
     * @return Page<OrderHistDto> - 주문 내역 DTO의 페이징 결과
     *                              (주문 목록 + 전체 건수 + 페이지 정보를 함께 담은 객체)
     */
    // ✅ @Transactional(readOnly = true) 설명:
    // 이 메서드는 데이터를 "읽기만" 하고 수정하지 않습니다.
    // readOnly = true를 설정하면:
    // 1) JPA가 변경 감지(dirty checking)를 하지 않아서 성능이 좋아집니다
    // 2) DB에 "이건 읽기 전용이야"라고 알려줘서, DB도 최적화할 수 있습니다
    // 비유: 도서관에서 책을 "열람만" 할 때와 "대출(수정)"할 때의 절차가 다른 것과 같습니다.
    //       열람만 하면 대출 카드 작성이 필요 없으니 더 빠릅니다.
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        // ✅ [1단계] 해당 이메일의 주문 목록을 DB에서 조회 (페이징 적용)
        List<Order> orders = orderRepository.findOrders(email, pageable);
        // ✅ [2단계] 해당 이메일의 전체 주문 건수 조회 (페이징 하단에 "총 N건" 표시용)
        Long totalCount = orderRepository.countOrder(email);

        // ✅ [3단계] 화면에 보여줄 주문 내역 DTO 리스트 생성
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        // ✅ [4단계] 각 주문(Order)을 순회하면서 화면용 DTO로 변환
        for (Order order : orders) {
            // 주문 엔티티 → 주문 내역 DTO로 변환 (주문일, 주문상태 등 기본 정보)
            OrderHistDto orderHistDto = new OrderHistDto(order);
            // 해당 주문에 포함된 주문 상품 목록을 가져옴
            List<OrderItem> orderItems = order.getOrderItems();

            // ✅ [5단계] 각 주문 상품의 대표 이미지를 찾아서 DTO에 추가
            // 비유: 주문 내역에 상품 사진도 함께 보여주기 위해, 각 상품의 대표 사진을 찾는 과정
            for (OrderItem orderItem : orderItems) {
                // 해당 상품의 대표 이미지(repimgYn = "Y")를 DB에서 조회
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn
                        (orderItem.getItem().getId(), "Y");
                // 주문 상품 정보 + 이미지 URL을 합쳐서 주문 상품 DTO 생성
                OrderItemDto orderItemDto =
                        new OrderItemDto(orderItem, itemImg.getImgUrl());
                // 주문 내역 DTO에 주문 상품 DTO를 추가
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            // 완성된 주문 내역 DTO를 결과 리스트에 추가
            orderHistDtos.add(orderHistDto);
        }

        // ✅ [6단계] PageImpl로 페이징 결과 객체를 만들어 반환
        // PageImpl(데이터 리스트, 페이징 정보, 전체 건수) → 화면에서 페이지 번호 계산에 사용
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    /**
     * 주문을 취소할 권한이 있는지 검증합니다.
     *
     * 비유: 택배 취소 요청 시 "본인 확인" 절차입니다.
     * 다른 사람의 주문을 함부로 취소할 수 없도록,
     * 현재 로그인한 사용자와 주문한 사용자가 같은 사람인지 확인합니다.
     * 마치 은행에서 "본인만 출금 가능"한 것과 같은 보안 절차입니다.
     *
     * @param orderId - 검증할 주문의 ID (어떤 주문인지 식별)
     * @param email   - 현재 로그인한 사용자의 이메일 (요청한 사람이 누구인지)
     * @return true: 본인의 주문이 맞음 (취소 가능) / false: 본인의 주문이 아님 (취소 불가)
     */
    @Transactional(readOnly = true)  // 데이터 조회만 하므로 읽기 전용 트랜잭션
    public boolean validateOrder(Long orderId, String email){
        // ✅ [1단계] 현재 로그인한 사용자 정보 조회
        Member curMember = memberRepository.findByEmail(email);
        // ✅ [2단계] 주문 ID로 해당 주문 조회 (없으면 EntityNotFoundException 발생)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        // ✅ [3단계] 주문에 저장된 회원 정보를 꺼냄 (이 주문을 실제로 한 사람)
        Member savedMember = order.getMember();

        // ✅ [4단계] 현재 로그인한 사용자의 이메일 vs 주문한 사용자의 이메일 비교
        // 비유: 택배 수령 시 "주문자 이름"과 "수령인 신분증"을 대조하는 것
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;  // 이메일이 다르면 → 본인의 주문이 아님 → 권한 없음
        }

        return true;  // 이메일이 같으면 → 본인의 주문 맞음 → 권한 있음
    }

    /**
     * 주문을 취소합니다.
     *
     * 비유: 음식점에서 "주문 취소해주세요"라고 요청하면,
     * 주방에서 해당 주문을 찾아서 취소 도장을 찍고, 재료(재고)를 다시 원래대로 돌려놓는 것입니다.
     *
     * 내부적으로 order.cancelOrder()가 호출되면:
     * 1) 주문 상태가 "CANCEL"로 변경되고
     * 2) 주문했던 상품의 재고가 원래대로 복구됩니다
     *
     * @param orderId - 취소할 주문의 ID
     */
    public void cancelOrder(Long orderId){
        // ✅ [1단계] 취소할 주문을 DB에서 조회 (없으면 예외 발생)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        // ✅ [2단계] 주문 취소 실행 (주문 상태 변경 + 재고 복구)
        // JPA의 변경 감지(dirty checking)에 의해 트랜잭션 커밋 시 자동으로 DB에 반영됩니다.
        // 비유: 엔티티 객체의 값만 바꿔도, JPA가 알아서 UPDATE SQL을 날려줍니다.
        //       마치 구글 문서 편집처럼 "저장 버튼 없이도 자동 저장"되는 것과 비슷합니다.
        order.cancelOrder();
    }

    /**
     * 여러 상품을 한 번에 주문합니다. (장바구니 주문용)
     *
     * 비유: 마트에서 장바구니에 여러 물건을 담아서 한 번에 계산하는 것입니다.
     * 위의 order() 메서드는 상품 1개를 주문하지만,
     * 이 메서드는 장바구니에 담긴 여러 상품(List)을 한 번에 주문 처리합니다.
     *
     * 처리 흐름:
     * 1) 회원 정보 조회
     * 2) 장바구니의 각 상품을 순회하며 OrderItem(주문 상품) 생성
     * 3) 회원 + 주문 상품 리스트로 Order(주문) 생성
     * 4) DB에 저장 후 주문 ID 반환
     *
     * @param orderDtoList - 주문할 상품 목록 (상품 ID + 수량이 담긴 DTO 리스트)
     *                       비유: 장바구니에 담긴 물건 목록 ("A상품 2개, B상품 1개, ...")
     * @param email        - 현재 로그인한 사용자의 이메일
     * @return 생성된 주문의 ID (Long)
     */
    public Long orders(List<OrderDto> orderDtoList, String email){

        // ✅ [1단계] 주문하는 회원 정보를 이메일로 조회
        Member member = memberRepository.findByEmail(email);
        // ✅ [2단계] 주문 상품(OrderItem)들을 담을 빈 리스트 생성
        List<OrderItem> orderItemList = new ArrayList<>();

        // ✅ [3단계] 장바구니의 각 상품(OrderDto)을 순회하며 주문 상품 생성
        for (OrderDto orderDto : orderDtoList) {
            // 상품 ID로 실제 상품 엔티티 조회 (없으면 예외 발생)
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);

            // 상품 + 수량으로 주문 상품(OrderItem) 생성 후 리스트에 추가
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        // ✅ [4단계] 회원 + 주문 상품 리스트로 최종 주문(Order) 생성
        Order order = Order.createOrder(member, orderItemList);
        // ✅ [5단계] 주문을 DB에 저장
        orderRepository.save(order);

        // ✅ [6단계] 생성된 주문의 ID를 반환
        return order.getId();
    }

}