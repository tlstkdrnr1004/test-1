package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shop.dto.ItemDto;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================
 * Thymeleaf 예제용 컨트롤러 (ThymeleafExController) - "타임리프 연습장"
 * ============================================
 * Thymeleaf 템플릿 엔진의 다양한 기능을 학습하기 위한 예제 컨트롤러입니다.
 *
 * 비유: "타임리프 연습장"입니다.
 * 학교에서 본 시험을 보기 전에 연습장에 문제를 풀어보듯이,
 * 실제 쇼핑몰 화면을 만들기 전에 이 컨트롤러에서 타임리프 문법을 하나씩 연습합니다.
 *
 * 📖 연습장 목차:
 * - ex01: 텍스트 출력 연습 (th:text)          → 데이터를 화면에 글자로 보여주기
 * - ex02: 객체 바인딩 연습 (th:text + 객체)    → 상품 정보를 화면에 보여주기
 * - ex03: 반복문 연습 (th:each)               → 상품 목록을 반복해서 보여주기
 * - ex04: 조건문 연습 (th:if, th:unless)      → 조건에 따라 다르게 보여주기
 * - ex05: 링크 연습 (th:href)                 → 페이지 이동 링크 만들기
 * - ex06: 파라미터 전달 연습 (쿼리 스트링)      → URL로 데이터 주고받기
 * - ex07: 레이아웃 연습 (layout)              → 공통 틀(헤더/푸터) 재사용하기
 *
 * 컨트롤러(Controller)란?
 * - 사용자의 요청(URL 접속)을 받아서, 필요한 데이터를 준비하고,
 *   어떤 화면(HTML 파일)을 보여줄지 결정하는 "안내 데스크" 역할입니다.
 * - 비유: 레스토랑의 웨이터가 주문(요청)을 받고, 주방(서비스)에 전달하고,
 *   음식(데이터)을 손님(화면)에게 가져다주는 것과 같습니다.
 */
@Controller                          // Spring: 이 클래스가 웹 요청을 처리하는 컨트롤러임을 선언합니다
@RequestMapping(value="/thymeleaf")  // 이 컨트롤러의 모든 URL은 "/thymeleaf"로 시작합니다 (공통 경로 설정)
public class ThymeleafExController {

    /**
     * [예제 1] 단순 텍스트 데이터를 화면에 전달하는 예제
     *
     * 비유: 웨이터가 손님에게 "오늘의 인사말"이 적힌 카드를 가져다주는 것입니다.
     *
     * URL: /thymeleaf/ex1212 (GET 요청)
     *
     * @param model - 화면(View)으로 데이터를 전달하는 "택배 상자" 역할의 객체.
     *                model.addAttribute("키", 값)으로 데이터를 담으면,
     *                HTML에서 th:text="${키}"로 꺼내 쓸 수 있습니다.
     * @return "thymeleafEx/thymeleafEx01" - templates/thymeleafEx/thymeleafEx01.html 파일을 화면으로 보여줍니다
     */
    @GetMapping(value = "/ex1212")  // GET 방식으로 /thymeleaf/ex1212 URL 요청이 오면 이 메서드가 실행됩니다
    public String thymeleafExample01(Model model){
        model.addAttribute("data", "타임리프 예제 입니다.");  // "data"라는 이름으로 문자열을 화면에 전달
        return "thymeleafEx/thymeleafEx01";  // templates 폴더 기준으로 뷰의 위치와 이름을 반환합니다
    }

    /**
     * [예제 2] DTO 객체 하나를 화면에 전달하는 예제
     *
     * 비유: 웨이터가 손님에게 상품 카탈로그 1페이지를 가져다주는 것입니다.
     * 카탈로그에는 상품명, 가격, 상세설명, 등록일이 적혀 있습니다.
     *
     * URL: /thymeleaf/ex02 (GET 요청)
     * Thymeleaf에서 사용: th:text="${itemDto.itemNm}" 등으로 객체의 각 필드에 접근
     *
     * @param model - 화면으로 데이터를 전달하는 객체
     * @return "thymeleafEx/thymeleafEx02" - templates/thymeleafEx/thymeleafEx02.html 파일을 화면으로 보여줍니다
     */
    @GetMapping(value = "/ex02")
    public String thymeleafExample02(Model model){
        ItemDto itemDto = new ItemDto();              // 빈 상품 DTO 객체 생성
        itemDto.setItemDetail("상품 상세 설명");        // 상품 상세 설명 설정
        itemDto.setItemNm("테스트 상품1");              // 상품명 설정
        itemDto.setPrice(10000);                       // 상품 가격 설정 (10,000원)
        itemDto.setRegTime(LocalDateTime.now());       // 등록 시간을 현재 시간으로 설정

        model.addAttribute("itemDto", itemDto);        // "itemDto"라는 이름으로 상품 객체를 화면에 전달
        return "thymeleafEx/thymeleafEx02";
    }

