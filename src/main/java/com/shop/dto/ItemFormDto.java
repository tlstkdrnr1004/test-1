package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/*
 * ============================================================
 * 📝 ItemFormDto - 상품 등록 신청서
 * ============================================================
 *
 * [비유] 백화점에 새 상품을 입점시킬 때 작성하는 "상품 등록 신청서"를 떠올려 보세요.
 *        신청서에는 상품명, 가격, 설명, 재고, 판매 상태, 첨부 사진 등을 적습니다.
 *        이 클래스가 바로 그 "신청서" 역할을 합니다.
 *
 * [역할] 상품 등록/수정 화면(Form)에서 입력한 데이터를 담아서
 *        컨트롤러 -> 서비스 -> DB(Entity)로 전달하거나,
 *        반대로 DB에서 조회한 상품 정보를 화면에 보여줄 때 사용하는 DTO입니다.
 *
 * [사용 시점]
 *   - 관리자가 상품을 새로 등록할 때 (신청서 작성)
 *   - 기존 상품 정보를 수정할 때 (신청서 수정)
 *   - 상품 수정 화면에 기존 데이터를 불러올 때
 * ============================================================
 */
@Getter @Setter
public class ItemFormDto {

    // 상품 고유 식별 번호 (DB의 Primary Key)
    // [비유] 백화점에서 상품마다 붙이는 관리 번호. 예: 1, 2, 3...
    //        신규 등록 시에는 아직 번호가 없고(null), 수정 시에는 기존 번호가 들어옵니다.
    private Long id;

    // 상품명 (빈 문자열이거나 null일 수 없음)
    // [비유] 신청서의 "상품 이름" 칸. 반드시 적어야 합니다.
    //        예: "나이키 에어맥스", "삼성 갤럭시 S24"
    // @NotBlank: 빈 문자열("")과 null 모두 허용하지 않는 검증 어노테이션
    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    // 상품 가격 (null일 수 없음)
    // [비유] 신청서의 "판매가격" 칸. 가격 없이는 등록 불가!
    //        예: 59000, 1290000
    // @NotNull: null을 허용하지 않는 검증 어노테이션 (0은 허용됨)
    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    // 상품 상세 설명 (빈 문자열이거나 null일 수 없음)
    // [비유] 신청서의 "상품 설명" 칸. 고객이 상품을 이해할 수 있도록 상세히 적습니다.
    //        예: "최신 쿠셔닝 기술이 적용된 런닝화입니다."
    @NotBlank(message = "상품 상세는 필수 입력 값입니다.")
    private String itemDetail;

    // 상품 재고 수량 (null일 수 없음)
    // [비유] 신청서의 "입고 수량" 칸. 창고에 몇 개 있는지 적습니다.
    //        예: 100, 50, 0
    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    // 상품 판매 상태 (SELL: 판매중, SOLD_OUT: 품절)
    // [비유] 신청서의 "판매 상태" 칸. 현재 팔고 있는지(SELL), 다 팔렸는지(SOLD_OUT)를 표시.
    //        enum 타입이라 정해진 값만 선택할 수 있습니다. (선택지 밖의 값은 불가)
    private ItemSellStatus itemSellStatus;

    /*
     * 상품 이미지 DTO 리스트 - 첨부 사진 목록
     *
     * [비유] 신청서에 첨부하는 "상품 사진 묶음"입니다.
     *        상품 수정 화면에서 기존에 등록된 이미지 정보(파일명, 경로 등)를
     *        화면에 보여주기 위해 사용합니다.
     *
     * [언제 채워지나?]
     *   - 상품 수정 시: DB에서 조회한 이미지 정보가 이 리스트에 담겨 화면에 전달됩니다.
     *   - 상품 신규 등록 시: 아직 이미지가 없으므로 빈 리스트입니다.
     */
    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

    /*
     * 상품 이미지 ID 리스트 - 사진 관리 번호 목록
     *
     * [비유] 이미 등록된 사진들의 "접수 번호 목록"입니다.
     *        수정할 때 "몇 번 사진을 어떤 사진으로 바꿀 건지" 알아야 하므로,
     *        기존 이미지들의 ID를 이 리스트에 담아둡니다.
     *
     * [언제 채워지나?]
     *   - 상품 수정 시: 기존 이미지의 ID들이 담깁니다.
     *   - 상품 신규 등록 시: 아직 이미지가 없으므로 빈 리스트입니다.
     */
    private List<Long> itemImgIds = new ArrayList<>();

    /*
     * ModelMapper - 자동 복사기
     *
     * [비유] "신청서(DTO)"와 "상품 대장(Entity)" 사이에서
     *        항목 이름이 같으면 값을 자동으로 옮겨 적어주는 복사기입니다.
     *        수작업으로 하나하나 옮기면 실수하기 쉬운데, 이 복사기가 대신 해줍니다.
     *
     * [static인 이유]
     *   - 복사기는 하나만 있으면 모든 신청서에서 공유해서 쓸 수 있습니다.
     *     매번 새 복사기를 사는 것보다 효율적이죠.
     */
    private static ModelMapper modelMapper = new ModelMapper();

    /*
     * createItem() 메서드 - 신청서를 상품 대장으로 변환
     *
     * [비유] 작성 완료된 "신청서(ItemFormDto)"를 바탕으로
     *        백화점 "상품 대장(Item Entity)"에 정식 등록하는 과정입니다.
     *        신청서에 적힌 내용(상품명, 가격 등)을 대장 양식에 맞게 옮겨 적습니다.
     *
     * [동작 흐름]
     *   1. 현재 객체(this = 작성된 신청서)를 ModelMapper에 넘깁니다.
     *   2. ModelMapper가 필드명이 같은 항목끼리 값을 자동 복사합니다.
     *   3. 완성된 Item 엔티티 객체를 반환합니다.
     *
     * @return Item - DB에 저장할 상품 엔티티 객체
     */
    public Item createItem(){
        return modelMapper.map(this, Item.class);
    }

    /*
     * of() 메서드 - 상품 대장을 신청서로 변환 (static 팩토리 메서드)
     *
     * [비유] 이미 등록된 "상품 대장(Item Entity)"의 내용을
     *        "신청서(ItemFormDto)" 양식에 옮겨 적는 과정입니다.
     *        상품 수정 화면에서 기존 정보를 보여줄 때 사용합니다.
     *
     * [동작 흐름]
     *   1. Item 엔티티 객체를 매개변수로 받습니다.
     *   2. ModelMapper가 Entity -> DTO로 필드 값을 자동 복사합니다.
     *   3. 완성된 ItemFormDto 객체를 반환합니다.
     *
     * @param item - DB에서 조회한 상품 엔티티 객체
     * @return ItemFormDto - 화면에 전달할 상품 폼 DTO 객체
     */
    public static ItemFormDto of(Item item){
        return modelMapper.map(item, ItemFormDto.class);
    }

}