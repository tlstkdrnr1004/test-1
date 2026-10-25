// 이 클래스는 Spring Security 설정을 정의하는 구성 클래스입니다.
package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 스프링 시큐리티 관련 주요 클래스 임포트
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/*
 참고.
 	자바에서 람다 표현식
 	 - 간결한 방식으로 익명함수를 표현하기 위해 사용됩니다.
 	   람다 표현식은 주로 함수형 인터페이스를 구현할때 사용되며,
 	   함수형 인터페이스는 오직 하나의 추상메소드만을 가지고 있는 인터페이스를 말합니다.

 	람다 표현식의 문법

 	   	(매개변수 목록) -> { 실행문 }

 	   	- 매개변수 목록 : 메소드의 매개변수 목록처럼, 람다 표현식에서도 입력으로 사용될 변수들을 정의합니다.
 	   	  			   매개변수 타입은 생략할수 있으며, 매개변수가 하나뿐이라면 괄호도 생략할수 있습니다.
 	   	-  -> 화살표  :  매개변수 목록과 람다 몸체를 구분하는 역할을 합닌다.
 	   	-  실행문 : 람다 표현식의 본체로, 실제 작업을 수행하는 코드가 들어갑니다.
 	   	           실행문이 단일 문장이라면 중괄호를 생략할수 있습니다.
 	   	           하지만 실행문이 반환문이거나 여러 개라면 중괄호를 사용해야 합니다.

 	 람다 표현식 예시

 	 	예시1.   매개변수가 없는 람다 표현식 코드

 	 	()  -> System.out.println("Hello, Lambda!");

 	 	예시2.  매개변수 하나를 받은 람다 표현식 코드

 	 	(int a) -> a * a;

 	 		매개변수가 하나일 경우 괄호 생략 가능 코드
 	 		a -> a * a;

 	 	예시3. 매개변수 두개를 받고 본문이 여러 줄인 람다 표현식 코드

 	 	(int a, int b) -> {
 	 						int sum = a + b;
 	 						System.out.println(sum);
 	 					    return sum;
 	 					  };

 	 	예시4. 람다 표현식을 인자로 사용하는 예 코드

 	 	List<String> names = Arrays.asList("Steve", "John", "kim", "Park");

 	 	Collections.sort( names, (String s1,  String s2) -> s1.compareTo(s2) );


 이러한 람다 표현식은 코드를 더 간결하게 만들고, 메소드의 매개변수로 전달되거나 변수에 할당되는 등
 다양한 상황에서 유용하게 사용됩니다.
 함수형 엔터페이스를 사용하는 다양한 자바 API에서 람다 표현식은 코드를 훨씬 더 직관적이고 간결하게 만들어줍니다.

 */

//스프링 시큐리티의 설정을 담당할 SecurityConfig.java
    //이 코드는 웹 애플리케이션에서 모든 HTTP요청을 허용하도록  Spring Security를 설정합니다.
    //실제 애플리케이션에서는 특정 경로에 대한 접근을 제한 하거나 사용자 인증을 요구하는 등 더 복잡한 보안 설정이 필요할수 있습니다.
    //인증 (Authentication) 관련 설정 : 사용자가 로그인하여 신원을 확인하는 과정과 관련된 부분입니다.
    //권한 (Authorization) 관련 설정 : 인증된 사용자가 특정 리소스에 접근할 수 있는지 여부를 결정하는 부분입니다.

//@Configuration은  이 파일이 스프링의 환경 설정 파일임을 의미하는 에너테이션 기호 입니다.
//				   여기서는 스프링 시큐리티를 설정하기 위해 사용했습니다.
//@EnableWebSecurity 는 모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 애너테이션 입니다.
//이 어노테이션을 사용하면 스프링 시큐리티를 활성화하는 역할을 합니다.
//내부적으로 SecurityFilterChain클래스가 동작 하여 모든 요청 URL에 이 클래스가 필터로 적용되어
//URL별로 특별한 설정을 할수 있게 됩니다.
//스프링 시큐리티의 세부 설정은  @Bean 애너테이션을 통해 SecurityFilterChain 빈을 자동생성하여 설정할수 있음
@Configuration // 스프링 설정 클래스임을 나타냄
@EnableWebSecurity // 스프링 시큐리티 활성화 (자동 설정 + 사용자 정의 설정 동작하게 함)
public class SecurityConfig {

