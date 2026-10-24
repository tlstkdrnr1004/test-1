package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import com.shop.dto.CartDetailDto;
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;

/**
 * 장바구니 서비스 (CartService)
 *
 * 비유: 마트의 "창고 관리자"라고 생각하면 됩니다.
 *       카운터 직원(CartController)이 고객의 요청을 전달하면,
 *       창고 관리자가 실제로 장바구니에 상품을 넣고, 빼고, 수량을 조절하고,
 *       결제(주문) 처리까지 해줍니다.
 *
 * 역할: 장바구니와 관련된 모든 비즈니스 로직을 처리합니다.
 *       - 장바구니에 상품 담기
 *       - 장바구니 목록 조회
 *       - 장바구니 상품 수량 수정/삭제
 *       - 장바구니 상품 주문 처리
 */
@Service // 이 클래스가 비즈니스 로직을 담당하는 서비스 계층임을 Spring에게 알려줍니다
@RequiredArgsConstructor // final 필드들에 대한 생성자를 Lombok이 자동 생성합니다 (의존성 주입용)
@Transactional // 이 클래스의 모든 메서드에 트랜잭션을 적용합니다. 비유: 은행 송금처럼 작업이 모두 성공하거나, 하나라도 실패하면 전부 취소됩니다.
public class CartService {

    private final ItemRepository itemRepository;         // 상품 정보를 DB에서 조회하는 저장소
    private final MemberRepository memberRepository;     // 회원 정보를 DB에서 조회하는 저장소
    private final CartRepository cartRepository;         // 장바구니를 DB에서 조회/저장하는 저장소
    private final CartItemRepository cartItemRepository; // 장바구니 상품을 DB에서 조회/저장하는 저장소
    private final OrderService orderService;             // 주문 처리를 담당하는 서비스 (장바구니 → 주문 전환 시 사용)

    /**
     * 장바구니에 상품 담기
     *
     * 비유: 마트에서 상품을 장바구니에 넣는 과정입니다.
     *       1) 먼저 장바구니가 있는지 확인하고, 없으면 새 장바구니를 만듭니다.
     *       2) 이미 같은 상품이 장바구니에 있으면 수량만 추가합니다.
     *       3) 새로운 상품이면 장바구니에 새로 넣습니다.
     *
     * @param cartItemDto 담을 상품 정보 (상품 ID, 수량)
     * @param email       현재 로그인한 사용자의 이메일
     * @return 장바구니에 담긴 상품(CartItem)의 ID
     */
    public Long addCart(CartItemDto cartItemDto, String email){

        // 1. 담으려는 상품을 DB에서 조회합니다. 없으면 EntityNotFoundException 발생
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        // 2. 이메일로 현재 로그인한 회원 정보를 조회합니다
        Member member = memberRepository.findByEmail(email);

        // 3. 해당 회원의 장바구니가 이미 있는지 확인합니다
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            // 장바구니가 없으면 새로 생성하고 DB에 저장합니다 (처음 장바구니를 사용하는 회원)
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        // 4. 이미 같은 상품이 장바구니에 담겨있는지 확인합니다
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if(savedCartItem != null){
            // 이미 담긴 상품이면 수량만 추가합니다 (사과 2개 + 3개 = 5개)
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            // 새로운 상품이면 장바구니 상품(CartItem)을 새로 생성하고 DB에 저장합니다
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    /**
     * 장바구니 목록 조회
     *
     * 비유: 장바구니를 열어서 안에 담긴 상품들을 하나씩 꺼내 보여주는 것입니다.
     *       각 상품의 이름, 가격, 수량, 대표 이미지를 목록으로 반환합니다.
     *
     * @param email 현재 로그인한 사용자의 이메일
     * @return 장바구니에 담긴 상품들의 상세 정보 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션: 데이터를 조회만 하므로 DB 성능을 최적화합니다
    public List<CartDetailDto> getCartList(String email){

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>(); // 빈 목록을 준비합니다

        Member member = memberRepository.findByEmail(email); // 이메일로 회원 조회
        Cart cart = cartRepository.findByMemberId(member.getId()); // 해당 회원의 장바구니 조회
        if(cart == null){
            return cartDetailDtoList; // 장바구니가 없으면 빈 목록을 반환합니다
        }

        // 장바구니 ID로 장바구니에 담긴 상품 상세 정보를 JPQL 쿼리로 한 번에 조회합니다
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    /**
     * 장바구니 상품의 소유자 확인 (권한 검증)
     *
     * 비유: 다른 사람의 장바구니를 몰래 수정하면 안 되겠죠?
     *       이 메서드는 "이 장바구니 상품이 현재 로그인한 사용자의 것인지" 확인합니다.
     *       마트에서 "이 장바구니가 제 것이 맞나요?"라고 확인하는 것과 같습니다.
     *
     * @param cartItemId 확인할 장바구니 상품 ID
     * @param email      현재 로그인한 사용자의 이메일
     * @return true: 본인의 장바구니 상품, false: 다른 사람의 장바구니 상품
     */
    @Transactional(readOnly = true) // 읽기 전용: 검증만 하고 데이터를 변경하지 않습니다
    public boolean validateCartItem(Long cartItemId, String email){
        Member curMember = memberRepository.findByEmail(email); // 현재 로그인한 회원 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new); // 장바구니 상품 조회 (없으면 예외 발생)
        Member savedMember = cartItem.getCart().getMember(); // 장바구니 상품 → 장바구니 → 소유자 회원 정보를 따라가서 조회

        // 현재 로그인한 사용자와 장바구니 소유자의 이메일이 다르면 false (권한 없음)
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }

        return true; // 이메일이 같으면 true (본인의 장바구니)
    }

    /**
     * 장바구니 상품 수량 변경
     *
     * 비유: "사과 3개를 5개로 바꿔주세요"와 같이 장바구니에 담긴 상품의 수량을 변경합니다.
     *
     * @param cartItemId 수량을 변경할 장바구니 상품 ID
     * @param count      변경할 수량
     */
    public void updateCartItemCount(Long cartItemId, int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new); // 장바구니 상품을 DB에서 조회합니다

        cartItem.updateCount(count); // 수량을 변경합니다. JPA의 변경 감지(dirty checking)로 자동 DB 반영됩니다.
    }

