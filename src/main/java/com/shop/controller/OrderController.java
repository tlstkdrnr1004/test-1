package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.shop.dto.OrderHistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 📌 클라이언트에서 "/order" 경로로 POST 요청을 보내면 이 메서드가 해당 주문 요청을 처리함
    // 예: ItemDtl.html에서 주문 버튼 클릭 시 AJAX 요청 발생
    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order(

            // ✅ 클라이언트가 보낸 JSON 데이터를 OrderDto 객체로 매핑
            // - @RequestBody: HTTP 요청 body의 JSON을 자바 객체로 자동 변환
            // - @Valid: OrderDto 내 필드에 선언된 Bean Validation (@NotNull 등)을 자동 검사
            @RequestBody @Valid OrderDto orderDto,

            // ✅ 유효성 검사 결과를 저장하는 객체
            // - @Valid에 의해 검증된 결과(성공 또는 실패)를 가지고 있음
            // - 검증 실패 시 에러 메시지들을 확인 가능
            BindingResult bindingResult,

            // ✅ 현재 로그인한 사용자의 인증 정보를 나타내는 객체
            // - Java의 표준 인터페이스인 java.security.Principal을 사용
            // - Spring Security가 로그인 후 사용자의 인증 정보를 SecurityContext에 저장
            // - 컨트롤러 메서드에서 Principal을 파라미터로 선언하면, Spring이 자동으로 현재 로그인한 사용자의 정보를 주입함
            // - principal.getName() 메서드를 호출하면 로그인한 사용자의 "username"을 반환 (일반적으로 이메일이나 사용자 ID로 구성됨)
            //
            // 🔍 예시 상황:
            //   - 로그인한 사용자가 "user1@example.com"이라면,
            //     principal.getName() → "user1@example.com"
            //
            // 🔐 내부 동작 요약:
            //   - 사용자가 로그인 성공 시 Authentication 객체가 생성되고, SecurityContext에 저장됨
            //   - 이 Authentication 객체 안에 Principal(UserDetails 구현 객체 또는 username 문자열)이 포함됨
            //   - 컨트롤러에서 Principal 타입 파라미터를 선언하면 이 정보를 자동으로 주입해줌
            Principal principal

    ) {
        // ✅ 1. 유효성 검사 실패 처리
        // - OrderDto에 정의된 검증 조건(@NotNull 등)을 위반한 경우
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();  // 모든 에러 메시지를 누적할 문자열 버퍼 생성

            List<FieldError> fieldErrors = bindingResult.getFieldErrors(); // 필드별 에러 정보 수집

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());  // 각 에러 메시지를 문자열에 추가
            }

            // ❗ 클라이언트에게 400 Bad Request 상태 코드와 함께 에러 메시지 전송
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // ✅ 2. 인증된 사용자(주문자)의 이메일 주소 가져오기
        String email = principal.getName(); // 로그인한 사용자의 고유 식별자(보통 이메일)

        Long orderId;  // 주문 결과로 반환될 주문 ID

        try {
            // ✅ 3. 주문 서비스 호출 → 실제 주문 로직 수행
            // - orderDto: 주문할 상품과 수량 등의 정보
            // - email: 주문자 식별 정보
            orderId = orderService.order(orderDto, email);

        } catch (Exception e) {
            // ❌ 예외 발생 시 (ex: 품절, 재고 부족 등) 클라이언트에 400 Bad Request와 예외 메시지 전송
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // ✅ 4. 주문 성공 시 주문 ID와 200 OK 상태 코드 반환
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }



    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId , Principal principal){

        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderService.cancelOrder(orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}