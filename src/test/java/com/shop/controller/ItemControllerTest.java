package com.shop.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * ========================================================================
 * ItemControllerTest - 상품 컨트롤러 권한 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 상품 등록 페이지("/admin/item/new")에 대한 접근 권한이 올바르게 설정되어 있는지 확인합니다.
 *   - 관리자(ADMIN)는 접근 가능해야 합니다.
 *   - 일반 회원(USER)은 접근이 거부되어야 합니다.
 *
 * 일상 비유:
 *   회사 건물에서 "서버실"은 IT 관리자만 들어갈 수 있고,
 *   일반 직원은 출입이 금지된 구역입니다.
 *   이 테스트는 출입 통제 시스템이 제대로 작동하는지 확인하는 것과 같습니다.
 *   - 관리자 카드를 대면 문이 열려야 하고 (테스트 1)
 *   - 일반 직원 카드를 대면 "출입 금지" 알림이 나와야 합니다 (테스트 2)
 *
 * [어노테이션 설명]
 * @SpringBootTest - 전체 스프링 애플리케이션 컨텍스트를 로드하여 테스트합니다.
 *   모든 설정과 빈(Bean)이 실제 환경과 동일하게 구성됩니다.
 *
 * @AutoConfigureMockMvc - MockMvc를 자동으로 설정해줍니다.
 *   실제 웹 서버(톰캣)를 띄우지 않고도 HTTP 요청을 보내고 응답을 확인할 수 있습니다.
 *
 * @TestPropertySource - 테스트 전용 설정 파일(application-test.properties)을 불러옵니다.
 *   실제 운영 환경과 분리된 별도의 DB, 포트 등을 사용할 수 있습니다.
 * ========================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations="classpath:application-test.properties")
class ItemControllerTest {

    /**
     * MockMvc - 가짜 웹 요청 도구
     *
     * 실제 웹 서버를 띄우지 않고 HTTP 요청(GET, POST 등)과 응답을 테스트할 수 있는 도구입니다.
     * 비유: 비행기 조종사가 실제 비행 전에 "비행 시뮬레이터"로 연습하듯,
     *       실제 서버 없이 웹 요청을 시뮬레이션합니다.
     *
     * @Autowired - 스프링이 자동으로 MockMvc 객체를 생성하여 주입해줍니다.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * [테스트 1] 관리자 권한으로 상품 등록 페이지 접근 가능 여부 테스트
     *
     * [테스트 시나리오]
     * 1. 관리자(ADMIN) 권한을 가진 사용자로 로그인한 상태를 설정합니다.
     * 2. 상품 등록 페이지("/admin/item/new")로 GET 요청을 보냅니다.
     * 3. 응답 상태 코드가 200(OK, 정상)인지 확인합니다.
     *
     * 비유: 관리자 카드를 가지고 서버실 문을 열었을 때, 정상적으로 문이 열리는지 확인합니다.
     *
     * @Test - JUnit에게 이 메서드가 테스트 대상임을 알립니다.
     * @DisplayName - 테스트 결과에 "상품 등록 페이지 권한 테스트"라고 표시됩니다.
     * @WithMockUser(username = "admin", roles = "ADMIN")
     *   - 테스트 실행 시, "admin"이라는 이름의 ADMIN 권한 가짜 사용자가
     *     로그인한 것으로 설정합니다.
     *   - 실제로 로그인하지 않아도 Spring Security가 "이 사용자는 관리자"라고 인식합니다.
     *   - 비유: 테스트를 위해 임시로 "관리자 명찰"을 달고 있는 상태입니다.
     */
    @Test
    @DisplayName("상품 등록 페이지 권한 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void itemFormTest() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                // MockMvcRequestBuilders.get(): GET 방식으로 해당 URL에 요청을 보냅니다.
                .andDo(print())
                // andDo(print()): 요청과 응답의 상세 내용을 콘솔에 출력합니다. (디버깅용)
                .andExpect(status().isOk());
                // status().isOk(): HTTP 응답 코드가 200(성공)인지 확인합니다.
                // 관리자이므로 상품 등록 페이지에 정상 접근이 가능해야 합니다.
    }

    /**
     * [테스트 2] 일반 사용자 권한으로 상품 등록 페이지 접근 불가 여부 테스트
     *
     * [테스트 시나리오]
     * 1. 일반 사용자(USER) 권한으로 로그인한 상태를 설정합니다.
     * 2. 상품 등록 페이지("/admin/item/new")로 GET 요청을 보냅니다.
     * 3. 응답 상태 코드가 403(Forbidden, 접근 거부)인지 확인합니다.
     *
     * 비유: 일반 직원 카드를 가지고 서버실 문을 열려고 했을 때,
     *       "출입 금지" 알림이 나오는 것을 확인합니다.
     *
     * @WithMockUser(username = "user", roles = "USER")
     *   - 이번에는 "user"라는 이름의 일반 사용자(USER 권한)가 로그인한 상태입니다.
     *   - 일반 사용자는 관리자 전용 페이지에 접근할 수 없어야 합니다.
     */
    @Test
    @DisplayName("상품 등록 페이지 일반 회원 접근 테스트")
    @WithMockUser(username = "user", roles = "USER")
    public void itemFormNotAdminTest() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                .andDo(print())
                .andExpect(status().isForbidden());
                // status().isForbidden(): HTTP 응답 코드가 403(Forbidden, 접근 거부)인지 확인합니다.
                // 일반 사용자이므로 관리자 전용 페이지 접근이 거부되어야 합니다.
                // 403은 "너는 권한이 없어서 이 페이지를 볼 수 없다"는 의미입니다.
    }
}