    /**
     * 장바구니 상품 삭제
     *
     * 비유: 장바구니에서 상품을 꺼내서 다시 진열대에 올려놓는 것과 같습니다.
     *
     * @param cartItemId 삭제할 장바구니 상품 ID
     */
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new); // 장바구니 상품을 DB에서 조회합니다
        cartItemRepository.delete(cartItem); // DB에서 해당 장바구니 상품을 삭제합니다
    }

    /**
     * 장바구니 상품들을 주문으로 전환
     *
     * 비유: 마트에서 장바구니에 담은 상품들을 "계산대"로 가져가서 결제하는 과정입니다.
     *       1) 장바구니 상품들의 정보를 주문 형태(OrderDto)로 변환합니다.
     *       2) OrderService를 통해 실제 주문을 생성합니다.
     *       3) 주문이 완료되면 장바구니에서 해당 상품들을 제거합니다.
     *
     * @param cartOrderDtoList 주문할 장바구니 상품 목록
     * @param email            현재 로그인한 사용자의 이메일
     * @return 생성된 주문의 ID
     */
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        List<OrderDto> orderDtoList = new ArrayList<>(); // 주문 정보를 담을 목록

        // 1단계: 장바구니 상품들을 주문 DTO로 변환합니다
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                            .findById(cartOrderDto.getCartItemId())
                            .orElseThrow(EntityNotFoundException::new); // 장바구니 상품 조회

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId()); // 상품 ID 설정
            orderDto.setCount(cartItem.getCount());          // 수량 설정
            orderDtoList.add(orderDto);
        }

        // 2단계: OrderService를 통해 주문을 생성합니다
        Long orderId = orderService.orders(orderDtoList, email);

        // 3단계: 주문 완료된 장바구니 상품들을 장바구니에서 삭제합니다 (계산 끝났으니 장바구니 비우기)
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                            .findById(cartOrderDto.getCartItemId())
                            .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }

        return orderId; // 생성된 주문 ID를 반환합니다
    }

}
