package com.shop.repository;

import com.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.dto.CartDetailDto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * 장바구니 상품(CartItem) 리포지토리 인터페이스
 *
 * 비유: 장바구니 안에 담긴 상품들을 관리하는 "서랍장"이라고 생각하면 됩니다.
 *       상품을 넣고(save), 꺼내고(find), 빼는(delete) 기능을 제공합니다.
 *
 * JpaRepository<CartItem, Long>를 상속받으면 기본적인 CRUD(생성/조회/수정/삭제) 메서드가
 * 자동으로 제공됩니다. 여기에 추가로 장바구니에 특화된 조회 메서드를 정의합니다.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 특정 장바구니에서 특정 상품이 이미 담겨있는지 찾는 메서드
     *
     * 비유: "내 장바구니에 사과가 이미 있나?" 확인하는 것입니다.
     *       이미 담긴 상품이면 수량만 추가하고, 없으면 새로 담습니다.
     *
     * Spring Data JPA의 "메서드 이름 규칙"에 따라 자동으로 쿼리가 생성됩니다.
     * findBy + CartId + And + ItemId → WHERE cart_id = ? AND item_id = ?
     *
     * @param cartId 장바구니 ID (어느 장바구니인지)
     * @param itemId 상품 ID (어떤 상품인지)
     * @return 해당 장바구니에 해당 상품이 담겨 있으면 CartItem 반환, 없으면 null
     */
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    /**
     * 장바구니에 담긴 상품들의 상세 정보를 한 번에 조회하는 JPQL 쿼리
     *
     * 비유: 장바구니를 열어서 안에 담긴 모든 상품의 "이름, 가격, 수량, 사진"을
     *       한 장의 영수증처럼 정리해서 보여주는 것입니다.
     *
     * === JPQL 쿼리 상세 해설 ===
     *
     * select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl)
     *   → CartDetailDto 객체를 직접 생성합니다. 생성자에 5개의 값을 넣어서 만듭니다.
     *     ci.id: 장바구니 상품 ID
     *     i.itemNm: 상품명
     *     i.price: 상품 가격
     *     ci.count: 담은 수량
     *     im.imgUrl: 상품 대표 이미지 URL
     *
     * from CartItem ci, ItemImg im
     *   → CartItem(장바구니 상품)과 ItemImg(상품 이미지) 두 테이블을 사용합니다.
     *     ci, im은 각각의 별칭(닉네임)입니다.
     *
     * join ci.item i
     *   → CartItem이 가지고 있는 Item(상품 정보)을 조인합니다. i는 Item의 별칭입니다.
     *     비유: 장바구니 상품이 "어떤 상품인지" 연결해서 가져오는 것입니다.
     *
     * where ci.cart.id = :cartId
     *   → 특정 장바구니(cartId)에 담긴 상품들만 필터링합니다.
     *     :cartId는 메서드 파라미터의 값이 들어가는 자리입니다.
     *
     * and im.item.id = ci.item.id
     *   → 상품 이미지의 상품 ID와 장바구니 상품의 상품 ID가 같은 것만 가져옵니다.
     *     비유: 사과 장바구니 상품에는 사과 이미지를, 바나나에는 바나나 이미지를 매칭합니다.
     *
     * and im.repimgYn = 'Y'
     *   → 대표 이미지만 가져옵니다. 상품에 여러 이미지가 있을 수 있는데 대표(Y) 이미지 1개만 사용합니다.
     *
     * order by ci.regTime desc
     *   → 최근에 장바구니에 담은 순서대로 정렬합니다 (최신이 위로 오도록).
     *
     * @param cartId 조회할 장바구니 ID
     * @return 장바구니 상품들의 상세 정보 목록
     */
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by ci.regTime desc"
            )
    List<CartDetailDto> findCartDetailDtoList(Long cartId);

}
