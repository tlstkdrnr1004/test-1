package com.shop.controller;
import com.shop.dto.CartItemDto;
import com.shop.service.CartService;
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

import com.shop.dto.CartDetailDto;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.DeleteMapping;

import com.shop.dto.CartOrderDto;

/**
 * 장바구니 컨트롤러 (CartController)
 *
 * 비유: 마트에서 장바구니를 관리하는 "직원"이라고 생각하면 됩니다.
 *       고객이 "이 상품 담아주세요", "수량 바꿔주세요", "이거 빼주세요", "계산해주세요"라고 요청하면
 *       이 직원이 해당 요청을 접수하고, 실제 처리는 CartService(창고 담당)에게 넘깁니다.
 *
 * 역할: 사용자의 HTTP 요청(장바구니 담기, 조회, 수정, 삭제, 주문)을 받아서
 *       CartService에 위임하고, 결과를 클라이언트에게 응답으로 돌려줍니다.
 */
@Controller // 이 클래스가 Spring MVC의 컨트롤러임을 선언합니다. 웹 요청을 처리하는 역할입니다.
@RequiredArgsConstructor // final 필드(cartService)에 대한 생성자를 Lombok이 자동으로 만들어줍니다. 의존성 주입에 사용됩니다.
public class CartController {

    private final CartService cartService; // 장바구니 비즈니스 로직을 처리하는 서비스. 컨트롤러는 요청만 받고, 실제 일은 서비스가 합니다.

