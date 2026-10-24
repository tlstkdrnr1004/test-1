package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;



/// 메인 페이지에서 상품 데이터를 가져와 사용자에게 보여주기 위한 컨트롤러 클래스
@Controller // 해당 클래스가 Spring MVC의 컨트롤러 역할을 한다는 것을 명시
@RequiredArgsConstructor // Lombok 어노테이션: final 필드를 대상으로 생성자를 자동 생성하여 의존성 주입을 편하게 함
public class MainController {

    // ItemService는 상품 관련 비즈니스 로직을 담당하는 서비스 클래스
    private final ItemService itemService;

    /**
     * 메인 페이지 요청을 처리하는 메서드
     *
     * @param itemSearchDto 검색 조건(상품명, 카테고리 등)을 담는 DTO 객체.
     *                      Spring이 자동으로 바인딩해줌 (ex: ?searchQuery=가방)
     * @param page 페이지 번호 (Optional<Integer>로 받아서 기본값 설정 가능)
     * @param model 뷰로 데이터를 전달하기 위한 Spring의 Model 객체
     * @return "main"이라는 이름의 뷰 리턴 (Thymeleaf 템플릿 또는 JSP에서 main.html/main.jsp로 해석)
     */
    @GetMapping(value = "/") // 루트 경로("/")로 들어온 GET 요청을 처리
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {

        // Pageable 객체는 Spring Data JPA에서 페이징 처리에 사용됩니다.
        // PageRequest.of(page, size)를 통해 특정 페이지와 페이지당 항목 수를 지정할 수 있습니다.

        // 아래 코드에서 사용되는 `page`는 Optional<Integer> 타입입니다.
        // 이는 사용자가 페이지 번호를 요청하지 않았을 경우(null)에도 대비하기 위한 안전한 방식입니다.

        // `page.orElse(0)` 부분은 다음과 같은 의미를 가집니다:
        // - Optional 객체인 `page`에 값이 존재하면 해당 값을 반환합니다.
        // - 만약 값이 존재하지 않으면 (즉, 사용자가 페이지 번호를 제공하지 않았다면)
        //   기본값인 0을 반환하여 첫 페이지를 의미하게 됩니다.
        // - 예: page에 2가 있으면 → 2 반환 → 2번 페이지 요청 (3번째 데이터 그룹)
        //       page가 비어 있으면 → 0 반환 → 0번 페이지 요청 (첫 번째 데이터 그룹)

        // `6`은 한 페이지에 보여줄 데이터 수를 의미합니다.
        // 즉, 매 페이지마다 최대 6개의 항목이 조회됩니다.

        // 최종적으로 생성된 Pageable 객체는 이후의 JPA 쿼리에서
        // 페이징 처리된 결과를 가져올 때 사용됩니다.
        //Pageable 객체 얻기
        //[number: 0, size: 6, sort:UNSORTED]
        Pageable pageable = PageRequest.of(
                page.orElse(0), // Optional에서 페이지 값 추출. 없으면 기본값 0 (첫 페이지)
                6               // 페이지당 항목 수: 6개
        );



        // itemService에서 검색 조건과 페이징 정보를 기반으로 메인 페이지에 보여줄 상품 목록을 가져옴
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // 뷰로 전달할 데이터 설정
        model.addAttribute("items", items); // 상품 리스트
        model.addAttribute("itemSearchDto", itemSearchDto); // 검색 조건 유지용
        model.addAttribute("maxPage", 5); // 페이지 네비게이션에서 최대 페이지 수 (ex: 1~5)

        // 반환하는 뷰 이름 (템플릿 엔진이 "main.html" 또는 "main.jsp" 등을 렌더링)
        return "main";
    }
}
