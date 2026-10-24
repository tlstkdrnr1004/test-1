package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/*
 * ========================================================================
 *  ItemImg 엔티티 — "상품 카탈로그의 사진 한 장"
 * ========================================================================
 *
 *  [비유]
 *  백화점에서 상품 카탈로그를 펼치면, 한 상품마다 여러 장의 사진이 붙어 있습니다.
 *  이 클래스는 그 사진 한 장 한 장에 해당합니다.
 *  - 사진마다 고유번호(id)가 있고,
 *  - 서버에 저장된 파일명(imgName), 원래 파일명(oriImgName), 웹 주소(imgUrl)를 기록하며,
 *  - 그 중 대표 사진(repimgYn = "Y") 한 장은 메인 페이지 썸네일로 사용됩니다.
 *
 *  [테이블 구조]
 *
 *        item 테이블                item_img 테이블
 *        ------------              --------------------
 *        item_id (PK)              item_img_id (PK)
 *        item_nm                   item_id (FK)  ← item 테이블 참조
 *        price                     img_name
 *        stock_number              ori_img_name
 *        item_detail               img_url
 *        item_sell_status          rep_img_yn
 *
 *  하나의 상품(Item)에 여러 장의 사진(ItemImg)이 달릴 수 있으므로
 *  item_img 테이블은 item_id를 외래키(FK)로 가집니다. (다대일 관계)
 */

// ──────────────────────────────────────────────
// @Entity   : 이 클래스가 JPA 엔티티(= DB 테이블과 1:1 매핑되는 자바 객체)임을 선언합니다.
//             비유: "이 종이는 공식 서류입니다" 라고 도장을 찍는 것과 같습니다.
// @Table    : 매핑될 실제 DB 테이블 이름을 지정합니다. 여기서는 "item_img" 테이블입니다.
//             비유: 서류철(테이블)의 라벨에 "item_img"라고 적어 두는 것입니다.
// @Getter @Setter : Lombok이 모든 필드의 getter/setter 메소드를 자동 생성해 줍니다.
// ──────────────────────────────────────────────
@Entity
@Table(name="item_img")
@Getter @Setter
// ──────────────────────────────────────────────
// extends BaseEntity :
//   BaseEntity는 등록일(regTime), 수정일(updateTime), 등록자(createdBy), 수정자(modifiedBy)를
//   자동으로 관리해주는 부모 클래스입니다.
//   비유: 모든 공식 서류에 공통으로 찍히는 "작성일 / 수정일 / 작성자" 도장 틀과 같습니다.
//   ItemImg는 이 틀을 상속받아, 사진이 언제·누구에 의해 등록/수정되었는지 자동 기록됩니다.
// ──────────────────────────────────────────────
public class ItemImg extends BaseEntity{

    // ──────────────────────────────────────────────
    // @Id             : 이 필드가 테이블의 기본키(PK)임을 선언합니다.
    //                   비유: 사진 뒷면에 찍힌 "고유 일련번호"입니다. 절대 중복되지 않습니다.
    // @Column         : 매핑될 DB 컬럼 이름을 "item_img_id"로 지정합니다.
    // @GeneratedValue : 기본키 값을 자동 생성합니다.
    //                   strategy = AUTO → DB 벤더(MySQL, H2 등)에 맞는 전략을 JPA가 자동 선택합니다.
    //                   비유: 은행 번호표 기계처럼, 새 사진이 등록될 때마다 번호를 자동으로 하나씩 매깁니다.
    // ──────────────────────────────────────────────
    @Id
    @Column(name="item_img_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;            // 사진 고유번호 (PK) — 각 이미지를 구별하는 유일한 식별자

    // 서버에 실제로 저장된 파일명 (UUID + 확장자 형태)
    // 비유: 도서관에서 책에 부여하는 "청구기호"처럼, 서버 내부에서 파일을 찾기 위한 이름입니다.
    //       UUID(범용 고유 식별자)를 사용하므로 같은 이름의 파일을 올려도 충돌이 발생하지 않습니다.
    //       예) "550e8400-e29b-41d4-a716-446655440000.jpg"
    private String imgName;

    // 사용자가 업로드할 때의 원본 파일명
    // 비유: 내가 컴퓨터에서 "강아지사진.jpg"로 저장한 원래 이름입니다.
    //       서버에는 UUID 이름으로 저장되지만, 원본 이름도 기록해 두어야
    //       나중에 "어떤 사진이었지?" 확인할 수 있습니다.
    private String oriImgName;

    // 웹 브라우저에서 이미지에 접근할 수 있는 경로 (URL)
    // 비유: 인터넷 주소창에 입력하면 사진을 볼 수 있는 "웹 주소"입니다.
    //       예) "/images/item/550e8400-e29b-41d4-a716-446655440000.jpg"
    private String imgUrl;

    // 대표 이미지 여부 ("Y" = 대표 이미지, "N" = 일반 이미지)
    // 비유: 여러 장의 사진 중 카탈로그 표지에 실릴 "대표 사진" 딱 한 장을 표시하는 스티커입니다.
    //       메인 페이지 상품 목록에서 썸네일로 보여줄 사진이 바로 이 값이 "Y"인 사진입니다.
    private String repimgYn;


    // @ManyToOne : 상품 이미지(ItemImg) 여러 개가 하나의 상품(Item)에 속하는 "다대일" 관계를 설정합니다.
    // 비유: 하나의 상품 카탈로그(Item)에 여러 장의 사진(ItemImg)이 붙어 있는 것과 같습니다.
    //       사진 한 장은 반드시 하나의 상품에만 소속되지만, 상품 하나에는 사진이 여러 장 있을 수 있습니다.
    // fetch = FetchType.LAZY(지연 로딩) : 상품 이미지를 조회할 때 연결된 상품(Item) 정보를 즉시 가져오지 않고,
    //       실제로 item 필드에 접근하는 시점에 DB에서 조회합니다. 불필요한 데이터 로딩을 방지하여 성능을 높입니다.
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "item_id") : item_img 테이블의 "item_id" 컬럼이 item 테이블의 기본키를 참조하는 외래키(FK)임을 지정합니다.
    @JoinColumn(name = "item_id")
    private Item item;

    // 상품 이미지 정보를 수정(업데이트)하는 메소드
    // 비유: 상품 카탈로그에 붙어있던 사진을 새 사진으로 교체하는 작업입니다.
    //       원본 파일명(oriImgName), 서버에 저장된 파일명(imgName), 브라우저에서 접근할 경로(imgUrl)를 새 값으로 덮어씁니다.
    // 이 메소드가 호출되면 JPA의 "변경 감지(dirty checking)" 기능에 의해 트랜잭션 커밋 시 자동으로 UPDATE SQL이 실행됩니다.
    public void updateItemImg(String oriImgName, String imgName, String imgUrl){
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }

}