    //인증 (Authentication) 관련 설정
        //사용자가 로그인하여 신원을 확인하는 과정과 관련된 부분입니다.
        //설명: MemberService는 사용자 ID(여기서는 이메일)와 비밀번호를 기반으로 사용자를 인증하고, 사용자 정보를 로드하는 역할을 합니다. 이는 인증 과정의 핵심입니다.
    @Autowired
    MemberService memberService; // 사용자의 인증/회원 정보를 가져오는 서비스 주입

    /*
    아래의 filterChain 메소드는????? Spring Security에서 클라이언트의  HTTP 요청(브라우저 요청)에 대한 보안 설정을 정의하는 핵심 메서드입니다.
    즉, "어떤 URL요청주소를 서버가 받았을때  누가 접근할 수 있는가?"를 정해주는 **보안 필터 체인(SecurityFilterChain 객체)**을 만들어서 스프링 컨테이너에 등록(보관)하는 것입니다.
    다시 말해  @Bean은 스프링 프레임워크에서 직접 만든 객체를 스프링 빈(Bean)으로 등록하겠다는 뜻입니다.
    즉, 이 메서드에서 리턴하는 SecurityFilterChain 객체를 스프링 컨테이너에 사용하도록 등록(보관)해줍니다.
    이 설정은 Spring Security가 자동으로 적용하게 됩니다.
    */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                                          //HttpSecurity http
                                          //- Spring Security에서 보안 설정을 도와주는 HttpSecurity객체 입니다.
                                          //- HttpSecurity객체를 통해 URL 요청 주소로 서버페이지에 접속할 권한 설정, 로그인/로그아웃, CSRF 설정 등을 구성합니다.

/*
중요1.  CSRF(Cross-Site Request Forgery, 사이트 간 요청 위조) 뭔가요?

        CSRF 공격은 다음과 같은 방식으로 이루어져요:

            동작1. 사용자가 A사이트에 로그인한 상태에서

            동작2. B사이트(악성 사이트)에 접속했을 때,

            동작3. B사이트가 A사이트에 몰래 요청을 보내도록 유도하는 공격이에요.

            예: 사용자가 은행 사이트에 로그인한 상태에서, 다른 악성 사이트를 방문하면 그 악성 사이트가 사용자 몰래 "이체 요청"을 보낼 수 있어요.

                > 이런 걸 막기 위해 사용하는 것이 CSRF 토큰입니다.

중요2. Spring Security 에서 CSRF 공격을  보호 하는 3단계 방법

        1단계. 서버가 사용자에게 CSRF 토큰을 발급
                 - 사용자가 웹사이트에 처음 접속합니다.
                    (예: 웹 브라우저로 http://example.com 접속)

                 - 이때 서버(Spring Boot + Spring Security)는
                    **무작위의 문자열 (예: abc123xyz)**을 생성합니다.
                    이 문자열은 **"CSRF 토큰"**이라고 부릅니다.
                    이 값은 각 사용자마다 다르고, 매번 새로 생성됩니다.

                -  서버는 이 토큰을 사용자(요청한 클라이언트)의 브라우저에게 전달합니다.
                                        전달 방법은 2가지가 있습니다:
                                            방법1. HTML 내부에 <input type="hidden"> 태그로 포함
                                            방법2. 브라우저의 쿠키에 저장해서 보냄

        2단계. 사용자가 서버에 다시 요청을 보낼 때, 받았던 CSRF 토큰을 함께 보냄

                   - 사용자가 버튼을 클릭하거나, 폼을 제출하거나, Ajax로 데이터를 보낼 때,
                     서버에 요청을 보냅니다.
                        -> 이때 서버는 다음을 요구합니다: “내가 너한테 준 CSRF 토큰, 그거 같이 보내줘!”

                   - 사용자는 서버 요청에 어떻게 토큰을 포함시키나요?
                        방법 1: HTML <form> 태그 안에 CSRF 토큰이 자동으로 들어가 있습니다.

                                <form action="/delete" method="POST">
                                    <input type="hidden" name="_csrf" value="abc123xyz">
                                    <button type="submit">삭제</button>
                                </form>

                        방법 2: 자바스크립트(AJAX) 요청 시, HTTP 헤더에 넣어서 보냅니다.

                                axios.post("/delete", data, {
                                    headers: {
                                        "X-CSRF-TOKEN": "abc123xyz"
                                    }
                                });

                        이처럼 사용자가 서버에 요청을 보낼 때,
                        서버가 준 CSRF 토큰을 함께 전송해야 합니다.

            3단계. 서버가 토큰을 확인해서 요청을 처리할지 결정
                서버는 다음과 같은 검사를 합니다:
                - "요청 안에 들어온 CSRF 토큰이 내(서버)가 사용자(클라이언트의 브라우저)의 세션에 저장한 것과 같은가?"
                    1. 같으면: 요청을 정상적인 사용자 요청으로 인정하고 처리합니다.
                    2. 다르거나 없으면: 요청을 위조된 공격일 수 있다고 판단하고 거부합니다.


Spring Security 에서 CSRF 공격을  보호 하는 3단계 방법   요약하면 ????????????????????????????????????????????

                    | 시점      | 동작                       | 내용                                   |
                    | -------- | ------------------------  | ------------------------------------- |
                    | 사용자 접속| 서버가 CSRF 토큰 생성        | abc123xyz 같은 값을 만들어서 사용자에게 보냄 |
                    | 요청 발생  | 사용자가 폼 제출 or AJAX 요청 | CSRF 토큰을 같이 보냄                    |
                    | 요청 검사  | 서버가 토큰 비교             | 서버에 저장된 토큰과 같으면 요청 처리        |


중요3. 왜 이런 CSRF 공격을  보호 하는 보안 검사가 필요한가요?

    - 어떤 악성 사이트가 몰래 당신 계정으로 요청을 보내는 걸 막기 위해서입니다.

    - CSRF 토큰이 없다면, 누구나 서버에 몰래 요청을 보낼 수 있습니다.

    - 하지만 토큰을 사용하면, 서버가 진짜 사용자 요청인지 확인할 수 있게 되는 겁니다.

-----------------------------------------------------------------------------------------------------------------------------------------------------
                ####  먼저, 왜 이  http.csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository())) 코드가 필요한가요?

                        CSRF(사이트 간 요청 위조)는 해커가 당신이 로그인한 상태를 악용해서 몰래 요청을 보내는 공격입니다.

                        그래서 Spring Security는 아래 방식으로 이를 막습니다:

                         1. 서버가 사용자에게 CSRF 토큰(일회용 비밀번호 같은 것)을 줌

                         2. 사용자는 서버에 요청을 보낼 때 그 토큰을 같이 보냄

                         3. 서버는 요청에 포함된 토큰이 올바른지 확인 후 허용

                        그런데 여기서 문제:

                         - 기본 방식은 <form> 태그 안에만 CSRF 토큰이 들어갑니다.

                         - React나 Vue, 또는 JavaScript fetch, axios로 데이터를 보낼 때는 이 토큰이 빠짐
                            → 서버가 "CSRF 토큰 없어!"라며 요청을 차단합니다.

                        그래서 토큰을 HTML이 아닌 쿠키에 담아 보내기 위한 설정이 필요합니다.
                        그게 바로 아래 코드입니다:


아래의 람다표현식 .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository())) 으로 작성한 부분

                   일반 자바코드로 풀어서  작성 해보면?

                        //설명 . Spring Security -  org.springframework.security.web.csrf.CsrfFilter 클래스가
                        //                         HTTP 요청 매세지 전체의 Header영역 Cookie: 속성을 검사해
                        //
                        //                                              ----------HTTP 요청 매세지 전체의 Header영역----------
                        //                                              GET /somepage HTTP/1.1
                        //                                              Host: example.com
                        //                                              Cookie:
                        //                                              -----------------------------
                        //
                        //      HTTP 요청메세지안의 Cookie속성에  CSRF 토큰이 없으면 새 CSRF 토큰을 만들어
                        //      웹 서버가 응답 하는 HTTP 응답 메세지의 Header 영역 중  Set-Cookie 라는 속성에
                        //       기본 쿠키명 XSRF-TOKEN 과 함께  만든 새 CSRF 값을 저장합니다. 아래의 그림과 같이 말이죠?
                        //
                        //                                              ----------HTTP 응답 매세지 전체의 Header영역----------
                        //                                                HTTP/1.1 200 OK
                        //                                                Content-Type: text/html
                        //                                                Set-Cookie: XSRF-TOKEN=abcd1234; Path=/; HttpOnly
                        //                                              -------------------------------------------------------
                        //      그리고 CookieCsrfTokenRepository객체가 이 만든 새 CSRF 토큰이 저장된
                        //      "쿠키명=CSRF" ->   "XSRF-TOKEN=abcd1234;" 을
                        //      처음 서버의 사이트에 접속한 클라이언트(브라우저)에 전달 합니다.


                       참고. CookieCsrfTokenRepository 객체 주요 기능들
                               -  쿠키 이름 지정 (기본값은 XSRF-TOKEN)
                               -  쿠키에 토큰을 안전하게 저장  Set-Cookie: XSRF-TOKEN=abcd1234; (예: 보안 설정, 만료 시간 등)
                               -  HTTP 요청 매세지 전체의 Header영역에서 CSFR토큰 읽어오기
                               -  HTTP 응답 매세지 전체의 Header영역 중 Set-Cookie 라는 속성에  "쿠키명=CSRF" 형식으로 토큰 쓰기

//1. CsrfFilter가 생성한 CSRF 토큰을------------------------------------------------------------------------------------------------------------
//   HTTP 응답 매세지 전체의 Header영역에  Set-Cookie: XSRF-TOKEN=abcd1234; 이런식으로
//   포함해 기록하고 클라이언트(브라우저)에 전달해 저장하고 관리하는  CookieCsrfTokenRepository 객체를 생성합니다.
CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();



#################### CsrfConfigurer<HttpSecurity> csrfConfigurer = http.csrf(); 코드에 대한 사전 설명 ####################

                HttpSecurity http객체는 다양한 보안 설정(인증, 인가, CORS, CSRF 등)을 위한 메서드를 가지고 있습니다.
                이 중에서 .csrf()는 CSRF 관련 설정 체인(builder pattern)을 시작하는 메서드입니다.

                .csrf() 메서드를 호출하면 CsrfConfigurer<HttpSecurity> 타입의 객체가 반환됩니다.
                CsrfConfigurer: 이 클래스는 Spring Security에서 CSRF 보호 설정을 담당하는 전용 '설정 도우미' 객체입니다.

                이 객체를 통해 우리는 CSRF 보호에 대한 세부 사항(예: CSRF 토큰을 어디에 저장할지, 특정 요청에 대해 CSRF 보호를 비활성화할지 등)을 설정할 수 있습니다.

                <HttpSecurity>: 제네릭(<> 안에 있는 부분)은 이 CsrfConfigurer가 어떤 타입의 HttpSecurity 객체에 대한 설정을 돕는지 나타냅니다.
                즉, 이 CsrfConfigurer는 HttpSecurity 객체의 CSRF 설정을 담당한다는 의미입니다.

                따라서 CsrfConfigurer<HttpSecurity> csrfConfigurer = http.csrf();는
                http 객체로부터 CSRF 설정을 위한 CsrfConfigurer 인스턴스를 얻어와서 csrfConfigurer라는 변수에 저장하는 것입니다.
                이 변수를 통해 이제 CSRF 관련 설정을 이어갈 수 있게 됩니다.

//2.   HttpSecurity 객체에서 CSRF 보안 설정을 다루기 위한 전용 설정 객체(CsrfConfigurer)를 반환해 옵니다.
//      이 csrfConfigurer 객체를 통해 우리는 CSRF 토큰 관리 방식, CSRF 보호를 적용할지 말지 등을 세밀하게 설정할 수 있습니다. ---------------------------------------
CsrfConfigurer<HttpSecurity> csrfConfigurer = http.csrf();



#################### csrfConfigurer.csrfTokenRepository(csrfTokenRepository); 코드에 대한 사전 설명 ####################

이 코드는 Spring Security의 CSRF(사이트 간 요청 위조) 보호 설정에서 CSRF 토큰을 어디에, 어떤 방식으로 저장하고 관리할지를 최종적으로 결정하는 부분입니다.

1. csrfConfigurer.csrfTokenRepository() 메서드
    - csrfConfigurer 객체의 .csrfTokenRepository() 메서드를 호출하는 것은 **"CSRF 토큰을 저장하고 가져오는 방식을 설정하겠다"**는 의미입니다.
    - CSRF 토큰은 서버가 생성해서 클라이언트(브라우저)에게 보내고, 클라이언트가 다시 서버로 돌려보내는 과정에서 어딘가에 저장되어야 합니다.
      이 저장 방식을 이 메서드를 통해 지정하는 거예요.

2. csrfTokenRepository (매개변수)
이 메서드에 전달되는 csrfTokenRepository는 앞서 우리가 new CookieCsrfTokenRepository()로 생성했던 바로 그 객체입니다.
CookieCsrfTokenRepository는 CSRF 토큰을 브라우저의 쿠키에 저장하고, 요청 시 쿠키에서 CSRF 토큰을 읽어오는 전략을 구현한 클래스입니다.


// 3. 이 한 줄의 코드는 Spring Security의 CSRF 보호 메커니즘이 CSRF 토큰을 세션이 아닌 브라우저의 쿠키를 통해서 주고받도록 설정하는 핵심적인 부분입니다.
//    이를 통해 서버는 매번 새로운 토큰을 쿠키로 클라이언트에 전달하고, 클라이언트는 해당 토큰을 다시 서버로 보내 유효성을 검증받음으로써 CSRF 공격을 방지합니다.
csrfConfigurer.csrfTokenRepository(csrfTokenRepository);

-------------------------

아래의 람다표현식 .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository())) 으로 작성한 부분을
일반 자바코드로 풀어서  다시 작성 해보면?

                // 1. CsrfFilter가 생성한 CSRF 토큰을
                //    HTTP 응답 매세지 전체의 Header영역에  Set-Cookie: XSRF-TOKEN=abcd1234; 이런식으로
                //    포함해 기록하고 클라이언트(브라우저)에 전달해 저장하고 관리하는  CookieCsrfTokenRepository 객체를 생성합니다.
                CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();

                //2.   HttpSecurity 객체에서 CSRF 보안 설정을 다루기 위한 전용 설정 객체(CsrfConfigurer)를 반환해 옵니다.
                //     이 csrfConfigurer 객체를 통해 우리는 CSRF 토큰 관리 방식, CSRF 보호를 적용할지 말지 등을 세밀하게 설정할 수 있습니다.
                CsrfConfigurer<HttpSecurity> csrfConfigurer = http.csrf();

                // 3. 이 한 줄의 코드는 Spring Security의 CSRF 보호 메커니즘이 CSRF 토큰을 세션이 아닌 브라우저의 쿠키를 통해서 주고받도록 설정하는 핵심적인 부분입니다.
                //    이를 통해 서버는 매번 새로운 토큰을 쿠키로 클라이언트에 전달하고, 클라이언트는 해당 토큰을 다시 서버로 보내 유효성을 검증받음으로써 CSRF 공격을 방지합니다.
                csrfConfigurer.csrfTokenRepository(csrfTokenRepository);

 */
         return http
                 // CSRF(사이트 간 요청 위조) 보호 설정을 시작합니다.
                 // 람다 표현식을 사용하여 CSRF 설정을 직접 구성합니다.
                 .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
                                                         // CSRF 토큰을 HTTP 응답 헤더의 쿠키(Cookie)에 저장하고 관리하도록 설정합니다.
                                                         // 이는 CSRF 토큰이 서버 세션이 아닌 클라이언트(브라우저) 쿠키를 통해
                                                         // 주고받아지도록 하여 보안을 강화합니다.


