package com.shop.repository;

import com.shop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


/*
 * ========================================================================
 *  ItemImgRepository — "상품 사진 보관소"
 * ========================================================================
 *
 *  [비유]
 *  사진관(Photo Studio)의 보관소를 떠올려 보세요.
 *  - 보관소에 사진을 맡기면(save) 안전하게 보관해 줍니다.
 *  - "A 상품 사진 전부 주세요" 하면 해당 상품의 사진을 번호순으로 꺼내 줍니다.
 *  - "A 상품의 대표 사진 한 장만 주세요" 하면 표지에 쓸 대표 사진만 골라 줍니다.
 *  이 인터페이스가 바로 그런 보관소 역할을 합니다.
 *
 *  [JpaRepository<ItemImg, Long> 설명]
 *  JpaRepository를 상속(extends)하면 기본적인 CRUD 메소드를 자동으로 제공받습니다.
 *  - 제네릭 첫 번째 타입 ItemImg : 이 보관소가 다루는 엔티티(= DB 테이블에 매핑되는 객체)
 *  - 제네릭 두 번째 타입 Long   : 해당 엔티티의 기본키(PK) 타입
 *  비유: "이 보관소는 '상품 이미지(ItemImg)' 전용이고, 사진 번호(id)는 숫자(Long)입니다"
 *        라고 보관소 입구에 안내판을 붙여 놓는 것과 같습니다.
 *
 *  자동으로 사용 가능한 대표적인 메소드들:
 *    save(entity)      — 사진을 보관소에 저장 (INSERT 또는 UPDATE)
 *    findById(id)      — 사진 번호로 한 장 조회
 *    findAll()         — 보관소의 모든 사진 조회
 *    delete(entity)    — 사진 삭제
 *    count()           — 보관소에 있는 사진 총 개수
 *
 *  [쿼리 메소드 네이밍 규칙 — Spring Data JPA의 마법]
 *  메소드 이름만 규칙에 맞게 작성하면, Spring이 이름을 분석하여 SQL을 자동 생성합니다.
 *  비유: 도서관 사서에게 "제목이 OO이고 저자가 XX인 책 찾아주세요" 라고 말하면
 *        사서가 알아서 검색해주는 것처럼, 메소드 이름이 곧 검색 조건이 됩니다.
 *
 *    findBy + 필드명                         → WHERE 필드 = ?
 *    findBy + 필드명 + And + 필드명          → WHERE 필드1 = ? AND 필드2 = ?
 *    findBy + 필드명 + OrderBy + 필드명 + Asc → WHERE 필드 = ? ORDER BY 필드 ASC
 */
public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

    // ──────────────────────────────────────────────
    // 특정 상품에 속한 모든 이미지를 이미지 번호(id) 오름차순으로 조회
    // ──────────────────────────────────────────────
    // [비유]
    // "A 상품 카탈로그에 붙어있는 사진들을 1번부터 순서대로 전부 꺼내 주세요"
    //
    // [메소드 이름 해석]
    //   findBy       → 조회한다
    //   ItemId       → item_id(상품 번호) 조건으로
    //   OrderBy      → 정렬 기준은
    //   Id           → item_img_id(사진 번호)
    //   Asc          → 오름차순(1, 2, 3, ...)
    //
    // 생성되는 SQL:
    //   SELECT * FROM item_img WHERE item_id = ? ORDER BY item_img_id ASC
    //
    // @param  itemId  조회할 상품의 고유 번호(PK)
    // @return 해당 상품의 이미지 목록 (번호 오름차순)
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    // ──────────────────────────────────────────────
    // 특정 상품의 대표 이미지 1장을 조회
    // ──────────────────────────────────────────────
    // [비유]
    // "A 상품 카탈로그 표지에 실을 대표 사진 딱 한 장만 찾아 주세요"
    // 메인 페이지 상품 목록에서 썸네일로 보여줄 때 이 메소드를 사용합니다.
    //
    // [메소드 이름 해석]
    //   findBy       → 조회한다
    //   ItemId       → item_id(상품 번호) 조건 AND
    //   And          → 그리고
    //   RepimgYn     → rep_img_yn(대표 이미지 여부) 조건으로
    //
    // 생성되는 SQL:
    //   SELECT * FROM item_img WHERE item_id = ? AND rep_img_yn = ?
    //
    // @param  itemId   조회할 상품의 고유 번호(PK)
    // @param  repimgYn 대표 이미지 여부 ("Y"이면 대표 이미지)
    // @return 조건에 맞는 이미지 1장 (대표 이미지)
    ItemImg findByItemIdAndRepimgYn(Long itemId, String repimgYn);

}