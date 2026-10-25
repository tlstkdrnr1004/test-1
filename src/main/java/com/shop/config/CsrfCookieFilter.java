package com.shop.config;

import jakarta.servlet.FilterChain; // 서블릿 필터 체인의 다음 필터나 최종 리소스(컨트롤러)로 요청을 넘기는 데 사용됩니다.
import jakarta.servlet.ServletException; // 서블릿 관련 예외를 처리하기 위한 클래스입니다.
import jakarta.servlet.http.HttpServletRequest; // HTTP 요청 정보를 담는 객체입니다.
import jakarta.servlet.http.HttpServletResponse; // HTTP 응답 정보를 담는 객체입니다.
import org.springframework.security.web.csrf.CsrfToken; // CSRF 토큰 객체를 나타내는 인터페이스입니다.
import org.springframework.web.filter.OncePerRequestFilter; // Spring이 제공하는 필터의 기본 클래스로, 요청당 한 번만 실행됨을 보장합니다.

import java.io.IOException;

// 이 클래스는 사용자의 웹 요청이 들어올 때마다 한 번씩 실행되는 Spring Security 필터입니다.
// 주된 목적은 Spring Security가 내부적으로 생성한 CSRF 토큰을 강제로 '활성화'하여,
// 'CookieCsrfTokenRepository' 설정에 따라 이 CSRF 토큰이 HTTP 응답 메시지의 쿠키에 포함되어
// 클라이언트(브라우저)로 안전하게 전달되도록 돕는 것입니다.
// 이를 통해 프론트엔드(특히 JavaScript)에서 CSRF 토큰을 쉽게 읽어 다음 요청에 포함시킬 수 있게 됩니다.
public class CsrfCookieFilter extends OncePerRequestFilter { //
    /**
     * 이 메서드는 HTTP 요청이 필터 체인을 통과할 때마다 자동으로 호출되는 핵심 로직입니다.
     * 여기서는 들어오는 요청에서 CSRF 토큰을 확인하고, 필요한 경우 토큰을 쿠키로 발행하도록 유도하는 역할을 합니다.
     *
     * @param request     - 현재 HTTP 요청에 대한 정보를 담고 있는 객체입니다 (예: 요청 URL, 헤더, 파라미터 등).
     * @param response    - 서버가 클라이언트에게 보낼 HTTP 응답을 구성하는 객체입니다 (예: 응답 헤더, 상태 코드, 본문 등).
     * @param filterChain - 필터 체인에서 다음 필터나 최종 목적지(예: Spring MVC 컨트롤러)로 요청과 응답을 전달하는 데 사용됩니다.
     * @throws ServletException - 서블릿 관련 예외 발생 시 던져집니다.
     * @throws IOException      - 입출력 관련 예외 발생 시 던져집니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /**
         * [1] CSRF 토큰의 역할 이해:
         * - CSRF(Cross-Site Request Forgery)는 공격자가 사용자의 로그인된 세션을 이용하여
         * 사용자 몰래 원치 않는 요청(예: 비밀번호 변경, 송금)을 보내는 웹 보안 취약점입니다.
         * - CSRF 토큰은 이러한 공격을 방지하기 위한 보안 메커니즘으로, 서버가 각 사용자 세션에 대해
         * 고유한 '비밀 키'를 발급하고, 모든 중요한 요청에 이 키가 포함되어 있는지 확인하여
         * 요청이 진짜 사용자로부터 온 것인지 검증하는 데 사용됩니다.
         *
         * [2] 요청에서 CSRF 토큰 추출:
         * - Spring Security는 요청이 들어올 때 CSRF 토큰을 자동으로 생성하거나 기존 토큰을 로드하여
         * `HttpServletRequest` 객체의 '속성(Attribute)'으로 저장해둡니다.
         * - `getAttribute(CsrfToken.class.getName())`는 바로 이 요청 속성에서
         * `CsrfToken` 객체를 찾아 가져오는 코드입니다.
         * - `CsrfToken.class.getName()`은 `CsrfToken` 클래스의 전체 이름(예: "org.springframework.security.web.csrf.CsrfToken")을 반환하며,
         * 이 이름이 Spring Security가 토큰을 속성에 저장할 때 사용하는 키(key)로 사용됩니다.
         */
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName()); //

        /**
         * CSRF 토큰 '활성화' 및 쿠키 발행 유도:
         * - `if (csrfToken != null)`: 현재 요청에 CSRF 토큰이 존재할 경우에만 다음 로직을 실행합니다.
         * - `csrfToken.getToken()`: 이 메서드를 호출하는 것이 중요합니다.
         * 겉보기에는 단순히 토큰 문자열 값을 가져오는 것처럼 보이지만,
         * Spring Security의 내부 구현(특히 'CookieCsrfTokenRepository' 사용 시)에서는
         * 이 `getToken()` 호출이 **토큰이 '사용되었음'을 나타내는 트리거**가 됩니다.
         * 이렇게 토큰이 '사용된' 것으로 간주되면, Spring Security는 응답이 완료되기 전에
         * 새로운 또는 갱신된 CSRF 토큰을 HTTP 응답 헤더의 `Set-Cookie` 필드를 통해
         * 클라이언트(브라우저)의 쿠키로 전송하도록 자동으로 처리합니다.
         * - 이 필터가 없다면, 클라이언트(브라우저)는 CSRF 토큰이 필요한 시점에도 쿠키로 토큰을 받지 못할 수 있습니다.
         * 특히, GET 요청(데이터를 조회하는 요청)과 같이 상태를 변경하지 않는 요청에서는
         * 기본적으로 CSRF 토큰이 검증되지 않아 토큰이 쿠키로 발행되지 않을 수 있는데,
         * 이 필터는 이러한 경우에도 토큰을 강제로 '사용된 것처럼' 만들어 쿠키 발행을 유도합니다.
         */
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        /**
         * 필터 체인의 다음 단계로 요청 전달:
         * - `filterChain.doFilter(request, response)`: 현재 필터의 작업을 완료한 후,
         * 요청과 응답 객체를 필터 체인의 다음 필터로, 또는 더 이상 필터가 없으면
         * 최종 목적지인 Spring MVC 컨트롤러(또는 다른 리소스)로 전달합니다.
         * - 이 호출이 없으면 요청 처리가 이 필터에서 중단되어 클라이언트에 아무런 응답이 가지 않거나,
         * 서버가 응답을 보내지 못하고 대기하는 상태가 될 수 있습니다.
         */
        filterChain.doFilter(request, response);
    }
}