                // Spring Security의 org.springframework.security.web.csrf.CsrfFilter 클래스가 'CsrfCookieFilter'를 추가하는 설정입니다.
                // CsrfFilter은 웹 요청이 애플리케이션에 도달하기 전 여러 보안 검사를 순서대로 수행하는
                // 일련의 필터들(예: 인증, 인가, CSRF 검사 등)의 집합입니다.
                 .addFilterAfter(
                                 new CsrfCookieFilter(), // 첫 번째 인자: 체인에 추가할 사용자 정의 필터 인스턴스.
                                 // 이 필터는 주로 CSRF 토큰을 HTTP 응답 쿠키에 자동으로 포함시켜
                                 // 클라이언트(브라우저)로 전달하는 역할을 수행합니다.
                                 // 이는 'CookieCsrfTokenRepository' 설정을 사용했을 때,
                                 // 실제 프론트엔드(JavaScript 등)에서 토큰을 쉽게 읽어
                                 // 다음 요청의 헤더에 추가할 수 있도록 돕는 필수적인 과정입니다.
                                 // 예를 들어, SPA(Single Page Application)나 모바일 앱과 같이
                                 // 서버로부터 CSRF 토큰을 쿠키로 직접 받아야 하는 시나리오에서 중요합니다.
        
                                 BasicAuthenticationFilter.class // 두 번째 인자: 'CsrfCookieFilter'가 이  BasicAuthenticationFilter 뒤에 위치하도록 지정.
                                 // 'BasicAuthenticationFilter'는 HTTP Basic 인증을 처리하는 Spring Security의 기본 필터입니다.
                                 // 즉, 기본적인 사용자 인증 절차가 완료된 '후에'
                                 // CSRF 토큰을 쿠키에 삽입하는 작업을 수행하도록 순서를 지정하는 것입니다.
                                 // 이는 논리적인 흐름을 따르며, 인증 관련 필터가 먼저 작동하여
                                 // 사용자의 신원이 확립된 후에 CSRF 토큰을 관리하는 것이 일반적입니다.
                 )



//권한 (Authorization) 관련 설정
//- 인증된 사용자가 특정 리소스에 접근할 수 있는지 여부를 결정하는 부분입니다.
//- HttpSecurity객체의 authorizeHttpRequests()메소드를 호출해서 HTTP요청 URL에 대한 권한 (Authorization) 관련 설정을 구성합니다.
/*
## 코드 용어 설명
    requestMatchers("패턴"): 특정 URL 패턴에 대한 접근을 제어합니다.

    "**": 해당 경로 하위의 모든 경로를 포함합니다. 예: /css/**는 /css/style.css, /css/sub/page.css 등 포함

    permitAll(): 누구나 접근 가능 (로그인 필요 없음)

    hasRole("ADMIN"): "ADMIN" 권한이 있는 사용자만 접근 가능

    authenticated(): 로그인(인증)이 된 사용자만 접근 가능

*/
                // 요청 URL별 접근 권한 설정
                // authorizeHttpRequests: HTTP 요청(URL 경로)에 대해 보안(접근 권한)을 설정하는 메서드
                .authorizeHttpRequests(authorizeHttpRequestsCustomizer -> authorizeHttpRequestsCustomizer

                                                                                                        // [1] 정적 리소스에 대한 접근 권한 설정
                                                                                                        // 웹 페이지에서 사용하는 정적인 파일들(css, js, 이미지 등)은
                                                                                                        // 로그인하지 않은 사람도 접근할 수 있도록 허용합니다.
                                                                                                        // 예) 로그인 화면의 디자인을 위해 CSS 파일이 필요하므로, 로그인 전에도 볼 수 있어야 합니다.
                                                                                                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                                                                                                        // [2] 주요 공개 페이지들에 대한 접근 권한 설정
                                                                                                        // 아래 경로들은 사용자 인증 없이 누구나 접속할 수 있도록 허용합니다.
                                                                                                        //
                                                                                                        // "/" : 홈페이지. 첫 화면 (예: 메인 배너, 소개 페이지 등)
                                                                                                        // "/members/**" : 회원 관련 페이지들.
                                                                                                        //                 예: /members/login (로그인 페이지), /members/new (회원가입)
                                                                                                        // "/item/**" : 상품 관련 페이지들.
                                                                                                        //              예: /item/1 (상품 상세 보기), /item/search (검색)
                                                                                                        // "/images/**" : 서버에 저장된 상품 이미지들 (이미지 URL 접근용)
                                                                                                        // "/thymeleaf/**" : thymeleaf 템플릿 테스트나 예제 페이지 (개발 시 사용)
                                                                                                        .requestMatchers("/", "/members/**", "/item/**", "/images/**", "/thymeleaf/**").permitAll()

                                                                                                        // [3] 관리자(admin)만 접근 가능한 URL 설정
                                                                                                        // "/admin/**" : 관리자만 사용하는 기능에 대한 URL.
                                                                                                        //               예: /admin/item/new (상품 등록), /admin/users (회원 관리)
                                                                                                        // 이 URL은 로그인한 사용자 중에서도 "ADMIN 권한"이 있어야 접근할 수 있습니다.
                                                                                                        .requestMatchers("/admin/**").hasRole("ADMIN")

                                                                                                        // [4] 위에서 설정한 URL 이외의 모든 요청 처리
                                                                                                        // 나머지 모든 URL 요청은 "로그인한 사용자만 접근 가능"하도록 설정합니다.
                                                                                                        // 로그인하지 않은 상태에서 접근하면 로그인 페이지로 리다이렉트됩니다.
                                                                                                        .anyRequest().authenticated())

//인증 (Authentication) 관련 설정
        //설명: 사용자가 ID와 비밀번호를 입력하여 로그인하는 폼 기반 인증 절차를 설정합니다. 로그인 페이지, 성공/실패 시 동작 등을 정의하여 사용자 인증 과정을 제어합니다.
                // 로그인 관련 설정
                // '.formLogin()' 메서드는 Spring Security에서 폼 기반(HTML 폼을 통한) 인증을 구성하는 데 사용됩니다.
                // 이 메서드의 람다 표현식(formLoginCustomizer -> ...)을 통해 로그인 프로세스의 다양한 측면을 세밀하게 제어할 수 있습니다.
                 .formLogin(formLoginCustomizer -> formLoginCustomizer
                                                                                 // '.loginPage("/members/login")'은 사용자가 로그인을 시도할 때,
                                                                                 // Spring Security가 제공하는 기본 로그인 페이지 대신 우리가 직접 만든
                                                                                 // '/members/login' 경로의 HTML 페이지를 사용하도록 지정합니다.
                                                                                 // 이 페이지는 사용자에게 ID와 비밀번호를 입력받는 폼을 포함해야 합니다.
                                                                                 .loginPage("/members/login")
                                                                                 // '.defaultSuccessUrl("/", true)'는 사용자가 성공적으로 로그인했을 때
                                                                                 // 리다이렉트(재요청)될 기본 URL을 설정합니다.
                                                                                 // - "/"는 웹 애플리케이션의 루트 경로(홈페이지)를 의미합니다.
                                                                                 // - 'true'는 'alwaysUse' 매개변수로, 사용자가 로그인하기 전에 접근하려던 특정 URL이 있었더라도,
                                                                                 //   성공 시 무조건 지정된 '/'(홈페이지)로 이동시키겠다는 의미입니다.
                                                                                 //   (false로 설정하면, 로그인 전 접근하려던 페이지로 이동합니다.)
                                                                                 .defaultSuccessUrl("/", true)
                                                                                 // '.usernameParameter("email")'은 로그인 폼에서 사용자 ID(username)를 입력받는
                                                                                 // HTML `<input>` 태그의 `name` 속성 값을 지정합니다.
                                                                                 // Spring Security의 기본값은 'username'이지만, 여기서는 'email'로 변경하여
                                                                                 // 사용자가 이메일을 ID로 사용하도록 합니다.
                                                                                 // (예: <input type="text" name="email" ... />)
                                                                                 .usernameParameter("email")
                                                                                 // '.failureHandler(new FormLoginAuthenticationFailureHandler())'는
                                                                                 // 사용자의 로그인 시도가 실패했을 때 (예: ID 또는 비밀번호 불일치)
                                                                                 // 어떻게 처리할지 정의하는 커스텀 핸들러를 지정합니다.
                                                                                 // 'FormLoginAuthenticationFailureHandler'는 우리가 직접 구현한 클래스로,
                                                                                 // 로그인 실패 시 특정 메시지를 표시하거나 다른 페이지로 리다이렉트하는 등의
                                                                                 // 맞춤형 로직을 처리할 수 있습니다.
                                                                                 .failureHandler(new FormLoginAuthenticationFailureHandler())
                 )
//인증 (Authentication) 관련 설정
    //설명: 사용자의 인증 상태를 종료(로그아웃)하는 과정을 설정합니다. 이는 인증 과정의 역방향으로 볼 수 있습니다.
                // 로그아웃 관련 설정
                // '.logout()' 메서드는 Spring Security에서 사용자의 로그아웃 기능을 구성하는 데 사용됩니다.
                // 이 메서드의 람다 표현식(logoutCustomizer -> ...)을 통해 로그아웃 프로세스의 세부 사항을 정의할 수 있습니다.
                 .logout(logoutCustomizer -> logoutCustomizer
                                                                         // '.logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))'는
                                                                         // 사용자가 로그아웃을 요청할 때 어떤 URL 경로를 통해 요청할지 지정합니다.
                                                                         // Spring Security는 이 경로로 들어오는 요청을 자동으로 가로채서 로그아웃 처리를 시작합니다.
                                                                         // 예를 들어, 사용자가 '/members/logout' URL에 접근하면 Spring Security는
                                                                         // 현재 세션을 무효화하고 인증 정보를 삭제하는 등 로그아웃 절차를 수행합니다.
                                                                         .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                                                                         // '.logoutSuccessUrl("/")'는 사용자가 성공적으로 로그아웃한 후에
                                                                         // 리다이렉트될 URL 경로를 지정합니다.
                                                                         // 여기서는 로그아웃이 완료되면 웹 애플리케이션의 루트 경로인 '/' (홈페이지)로 이동하도록 설정합니다.
                                                                         // 이 설정은 사용자에게 로그아웃이 성공적으로 완료되었음을 시각적으로 알려주는 역할을 합니다.
                                                                         .logoutSuccessUrl("/")
                 )
//인증 (Authentication) 관련 설정
    //설명: 로그인하지 않은(인증되지 않은) 사용자가 보호된 자원에 접근하려 할 때, 어떤 방식으로 처리할지 정의합니다. 예를 들어 로그인 페이지로 리다이렉트하거나 오류 메시지를 반환합니다.
                // 인증되지 않은 사용자가 보호된 자원에 접근하려 할 때 처리할 예외 핸들러 설정
                // '.exceptionHandling()' 메서드는 Spring Security에서 발생하는 예외, 특히 '인증(Authentication)' 및 '인가(Authorization)' 실패와 관련된 예외를
                // 어떻게 처리할지 정의하는 데 사용됩니다. 람다 표현식(e -> ...)을 통해 세부적인 예외 처리 로직을 구성합니다.
                 .exceptionHandling(e -> e
                                                                             // '.authenticationEntryPoint(new CustomAuthenticationEntryPoint())'는
                                                                             // 사용자가 '인증되지 않은' 상태에서(즉, 로그인하지 않은 상태에서) 접근 권한이 필요한 보호된 자원(URL)에 접근을 시도할 때
                                                                             // 어떤 동작을 수행할지 지정하는 '인증 진입점(Authentication Entry Point)'을 설정합니다.
                                                                             // Spring Security의 기본 동작은 보통 로그인 페이지로 리다이렉트하는 것이지만,
                                                                             // 여기서는 'CustomAuthenticationEntryPoint'라는 우리가 직접 구현한 클래스를 사용하여
                                                                             // 인증 실패 시의 동작을 사용자 정의합니다.
                                                                             // 예를 들어, JSON 형태의 오류 메시지를 반환하거나, 특정 오류 페이지로 이동시키는 등의
                                                                             // 맞춤형 처리를 구현할 수 있습니다. 이는 REST API나 AJAX 요청 처리 시 특히 유용합니다.
                                                                             .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                 )