    /**
     * 장바구니에 상품 담기
     *
     * 비유: 마트에서 고객이 "이 상품 3개 담아주세요"라고 말하면,
     *       직원이 상품과 수량을 확인하고 장바구니에 넣어주는 과정입니다.
     *
     * @param cartItemDto   담을 상품 정보 (상품 ID, 수량)
     * @param bindingResult 입력값 검증 결과 (수량이 0 이하이거나 상품ID가 없으면 에러 발생)
     * @param principal     현재 로그인한 사용자 정보 (누구의 장바구니인지 식별)
     * @return 성공 시 장바구니 상품 ID, 실패 시 에러 메시지
     */
    @PostMapping(value = "/cart") // POST /cart 요청을 이 메서드가 처리합니다. POST는 "새로 만들기(담기)" 요청에 사용됩니다.
    public @ResponseBody ResponseEntity order(
            @RequestBody @Valid CartItemDto cartItemDto, // @RequestBody: JSON 데이터를 CartItemDto 객체로 변환, @Valid: 유효성 검증 실행
            BindingResult bindingResult, // 유효성 검증 결과가 담기는 객체. 에러가 있으면 hasErrors()가 true를 반환합니다.
            Principal principal){ // Spring Security가 제공하는 현재 로그인 사용자 정보. getName()으로 이메일을 가져옵니다.

        // 입력값 검증에 실패한 경우 (예: 수량이 0 이하, 상품 ID가 비어있는 경우)
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder(); // 에러 메시지를 하나의 문자열로 합치기 위한 도구
            List<FieldError> fieldErrors = bindingResult.getFieldErrors(); // 어떤 필드에서 에러가 났는지 목록을 가져옵니다

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage()); // 각 에러의 메시지를 이어붙입니다
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST); // 400 에러와 함께 에러 메시지 반환
        }

        String email = principal.getName(); // 현재 로그인한 사용자의 이메일을 가져옵니다
        Long cartItemId; // 장바구니에 담긴 상품의 ID를 저장할 변수

        try {
            cartItemId = cartService.addCart(cartItemDto, email); // 장바구니에 상품을 담는 실제 로직을 서비스에 위임합니다
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST); // 예외 발생 시 에러 메시지와 400 상태 반환
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK); // 성공 시 장바구니 상품 ID와 200 상태 반환
    }

    /**
     * 장바구니 목록 페이지 조회
     *
     * 비유: 마트에서 장바구니 안에 뭐가 들어있는지 꺼내서 보여주는 것과 같습니다.
     *       상품명, 가격, 수량, 이미지를 한눈에 볼 수 있도록 화면에 표시합니다.
     *
     * @param principal 현재 로그인한 사용자 정보
     * @param model     화면(View)에 데이터를 전달하는 객체
     * @return 장바구니 목록 화면(cart/cartList.html)
     */
    @GetMapping(value = "/cart") // GET /cart 요청을 이 메서드가 처리합니다. GET은 "조회" 요청에 사용됩니다.
    public String orderHist(Principal principal, Model model){
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName()); // 현재 사용자의 장바구니 상품 목록을 가져옵니다
        model.addAttribute("cartItems", cartDetailList); // "cartItems"라는 이름으로 데이터를 화면에 전달합니다
        return "cart/cartList"; // cart/cartList.html 템플릿을 렌더링하여 반환합니다
    }

    /**
     * 장바구니 상품 수량 변경
     *
     * 비유: 마트에서 "사과 3개 담았는데 5개로 바꿔주세요"라고 요청하는 것과 같습니다.
     *       본인의 장바구니만 수정할 수 있도록 권한을 확인합니다.
     *
     * @param cartItemId 수량을 변경할 장바구니 상품 ID (URL 경로에서 가져옴)
     * @param count      변경할 수량
     * @param principal  현재 로그인한 사용자 정보
     * @return 성공 시 장바구니 상품 ID, 실패 시 에러 메시지
     */
    @PatchMapping(value = "/cartItem/{cartItemId}") // PATCH 요청: 기존 데이터를 "부분 수정"할 때 사용합니다. {cartItemId}는 URL 경로 변수입니다.
    public @ResponseBody ResponseEntity updateCartItem(
            @PathVariable("cartItemId") Long cartItemId, // URL의 {cartItemId} 값을 변수로 받습니다
            int count, // 변경할 수량
            Principal principal){ // 현재 로그인한 사용자 정보

        // 수량이 0 이하인 경우 에러 반환
        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        // 본인의 장바구니 상품이 아닌 경우 권한 에러 반환
        } else if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN); // 403 Forbidden: 권한 없음
        }

        cartService.updateCartItemCount(cartItemId, count); // 수량 변경을 서비스에 위임합니다
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK); // 성공 시 200 상태와 함께 장바구니 상품 ID 반환
    }

    /**
     * 장바구니 상품 삭제
     *
     * 비유: 마트에서 "이 상품 장바구니에서 빼주세요"라고 요청하는 것과 같습니다.
     *       역시 본인의 장바구니 상품만 삭제할 수 있습니다.
     *
     * @param cartItemId 삭제할 장바구니 상품 ID
     * @param principal  현재 로그인한 사용자 정보
     * @return 성공 시 삭제된 장바구니 상품 ID, 실패 시 에러 메시지
     */
    @DeleteMapping(value = "/cartItem/{cartItemId}") // DELETE 요청: 데이터를 "삭제"할 때 사용합니다.
    public @ResponseBody ResponseEntity deleteCartItem(
            @PathVariable("cartItemId") Long cartItemId, // URL에서 삭제할 상품 ID를 가져옵니다
            Principal principal){

        // 본인의 장바구니 상품인지 확인. 아니면 권한 에러 반환
        if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId); // 삭제를 서비스에 위임합니다

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK); // 성공 시 200 상태 반환
    }

    /**
     * 장바구니 상품 주문하기
     *
     * 비유: 마트에서 장바구니에 담은 상품들을 "계산대"로 가져가서 결제하는 것과 같습니다.
     *       선택한 상품들을 주문으로 전환하고, 장바구니에서는 제거됩니다.
     *
     * @param cartOrderDto     주문할 장바구니 상품 목록 (여러 개의 장바구니 상품 ID를 포함)
     * @param principal        현재 로그인한 사용자 정보
     * @return 성공 시 주문 ID, 실패 시 에러 메시지
     */
    @PostMapping(value = "/cart/orders") // POST /cart/orders: 장바구니 상품들을 주문으로 전환하는 요청
    public @ResponseBody ResponseEntity orderCartItem(
            @RequestBody CartOrderDto cartOrderDto, // JSON으로 전달된 주문 정보를 객체로 변환
            Principal principal){

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList(); // 주문할 장바구니 상품 목록을 가져옵니다

        // 주문할 상품이 하나도 없는 경우 에러 반환
        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        // 각 장바구니 상품이 본인의 것인지 하나씩 확인합니다 (다른 사람의 장바구니 상품을 주문할 수 없도록)
        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName()); // 주문 처리를 서비스에 위임합니다
        return new ResponseEntity<Long>(orderId, HttpStatus.OK); // 성공 시 생성된 주문 ID와 200 상태 반환
    }

}
