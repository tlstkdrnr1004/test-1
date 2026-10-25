package com.shop.dto;

// QueryDSL에서 사용될 Projection 기능을 사용하기 위해 import합니다.
// 이 기능은 QueryDSL 쿼리 결과를 생성자 기반으로 바로 매핑할 수 있게 해줍니다.
import com.querydsl.core.annotations.QueryProjection;

// Lombok 라이브러리의 어노테이션을 사용하여 getter/setter 메서드를 자동 생성합니다.
import lombok.Getter;
import lombok.Setter;

// Lombok을 통해 id, itemNm 등 필드에 대한 getter/setter를 자동 생성합니다.
@Getter @Setter
public class MainItemDto {

    // 상품 ID (기본 키)
    private Long id;

    // 상품 이름
    private String itemNm;

    // 상품 상세 설명
    private String itemDetail;

    // 대표 이미지 URL
    private String imgUrl;

    // 상품 가격
    private Integer price;

    // 이 생성자는 QueryDSL에서 사용할 수 있도록 정의된 생성자입니다.
    // 일반적으로 JPQL이나 QueryDSL을 사용할 때, DTO로 직접 결과를 매핑하고자 할 경우
    // 생성자를 사용하는 방식(`constructor projection`)이 많이 사용됩니다.

    // @QueryProjection 어노테이션을 붙이면, 컴파일 시 QueryDSL의 Q타입 클래스가 생성될 때
    // 이 생성자를 참조한 Q클래스도 자동으로 생성됩니다.
    //
    // 예를 들어, MainItemDto 클래스에 이 어노테이션이 붙어 있으면
    // 컴파일 시 `QMainItemDto` 라는 클래스가 생성되며,
    // 내부적으로 아래와 같은 생성자가 포함됩니다:
    //
    // public QMainItemDto(Expression<Long> id, Expression<String> itemNm, Expression<String> itemDetail, Expression<String> imgUrl, Expression<Integer> price)
    //
    // 이렇게 생성된 QMainItemDto를 사용하면 QueryDSL에서 다음과 같이 DTO를 생성할 수 있습니다:
    //
    //     new QMainItemDto(item.id, item.itemNm, item.itemDetail, itemImage.imgUrl, item.price)
    //
    // 위와 같은 방식으로 작성하면 쿼리 결과를 바로 DTO에 매핑할 수 있으므로
    // 코드가 간결하고 타입 안정성도 확보됩니다.
    //
    // 단점으로는, @QueryProjection을 사용하면 QueryDSL에서 제공하는 `apt`(annotation processing tool)를 이용해
    // Q파일을 컴파일 시에 반드시 생성해야 하며, DTO가 QueryDSL에 종속된다는 점입니다.
    //
    // 이 생성자를 통해 다음 필드들이 초기화됩니다:
    // - id: 상품 ID
    // - itemNm: 상품명
    // - itemDetail: 상품 설명
    // - imgUrl: 대표 이미지 URL
    // - price: 상품 가격
    @QueryProjection
    public MainItemDto(Long id, String itemNm, String itemDetail, String imgUrl, Integer price) {
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
    }


}