                    // 전체 시큐리티 설정을 마무리하고 SecurityFilterChain 객체로 반환
                    // '.build()' 메서드는 'HttpSecurity' 객체에 적용된 모든 보안 설정들을 종합하여
                    // 최종적으로 'SecurityFilterChain' 객체를 생성하고 반환합니다.
                    // 이 'SecurityFilterChain' 객체는 Spring Security가 웹 요청을 처리할 때 사용하는
                    // 실제 보안 필터 체인(일련의 필터들)의 인스턴스이며, 스프링 컨테이너에 빈으로 등록됩니다.
                    // '.build()'가 호출되면, 설정된 모든 규칙(예: URL 접근 권한, 로그인/로그아웃 방식, CSRF 보호 등)이
                    // 적용된 보안 필터 체인이 완성되어 웹 애플리케이션의 모든 HTTP 요청에 대해 활성화됩니다.
                 .build();
    }



    // --------------------------------------------------------------------
    // [인증 (Authentication) 관련 설정]
    // --------------------------------------------------------------------
    // 이 부분은 사용자가 애플리케이션에 로그인할 때,
    // 입력한 비밀번호를 안전하게 처리하고 검증하는 데 필요한 설정을 정의합니다.

    @Bean // @Bean 어노테이션은 이 메서드가 반환하는 객체(여기서는 PasswordEncoder)를
    // Spring 컨테이너의 '빈(Bean)'으로 등록하여, 애플리케이션 전반에서 재사용할 수 있게 합니다.
    public PasswordEncoder passwordEncoder() {
        // 'PasswordEncoder' 인터페이스는 비밀번호를 암호화하고, 암호화된 비밀번호를 검증하는 기능을 제공합니다.
        // Spring Security에서 인증(로그인) 과정을 처리할 때 반드시 필요합니다.

        return new BCryptPasswordEncoder(); // 'BCryptPasswordEncoder'는 PasswordEncoder 인터페이스의 구현체 중 하나입니다.
        // BCrypt는 다음과 같은 특징을 가진 강력한 단방향 해싱(암호화) 알고리즘입니다:
        // - 보안성이 매우 높고, 무차별 대입 공격(Brute-force attack)과
        //   레인보우 테이블 공격(Rainbow table attack)에 강합니다.
        // - 비밀번호마다 '솔트(Salt)'를 자동으로 추가하여 해싱하기 때문에,
        //   동일한 비밀번호라도 매번 다른 해시 값을 생성하여 보안을 더욱 강화합니다.
        // - 해싱 속도를 조절할 수 있어, 하드웨어 성능에 따라 해싱 복잡도를 조절할 수 있습니다.

        // 이 빈이 등록되면, Spring Security는 사용자가 입력한 비밀번호를
        // 데이터베이스에 저장된 암호화된 비밀번호와 안전하게 비교하여 인증을 수행하게 됩니다.
        // 이 과정에서 비밀번호 원문은 절대 노출되거나 직접 비교되지 않으며,
        // 항상 암호화된 형태로 비교되어 보안을 강화합니다.
    }
}
