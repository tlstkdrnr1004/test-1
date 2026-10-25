package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

/**
 * ========================================================================
 * MemberControllerTest - 회원 로그인 컨트롤러 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 웹 브라우저에서 로그인 폼에 이메일과 비밀번호를 입력하고 "로그인" 버튼을 눌렀을 때,
 * 올바른 정보로 로그인하면 성공하고, 틀린 정보로 로그인하면 실패하는지를 확인합니다.
 *
 * 일상 비유:
 *   건물 출입구의 보안 시스템을 테스트하는 것과 같습니다.
 *   - 올바른 사원증을 대면 문이 열려야 하고 (로그인 성공 테스트)
 *   - 다른 사람의 사원증이나 잘못된 비밀번호를 대면 문이 안 열려야 합니다 (로그인 실패 테스트)
 *
 * [어노테이션 설명]
 * @SpringBootTest
 *   - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 *
 * @AutoConfigureMockMvc
 *   - MockMvc(가짜 웹 요청 도구)를 자동으로 설정해주는 어노테이션입니다.
 *   - 실제 웹 서버(톰캣)를 띄우지 않고도 HTTP 요청/응답을 테스트할 수 있게 해줍니다.
 *   - 비유: 실제 도로에 나가지 않고 운전 시뮬레이터로 운전 연습하는 것과 같습니다.
 *
 * @Transactional - 테스트 후 DB 변경사항을 자동으로 롤백합니다.
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class MemberControllerTest {

    @Autowired
    private MemberService memberService;  // 회원 관련 비즈니스 로직을 처리하는 서비스

    /**
     * MockMvc - 가짜 웹 요청 도구
     *
     * 실제 웹 브라우저 없이도 HTTP 요청(GET, POST 등)을 보내고
     * 응답 결과를 확인할 수 있는 테스트 도구입니다.
     *
     * 비유: 로봇 팔이 대신 키보드를 치고 마우스를 클릭해서
     *       웹사이트를 사용하는 것처럼, 코드로 웹 요청을 흉내냅니다.
     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;      // 비밀번호 암호화 도구

    /**
     * 테스트 전에 회원을 미리 등록하는 헬퍼 메서드입니다.
     *
     * 로그인을 테스트하려면 먼저 "가입된 회원"이 있어야 합니다.
     * 비유: 도어락 비밀번호를 테스트하려면, 먼저 비밀번호를 설정해둬야 하는 것과 같습니다.
     *
     * @param email    로그인에 사용할 이메일
     * @param password 로그인에 사용할 비밀번호
     * @return 저장된 회원 객체
     */
    public Member createMember(String email, String password){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail(email);
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword(password);
        Member member = Member.createMember(memberFormDto, passwordEncoder);
        return memberService.saveMember(member);
    }


    /**
     * 로그인 성공 테스트
     *
     * [테스트 시나리오]
     * 1. 이메일 "test@email.com", 비밀번호 "1234"로 회원을 미리 등록합니다.
     * 2. 같은 이메일과 비밀번호로 로그인을 시도합니다.
     * 3. 로그인이 성공했는지 (인증되었는지) 확인합니다.
     *
     * 비유:
     *   집 도어락에 올바른 비밀번호(1234)를 눌렀을 때 문이 열리는지 확인하는 것입니다.
     *
     * @Test - 이 메서드가 JUnit 테스트 대상임을 표시합니다.
     *   테스트 실행기가 이 어노테이션이 붙은 메서드만 자동으로 실행합니다.
     *
     * @DisplayName("로그인 성공 테스트")
     *   - 테스트 결과 화면에 표시될 이름을 지정합니다.
     *   - 이 어노테이션이 없으면 메서드명(loginSuccessTest)이 그대로 표시되어 읽기 어려울 수 있습니다.
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    public void loginSuccessTest() throws Exception {

        String email = "test@email.com";
        String password = "1234";

        // [준비] 테스트용 회원을 DB에 미리 등록
        this.createMember(email, password);

        // [실행 + 검증] 로그인 요청을 보내고, 인증 성공 여부를 확인
        mockMvc.perform(
                        // formLogin(): Spring Security 테스트에서 제공하는 로그인 폼 요청 생성 도구
                        formLogin()
                                // userParameter("email"): 로그인 폼의 아이디 입력란 이름이 "email"임을 지정
                                // (기본값은 "username"이지만, 이 프로젝트는 이메일을 아이디로 사용)
                                .userParameter("email")
                                // loginProcessingUrl: 로그인 처리를 담당하는 URL 경로
                                .loginProcessingUrl("/members/login")
                                // user(): 로그인할 때 입력하는 이메일(아이디) 값
                                .user(email)
                                // password(): 로그인할 때 입력하는 비밀번호 값
                                .password(password)
                )
                // andExpect: "이런 결과가 나와야 한다"라고 기대하는 조건을 검사
                // authenticated(): 로그인(인증)에 성공했는지 확인
                //   - 성공하면 테스트 통과
                //   - 실패하면 테스트 실패
                .andExpect(SecurityMockMvcResultMatchers.authenticated());
        /*
         *  전체 흐름 정리:
         *
         *  로그인 시도 (email + password 입력)
         *       |
         *  Spring Security가 로그인 처리
         *       |
         *  로그인 성공 여부 확인
         *    /            \
         *  YES             NO
         *   |               |
         *  authenticated()  authenticated()
         *  만족 -> 통과      실패 -> 테스트 실패
         */
    }

    /**
     * 로그인 실패 테스트
     *
     * [테스트 시나리오]
     * 1. 이메일 "test@email.com", 비밀번호 "1234"로 회원을 미리 등록합니다.
     * 2. 같은 이메일이지만 틀린 비밀번호("12345")로 로그인을 시도합니다.
     * 3. 로그인이 실패했는지 (인증되지 않았는지) 확인합니다.
     *
     * 비유:
     *   집 도어락에 틀린 비밀번호(12345)를 눌렀을 때 문이 열리지 않는 것을 확인하는 것입니다.
     *   보안이 잘 작동하려면, 틀린 비밀번호로는 절대 문이 열리면 안 됩니다.
     */
   @Test
   @DisplayName("로그인 실패 테스트")
   public void loginFailTest() throws Exception {

       String email = "test@email.com";
       String password = "1234";

       // [준비] 테스트용 회원을 DB에 미리 등록
       this.createMember(email, password);

       // [실행 + 검증] 틀린 비밀번호("12345")로 로그인 시도
       mockMvc.perform(
                       formLogin()
                               .userParameter("email")
                               .loginProcessingUrl("/members/login")
                               .user(email)
                               .password("12345") // 의도적으로 틀린 비밀번호
               )
               // unauthenticated(): 로그인(인증)에 실패했는지 확인
               //   - 인증 실패(로그인 거부)가 정상적으로 일어나면 테스트 통과
               //   - 만약 틀린 비밀번호인데도 로그인이 되면 테스트 실패 (보안 문제!)
               .andExpect(
                       SecurityMockMvcResultMatchers.unauthenticated()
               );
   }


}
