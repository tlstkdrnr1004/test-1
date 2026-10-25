package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.CartItemDto;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ========================================================================
 * CartServiceTest - 장바구니 서비스 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 쇼핑몰에서 고객이 상품을 장바구니에 담는 기능이 올바르게 동작하는지 확인합니다.
 *
 * 일상 비유:
 *   마트에서 장바구니에 물건을 담았을 때,
 *   "내가 담은 물건이 맞는지", "수량이 맞는지" 확인하는 것과 같습니다.
 *   실제 마트 대신 가상(테스트) 환경에서 확인하는 것이죠.
 *
 * [어노테이션 설명]
 * @SpringBootTest
 *   - 실제 애플리케이션과 동일한 환경을 구성하여 테스트합니다.
 *   - 모든 빈(스프링이 관리하는 객체)이 등록된 상태에서 실행됩니다.
 *
 * @Transactional
 *   - 테스트에서 DB에 저장한 데이터를 테스트가 끝나면 자동으로 롤백(취소)합니다.
 *   - 비유: 연습장에 연필로 써보고, 지우개로 깨끗이 지우는 것과 같습니다.
 *     테스트 데이터가 실제 DB에 남지 않으므로 안전합니다.
 *
 * @TestPropertySource
 *   - 테스트 전용 설정 파일(application-test.properties)을 사용하도록 지정합니다.
 *   - 실제 운영 DB 대신 테스트용 DB를 사용하기 위한 설정입니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class CartServiceTest {

    /**
     * @Autowired
     *   - 스프링이 자동으로 필요한 객체를 주입(연결)해주는 어노테이션입니다.
     *   - 비유: 레스토랑에서 주문하면 셰프가 알아서 재료를 준비해주는 것처럼,
     *     스프링이 알아서 필요한 객체를 만들어서 넣어줍니다.
     */
    @Autowired
    ItemRepository itemRepository;        // 상품 데이터를 DB에 저장/조회하는 도구

    @Autowired
    MemberRepository memberRepository;    // 회원 데이터를 DB에 저장/조회하는 도구

    @Autowired
    CartService cartService;              // 장바구니 관련 비즈니스 로직을 처리하는 서비스

    @Autowired
    CartItemRepository cartItemRepository; // 장바구니 상품 데이터를 DB에 저장/조회하는 도구

    /**
     * 테스트용 상품을 생성하여 DB에 저장하는 헬퍼 메서드입니다.
     *
     * 테스트를 실행하려면 먼저 장바구니에 담을 "상품"이 있어야 합니다.
     * 비유: 마트에서 장바구니에 물건을 담으려면, 먼저 진열대에 물건이 있어야 하는 것과 같습니다.
     *
     * @return 저장된 상품(Item) 객체
     */
    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    /**
     * 테스트용 회원을 생성하여 DB에 저장하는 헬퍼 메서드입니다.
     *
     * 장바구니에 물건을 담으려면 "누가" 담는지 알아야 합니다.
     * 비유: 마트 멤버십 카드가 있어야 장바구니 이력이 기록되는 것처럼,
     *       테스트를 위해 가상 회원을 미리 만들어둡니다.
     *
     * @return 저장된 회원(Member) 객체
     */
    public Member saveMember(){
        Member member = new Member();
        member.setEmail("test@test.com");
        return memberRepository.save(member);
    }

    /**
     * 장바구니 담기 기능 테스트
     *
     * [테스트 시나리오]
     * 1. 상품과 회원을 미리 DB에 저장합니다.
     * 2. 해당 회원이 해당 상품을 장바구니에 5개 담는 요청을 보냅니다.
     * 3. 장바구니에 담긴 결과를 DB에서 조회합니다.
     * 4. 담긴 상품이 원래 상품과 같은지, 수량이 맞는지 확인합니다.
     *
     * 비유:
     *   온라인 쇼핑몰에서 "사과 5개 담기" 버튼을 누른 후,
     *   장바구니 페이지에 들어가서 정말 "사과"가 "5개" 담겨있는지 확인하는 과정입니다.
     *
     * @Test - 이 메서드가 테스트 대상임을 JUnit에 알려줍니다.
     * @DisplayName - 테스트 실행 결과 화면에 표시될 이름을 지정합니다.
     *   "장바구니 담기 테스트"라고 표시되어 어떤 테스트인지 한눈에 알 수 있습니다.
     */
    @Test
    @DisplayName("장바구니 담기 테스트")
    public void addCart(){
        // [준비] 테스트용 상품과 회원을 DB에 저장
        Item item = saveItem();
        Member member = saveMember();

        // [준비] 장바구니에 담을 정보를 설정 (상품 ID와 수량)
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCount(5);                    // 5개 담기
        cartItemDto.setItemId(item.getId());        // 어떤 상품을 담을지 (상품 ID)

        // [실행] 장바구니 담기 서비스 호출 - 실제로 장바구니에 상품을 담는 핵심 로직
        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        // [검증 준비] DB에서 방금 담은 장바구니 상품을 다시 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);

        // [검증] 장바구니에 담긴 상품 ID가 원래 상품 ID와 일치하는지 확인
        assertEquals(item.getId(), cartItem.getItem().getId());

        // [검증] 장바구니에 담긴 수량이 요청한 수량(5개)과 일치하는지 확인
        assertEquals(cartItemDto.getCount(), cartItem.getCount());
    }

}
