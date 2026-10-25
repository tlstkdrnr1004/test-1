package com.shop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/*
    =====================================================================
    FormLoginAuthenticationFailureHandler - 로그인 실패 시 처리를 담당하는 클래스
    =====================================================================

    비유: 건물 출입구의 "경비원"과 같습니다.
    출입증(아이디/비밀번호)이 틀리면 경비원이 "출입증이 잘못됐습니다"라고 안내하며
    다시 안내 데스크(로그인 에러 페이지)로 보내주는 역할을 합니다.

    Spring Security에서 로그인(인증)이 실패했을 때 어떤 동작을 할지 정의합니다.
    AuthenticationFailureHandler 인터페이스를 구현하여, 실패 시 에러 페이지로 리다이렉트합니다.
 */

// AuthenticationFailureHandler: Spring Security가 제공하는 인터페이스로,
// 로그인 실패 시 "어떻게 처리할 것인가"를 정의하는 계약(규칙)입니다.
public class FormLoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    // onAuthenticationFailure: 로그인 실패 시 Spring Security가 자동으로 호출하는 메서드입니다.
    // - request  : 사용자가 보낸 HTTP 요청 정보 (아이디, 비밀번호 등이 담겨 있음)
    // - response : 서버가 사용자에게 보낼 HTTP 응답 (여기서 리다이렉트를 설정)
    // - exception: 로그인이 왜 실패했는지에 대한 예외 정보 (비밀번호 틀림, 계정 잠김 등)
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 로그인 실패 시 "/members/login/error" 페이지로 사용자를 이동(리다이렉트)시킵니다.
        // 이 페이지에서 "아이디 또는 비밀번호를 확인해주세요" 같은 에러 메시지를 보여줍니다.
        response.sendRedirect("/members/login/error");
    }

}
