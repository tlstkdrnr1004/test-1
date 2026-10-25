package com.shop.dto;

import com.shop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

/*
 * ============================================================
 * 📸 ItemImgDto - 상품 사진 정보 카드
 * ============================================================
 *
 * [비유] 사진관에서 사진을 맡기면 받는 "사진 보관 카드"를 떠올려 보세요.
 *        카드에는 사진 번호, 원래 파일명, 저장된 위치, 대표 사진 여부 등이 적혀 있습니다.
 *        이 클래스가 바로 그 "사진 보관 카드" 역할을 합니다.
 *
 * [역할] DB의 ItemImg 엔티티(item_img 테이블)에 저장된 상품 이미지 정보를
 *        화면(View)으로 전달하거나, 화면에서 받은 이미지 정보를 서비스 계층으로
 *        전달할 때 사용하는 DTO(Data Transfer Object) 클래스입니다.
 *
 * [사용 시점]
 *   - 상품 등록/수정 후 이미지 정보를 화면에 보여줄 때
 *   - 상품 상세 페이지에서 이미지 목록을 표시할 때
 * ============================================================
 */
@Getter @Setter
public class ItemImgDto {

    // 이미지 고유번호 (DB에서 자동 생성되는 PK)
    // [비유] 사진관에서 사진마다 붙이는 접수 번호. 예: 1, 2, 3...
    private Long id;

    // 서버에 저장된 파일명 (UUID 등으로 변환된 이름)
    // [비유] 사진관이 관리용으로 붙인 코드명. 예: "a1b2c3d4-photo.jpg"
    //        같은 이름의 파일이 올라와도 서로 겹치지 않도록 변환합니다.
    private String imgName;

    // 원본 파일명 (사용자가 업로드할 때의 원래 이름)
    // [비유] 고객이 사진을 맡기며 "이건 '가족사진.jpg'예요"라고 말한 그 이름.
    //        예: "맥북프로_정면.jpg"
    private String oriImgName;

    // 이미지 조회 경로 (브라우저에서 이미지를 불러올 URL)
    // [비유] 사진을 온라인에서 볼 수 있는 링크 주소.
    //        예: "/images/item/a1b2c3d4-photo.jpg"
    private String imgUrl;

    // 대표 이미지 여부 ("Y" = 대표, "N" = 일반)
    // [비유] 상품 목록에서 가장 먼저 보이는 '대문 사진'인지 여부.
    //        쇼핑몰 메인에 뜨는 사진이 바로 repImgYn = "Y"인 이미지입니다.
    private String repImgYn;

    /*
     * ModelMapper - 자동 복사기 📋
     *
     * [비유] 두 장의 양식(Entity와 DTO)이 있을 때,
     *        항목 이름이 같으면 자동으로 값을 옮겨 적어주는 "자동 복사기"입니다.
     *        예를 들어 Entity에 imgName이 있고, DTO에도 imgName이 있으면
     *        ModelMapper가 알아서 값을 복사해줍니다.
     *
     * [왜 쓰나요?]
     *   - 필드가 많을 때 하나하나 get/set으로 복사하면 코드가 길어지고 실수하기 쉽습니다.
     *   - ModelMapper를 쓰면 한 줄로 자동 복사가 가능합니다.
     *
     * [static인 이유]
     *   - ModelMapper는 한 번 만들어두면 여러 번 재사용할 수 있으므로
     *     객체마다 새로 만들 필요 없이 클래스 레벨에서 하나만 공유합니다.
     */
    private static ModelMapper modelMapper = new ModelMapper();

    /*
     * of() 메서드 - Entity를 DTO로 변환하는 "번역기"
     *
     * [비유] 도서관의 원서(Entity)를 번역본(DTO)으로 바꾸는 번역기입니다.
     *        원서(ItemImg Entity)의 내용을 읽어서,
     *        같은 항목 이름을 가진 번역본(ItemImgDto)에 옮겨 적어줍니다.
     *
     * [동작 흐름]
     *   1. ItemImg 엔티티 객체를 매개변수로 받습니다.
     *   2. ModelMapper가 엔티티의 필드명과 DTO의 필드명을 비교합니다.
     *   3. 이름이 같은 필드끼리 값을 자동 복사합니다.
     *   4. 완성된 ItemImgDto 객체를 반환합니다.
     *
     * @param itemImg - DB에서 조회한 상품 이미지 엔티티 객체
     * @return ItemImgDto - 화면에 전달할 상품 이미지 DTO 객체
     */
    public static ItemImgDto of(ItemImg itemImg) {
        return modelMapper.map(itemImg, ItemImgDto.class);
    }

}