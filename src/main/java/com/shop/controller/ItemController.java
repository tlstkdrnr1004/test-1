
package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import com.shop.dto.ItemFormDto;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.persistence.EntityNotFoundException;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

// 상품 관련 웹 요청을 처리하는 컨트롤러입니다.

@Controller
@RequiredArgsConstructor // 생성자 주입을 자동으로 생성해줌 (itemService 주입됨)
public class ItemController {

    private final ItemService itemService; // 서비스 계층 의존성 주입

    /**
     * 상품 등록 페이지로 이동하는 메서드
     * - 상품 등록 페이지로 이동시 빈 ~ ItemFormDto 객체를 생성해서 model에 담아서 뷰로 전달 하자.
     */
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model){
        model.addAttribute("itemFormDto", new ItemFormDto()); // 빈 DTO 객체 전달
        return "item/itemForm"; // 템플릿 경로: templates/item/itemForm.html
    }

    /**
     * 상품 등록 처리
     */
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                          Model model, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){

        // 유효성 검사 실패 시 다시 입력 폼으로 이동
        // - 상품 등록 시 폼에서 필 수값을 입력하지 않았다면 다시 상품 등록페이지로 전환합니다.
        if(bindingResult.hasErrors()){
            return "item/itemForm";
        }

        // 첫 번째 상품 이미지(메인 페이지에서 보여줄 상품 이미지)가 비어있으면 에러
        //- 상품 등록시 첫 번째 이미지가 없다면 에러 메세지와 함께 상품 등록 페이지로 전환합니다.
        //  상품의 첫 번째 이미지는 메인 페이지에서 보여줄 상품 이미지로 사용하기 위해서 필수 값으로 지정 하겠습니다.
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            //상품 저장 로직을 호출합니다. 매개변수로 등록할 상품 정보(itemFormDto)와 상품 이미지 정보를 담고 있는(itemImgFileList)를 전달해 줍니다.
            itemService.saveItem(itemFormDto, itemImgFileList); // 서비스 계층에서 저장 처리

        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/"; //상품이 정상적으로 등록되었다면?  메인 화면으로 리다이렉트
    }

    /**
     * 상품 수정 페이지 또는  상품 상세 페이지 요청
     *
     * 메인 페이지에서 상품 이미지나 상품 정보 클릭시 상품의 상세 정보를 보여주는 페이지 요청
     */
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model){

        try {
            //조회된 상품 데이터를 Model에 담아서 뷰로 전달 하기 위한 작업
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId); // 상품 정보 조회
            model.addAttribute("itemFormDto", itemFormDto); // 화면에 전달

        } catch(EntityNotFoundException e){
            //상품 엔티티가 DB에 존재하지 않은 경우 에러메세지를 담아서 상품 등록 페이지로 이동합니다.
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto()); // 빈 객체 전달

            return "item/itemForm";// 상품 등록 페이지를 상품수정페이지 뷰로 사용 하기 위해 itemForm.html로 설정

        }

        return "item/itemForm"; // 상품 수정 또는 상품 상세 페이지로 이동
    }

    /**
     * 상품 수정 요청을 처리하는 메서드입니다.
     * http://localhost/admin/item/1
     * id가 1인 등로된 상품 정보 수정 요청이 들어옴
     *
     * [요약]
     * - 상품 수정 폼에서 전달된 데이터(상품 정보 + 이미지 파일 목록)를 서버로 받아서
     * - 유효성 검사, 이미지 유무 확인 후,
     * - 상품 서비스 로직을 호출하여 실제 상품 정보를 업데이트합니다.
     *
     * @param itemFormDto        사용자가 입력한 상품 정보 (폼 데이터가 DTO 객체로 자동 바인딩됨)
     * @param bindingResult      유효성 검사 결과를 저장하는 객체
     * @param itemImgFileList    업로드된 이미지 파일 목록 (첫 번째 이미지는 대표 이미지)
     * @param model              뷰에 데이터를 전달할 때 사용하는 Spring MVC 객체
     * @return                   수정 성공 시 홈으로 리다이렉트, 실패 시 다시 상품 수정 폼 페이지로 이동
     */
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(
            @Valid ItemFormDto itemFormDto,                        // 사용자 입력값을 DTO에 바인딩하고 유효성 검사 수행
            BindingResult bindingResult,                           // 유효성 검사 결과가 저장되는 객체
            @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,  // 사용자가 업로드한 이미지 파일 목록
            Model model                                             // 뷰로 데이터 전달을 위한 객체
    ) {

        // 1. 유효성 검사에서 오류가 발생한 경우 (예: 필수값 누락 등)
        //    오류 메시지와 함께 다시 상품 등록/수정 폼을 사용자에게 보여줍니다.
        if (bindingResult.hasErrors()) {
            return "item/itemForm";  // 상품 등록 또는 수정 폼 뷰로 되돌아감
        }

        // 2. 상품 수정이 아닌 신규 등록 상황인데 첫 번째 이미지(대표 이미지)가 비어 있을 경우
        //    이미지가 하나도 첨부되지 않았으면 에러 메시지를 보여주고 폼으로 되돌립니다.
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            // itemFormDto.getId() == null 이라는 것은 새 상품 등록임을 의미합니다.
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            // 3. 상품 수정 로직 실행 ************************************************************************
            //    - itemFormDto: 사용자가 수정한 상품 정보 (이름, 가격, 재고 등)
            //    - itemImgFileList: 사용자가 새로 첨부한 이미지 파일 목록
            //    updateItem()은 내부적으로 기존 상품 정보를 찾아서 새로운 정보로 업데이트합니다.
            itemService.updateItem(itemFormDto, itemImgFileList);

        } catch (Exception e) {
            // 4. 수정 처리 중 오류가 발생한 경우 (예: 파일 업로드 실패, DB 예외 등)
            //    사용자에게 에러 메시지를 보여주고 다시 수정 폼으로 되돌아갑니다.
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        // 5. 상품 수정이 정상적으로 완료되면 홈 페이지로 리다이렉트합니다.
        //    "redirect:/"는 클라이언트에게 "/" 주소로 다시 요청하라는 의미입니다.
        return "redirect:/";
    }


    /**
     * [관리자 상품 목록 조회 및 페이지 이동 메서드]
     * 이 메서드는 관리자 상품 관리 페이지를 요청받았을 때 실행됩니다.
     *
     * - 요청 경로: /admin/items 또는 /admin/items/{page}
     *   -> 페이지 번호가 URL에 있으면 해당 페이지로, 없으면 기본값 0페이지로 처리
     *
     * - 주요 역할:
     *   1. 검색(조회) 조건(ItemSearchDto)을 통해 상품 목록 조회
     *   2. 페이징 처리(Pageable)를 적용하여 상품 리스트 조회
     *   3. 조회한 상품 리스트 및 검색 조건, 최대 페이지 수를 model에 담아 View로 전달
     *   4. 최종적으로 관리자 상품 목록 페이지(item/itemMng.html)로 이동
     */
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, //조회 조건 정보가 담긴 ImtemSearchDto 객체
                             @PathVariable("page") Optional<Integer> page, // URL에서 {page} 값이 있을 수도, 없을 수도 있으므로 Optional로 처리
                             Model model) { // Model: 컨트롤러에서 뷰로 데이터를 전달할 때 사용하는 객체

        // [1] 페이징 처리 설정

        // 'page'는 URL 경로에서 페이지 번호를 전달받는 값입니다.
        // 예를 들어, 사용자가 '/admin/items/2'로 요청했다면 page에는 2가 들어옵니다.
        // 그러나 '/admin/items'처럼 페이지 번호를 생략할 수도 있으므로,
        // Optional<Integer> 타입으로 선언되어 있어 "값이 있을 수도, 없을 수도" 있는 상태입니다.

        // page.orElse(0)
        // - Optional의 값이 존재하면 그 값을 사용하고, 없으면 기본값 0을 사용하라는 의미입니다.
        // - 즉, 사용자가 페이지 번호를 입력하지 않은 경우 자동으로 첫 번째 페이지(0번 페이지)를 보여줍니다.

        // PageRequest.of(페이지 번호, 한 페이지당 항목 수)
        // - Spring Data JPA에서 제공하는 페이징 객체(Pageable)를 생성합니다.
        // - 첫 번째 인자: 현재 페이지 번호 (0부터 시작. 0 = 첫 페이지)
        // - 두 번째 인자: 한 페이지에 몇 개의 항목(상품)을 보여줄지 설정

        // 정리:
        // → 사용자가 '/admin/items/2'로 접근하면 2번 페이지, 즉 세 번째 페이지가 출력됩니다.
        // → 사용자가 '/admin/items'로 접근하면 page 값이 없으므로 기본값 0번 페이지가 출력됩니다.
        // → 각 페이지에는 최대 3개의 상품이 출력됩니다.
        Pageable pageable = PageRequest.of(page.orElse(0), 3); // 한 페이지에 상품 3개씩 출력


        // [2] 서비스 계층(Service Layer)을 호출하여 조건에 맞는 상품 목록을 조회합니다.
        // 이때 페이징 처리를 적용하여, 지정한 페이지에 해당하는 상품 일부만 가져옵니다.

        // itemService.getAdminItemPage(...)
        // - itemService는 비즈니스 로직을 담당하는 Service 객체입니다.
        // - Controller는 직접 DB와 통신하지 않고, Service를 통해 간접적으로 작업을 수행합니다.
        // - 여기서는 "관리자 상품 목록"을 조건에 맞게 가져오는 역할을 합니다.

        // 매개변수:
        // 1. itemSearchDto
        //    - 사용자가 입력한 검색 조건(예: 상품명, 등록일, 판매 상태 등)이 담긴 객체입니다.
        //    - 예: "판매 중인 상품만", "노트북이라는 이름을 가진 상품만" 등
        //
        // 2. pageable
        //    - 페이징 정보가 담긴 객체로, 현재 몇 페이지인지, 한 페이지에 몇 개 보여줄지 등의 설정이 포함되어 있습니다.
        //    - 이전 줄에서 만든 PageRequest.of(page.orElse(0), 3) 객체가 전달됩니다.

        // 반환값:
        // - Page<Item> : 조회된 상품 목록이 담긴 객체입니다.
        //   단순한 리스트가 아니라, 페이지 정보(전체 페이지 수, 현재 페이지 번호, 조회된 총 상품 수 등)도 함께 포함되어 있어
        //   페이징 UI 구현에 매우 유용합니다.
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);


        // [3] 조회한 데이터와 정보를 model 객체에 담아 View로 전달
        // model은 컨트롤러에서 화면(View)으로 데이터를 넘겨주는 객체입니다.

        // 1. model.addAttribute("items", items);
        //    - 'items'라는 이름으로 상품 목록 데이터를 View에 전달합니다.
        //    - 여기서 items는 Page<Item> 타입으로, 실제 상품 리스트뿐 아니라
        //      전체 페이지 수, 현재 페이지 번호 등 페이징 정보도 함께 포함되어 있습니다.
        //    - 뷰(HTML 템플릿)에서는 ${items.content}, ${items.totalPages} 등으로 접근할 수 있습니다.

        // 2. model.addAttribute("itemSearchDto", itemSearchDto);
        //    - 사용자가 입력한 검색 조건을 그대로 View에 전달합니다.
        //    - 이렇게 하면 뷰에서 검색창에 이전에 입력한 조건을 유지할 수 있습니다.
        //    - 예: '상품명' 검색 필드에 이전 검색어가 그대로 남아 있도록.

        // 3. model.addAttribute("maxPage", 5);
        //    - 페이징 UI에서 최대 몇 개의 페이지 번호를 한 번에 보여줄지 설정합니다.
        //    - 예: 페이지 하단에 [1] [2] [3] [4] [5] 처럼 최대 5개의 페이지 버튼을 보여주기 위한 값입니다.
        //    - 이 값은 페이징 템플릿에서 반복문 범위를 조절하는 데 사용됩니다.
        model.addAttribute("items", items);                 // 조회된 상품 데이터와 페이징 정보
        model.addAttribute("itemSearchDto", itemSearchDto); // 검색 조건 값
        model.addAttribute("maxPage", 5);                   // 페이징 최대 범위 (5개 버튼 노출)


        // [4] 최종적으로 관리자 상품 관리 페이지로 이동
        // return 구문은 어떤 HTML 템플릿을 사용자에게 보여줄지를 지정합니다.
        //
        // 여기서 "item/itemMng"는 뷰 파일의 경로를 의미합니다.
        // Spring Boot에서는 기본적으로 "src/main/resources/templates" 폴더에서 템플릿을 찾습니다.
        //
        // 즉, 최종적으로 렌더링되는 화면은 다음 경로의 HTML 파일입니다:
        // → src/main/resources/templates/item/itemMng.html
        return "item/itemMng"; // 템플릿 이름 (확장자 .html 생략 가능)

    }


    /**
     * 사용자(고객) 상품 상세 보기
     */
    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId") Long itemId){
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId); // 상품 상세 데이터
        model.addAttribute("item", itemFormDto); // 템플릿에 전달
        return "item/itemDtl"; // 상세 페이지로 이동
    }
}
