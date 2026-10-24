package com.shop.config; // 이 클래스가 속한 패키지를 선언합니다.

import jakarta.servlet.ServletException; // 서블릿 관련 예외를 처리하기 위한 클래스입니다.
import jakarta.servlet.http.HttpServletRequest; // HTTP 요청 정보를 담는 객체입니다.
import jakarta.servlet.http.HttpServletResponse; // HTTP 응답 정보를 담는 객체입니다.
import org.springframework.security.core.AuthenticationException; // Spring Security에서 인증 실패 시 발생하는 최상위 예외 클래스입니다.
import org.springframework.security.web.AuthenticationEntryPoint; // 인증되지 않은 사용자가 보호된 자원에 접근할 때 호출되는 인터페이스입니다.

import java.io.IOException;

// CustomAuthenticationEntryPoint 클래스는 Spring Security의 AuthenticationEntryPoint 인터페이스를 구현합니다.
// 이 클래스의 목적은 인증되지 않은 사용자가 인증이 필요한 리소스에 접근을 시도했을 때,
// 기본 로그인 페이지 리다이렉션 대신 사용자 정의 응답을 제공하는 것입니다.
// 여기서는 간단하게 HTTP 상태 코드 401 (Unauthorized)을 반환하도록 설정합니다.
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 이 메서드는 사용자가 인증되지 않은 상태로 보호된 자원(예: 로그인 후 볼 수 있는 페이지)에 접근하려 할 때
     * Spring Security에 의해 자동으로 호출됩니다.
     *
     * @param request       - 클라이언트로부터의 HTTP 요청을 담고 있는 HttpServletRequest 객체입니다.
     * @param response      - 클라이언트에게 보낼 HTTP 응답을 담고 있는 HttpServletResponse 객체입니다.
     * @param authException - 인증 실패와 관련된 예외 객체입니다. 어떤 종류의 인증 실패가 발생했는지 정보를 포함합니다.
     * @throws IOException      - 입출력 작업 중 오류 발생 시 던져집니다.
     * @throws ServletException - 서블릿 관련 오류 발생 시 던져집니다.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 'response.sendError()' 메서드는 HTTP 응답에 오류 상태 코드와 메시지를 설정하여 클라이언트에게 전송합니다.
        // HttpServletResponse.SC_UNAUTHORIZED는 HTTP 상태 코드 401을 의미하며,
        // "Unauthorized"는 요청된 리소스에 접근하기 위한 유효한 인증 자격 증명이 부족하다는 것을 나타냅니다.
        // 이 코드는 클라이언트(브라우저 또는 API 호출자)에게 "인증되지 않았습니다"라는 명확한 신호를 보냅니다.
        // 이는 특히 REST API 환경에서 클라이언트가 HTTP 상태 코드를 통해 서버의 응답을 이해하는 데 유용합니다.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

}