    /**
     * [예제 3] DTO 객체 리스트(여러 개)를 화면에 전달하는 예제 - th:each(반복문) 학습용
     *
     * 비유: 웨이터가 손님에게 상품 카탈로그 10페이지를 한꺼번에 가져다주는 것입니다.
     * Thymeleaf의 th:each를 사용하면 리스트를 반복하여 각 상품을 화면에 출력할 수 있습니다.
     *
     * URL: /thymeleaf/ex03 (GET 요청)
     * Thymeleaf에서 사용: th:each="itemDto : ${itemDtoList}" 로 반복 출력
     *
     * @param model - 화면으로 데이터를 전달하는 객체
     * @return "thymeleafEx/thymeleafEx03" - 반복문(th:each) 예제 화면
     */
    @GetMapping(value = "/ex03")
    public String thymeleafExample03(Model model){

        List<ItemDto> itemDtoList = new ArrayList<>();  // 상품 DTO를 담을 빈 리스트 생성

        // 10개의 테스트 상품을 생성하여 리스트에 추가
        for(int i=1;i<=10;i++){

            ItemDto itemDto = new ItemDto();
            itemDto.setItemDetail("상품 상세 설명"+i);   // "상품 상세 설명1", "상품 상세 설명2", ...
            itemDto.setItemNm("테스트 상품" + i);        // "테스트 상품1", "테스트 상품2", ...
            itemDto.setPrice(1000*i);                    // 1000원, 2000원, ... 10000원
            itemDto.setRegTime(LocalDateTime.now());     // 현재 시간

            itemDtoList.add(itemDto);                    // 리스트에 상품 추가
        }

        model.addAttribute("itemDtoList", itemDtoList);  // "itemDtoList"라는 이름으로 상품 리스트를 화면에 전달
        return "thymeleafEx/thymeleafEx03";
    }

    /**
     * [예제 4] DTO 리스트 + 조건부 출력 예제 - th:if, th:unless(조건문) 학습용
     *
     * 비유: 상품 카탈로그를 보여주되, "짝수 번째 상품만 강조 표시" 같은 조건을 추가한 것입니다.
     * 데이터는 예제 3과 동일하지만, HTML 템플릿에서 조건부 렌더링을 연습합니다.
     *
     * URL: /thymeleaf/ex04 (GET 요청)
     *
     * @param model - 화면으로 데이터를 전달하는 객체
     * @return "thymeleafEx/thymeleafEx04" - 조건문(th:if, th:unless) 예제 화면
     */
    @GetMapping(value = "/ex04")
    public String thymeleafExample04(Model model){

        List<ItemDto> itemDtoList = new ArrayList<>();

        for(int i=1;i<=10;i++){

            ItemDto itemDto = new ItemDto();
            itemDto.setItemDetail("상품 상세 설명"+i);
            itemDto.setItemNm("테스트 상품" + i);
            itemDto.setPrice(1000*i);
            itemDto.setRegTime(LocalDateTime.now());

            itemDtoList.add(itemDto);
        }

        model.addAttribute("itemDtoList", itemDtoList);
        return "thymeleafEx/thymeleafEx04";
    }

    /**
     * [예제 5] Thymeleaf 링크(URL) 처리 예제 - th:href 학습용
     *
     * 비유: 레스토랑 메뉴판에 "자세히 보기" 링크를 추가하는 것입니다.
     * 별도의 데이터 전달 없이, HTML 템플릿에서 URL 생성 방법을 연습합니다.
     *
     * URL: /thymeleaf/ex05 (GET 요청)
     *
     * @return "thymeleafEx/thymeleafEx05" - 링크(th:href) 예제 화면
     */
    @GetMapping(value = "/ex05")
    public String thymeleafExample05(){
        return "thymeleafEx/thymeleafEx05";
    }

    /**
     * [예제 6] URL 파라미터(쿼리 스트링) 전달 예제
     *
     * 비유: 손님이 "매운맛으로, 곱빼기로 주세요"라고 조건을 말하면
     * 웨이터가 그 조건을 주방에 전달하고, 다시 손님에게 확인시켜주는 것입니다.
     *
     * URL: /thymeleaf/ex06?param1=값1&param2=값2 (GET 요청)
     * 예: /thymeleaf/ex06?param1=안녕&param2=하세요
     *
     * @param param1 - URL에서 전달받는 첫 번째 파라미터 (쿼리 스트링 ?param1=값)
     * @param param2 - URL에서 전달받는 두 번째 파라미터 (쿼리 스트링 &param2=값)
     * @param model  - 화면으로 데이터를 전달하는 객체
     * @return "thymeleafEx/thymeleafEx06" - 파라미터 출력 예제 화면
     */
    @GetMapping(value = "/ex06")
    public String thymeleafExample06(String param1, String param2, Model model){
        model.addAttribute("param1", param1);  // 전달받은 param1을 화면에 다시 전달
        model.addAttribute("param2", param2);  // 전달받은 param2를 화면에 다시 전달
        return "thymeleafEx/thymeleafEx06";
    }

    /**
     * [예제 7] Thymeleaf 레이아웃(layout) 적용 예제
     *
     * 비유: 모든 페이지에 공통으로 들어가는 "머리글(헤더)"과 "바닥글(푸터)"을
     * 매번 새로 만들지 않고, 하나의 틀(레이아웃)을 정해놓고 재사용하는 것입니다.
     * 마치 편지지 양식처럼, 상단/하단은 고정이고 내용만 바꿔 끼우는 방식입니다.
     *
     * URL: /thymeleaf/ex07 (GET 요청)
     *
     * @return "thymeleafEx/thymeleafEx07" - 레이아웃 적용 예제 화면
     */
    @GetMapping(value = "/ex07")
    public String thymeleafExample07(){
        return "thymeleafEx/thymeleafEx07";
    }

}
