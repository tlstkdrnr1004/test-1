package com.shop.service;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ========================================================================
 * MemberServiceTest - 회원 서비스 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 쇼핑몰의 회원가입 기능이 올바르게 동작하는지 확인합니다.
 * 구체적으로 두 가지를 검증합니다:
 *   1) 정상적인 회원가입이 성공하는지
 *   2) 이미 가입된 이메일로 다시 가입하려 할 때 올바르게 거부하는지
 *
 * 일상 비유:
 *   웹사이트에 회원가입할 때를 생각해보세요.
 *   - 처음 가입하면 정상적으로 완료되어야 하고 (테스트 1)
 *   - 같은 이메일로 또 가입하려 하면 "이미 가입된 이메일입니다"라는 메시지가 나와야 합니다 (테스트 2)
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 * @Transactional - 테스트 중 DB에 저장된 데이터를 테스트 종료 후 자동 롤백합니다.
 *   비유: 연습장에 연필로 써본 후 지우는 것처럼, 테스트 데이터가 실제 DB에 남지 않습니다.
 *   모든 테스트 메서드에서 DB 작업 중 에러가 발생하면 변경사항이 자동으로 취소됩니다.
 * @TestPropertySource - 테스트 전용 설정 파일(application-test.properties)을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class MemberServiceTest {

    @Autowired
    MemberService memberService;       // 회원 관련 비즈니스 로직을 처리하는 서비스

    @Autowired
    PasswordEncoder passwordEncoder;   // 비밀번호를 암호화하는 도구
    // 비유: 비밀번호를 금고에 넣기 전에 암호로 변환하는 장치

    /**
     * 테스트용 회원을 생성하는 헬퍼 메서드입니다.
     * 회원가입 화면에서 사용자가 입력하는 정보(이메일, 이름, 주소, 비밀번호)를
     * 미리 세팅한 Member 엔티티를 만들어 반환합니다.
     *
     * 비유: 회원가입 폼에 미리 정보를 채워넣은 상태의 신청서를 만드는 것입니다.
     *
     * @return 회원 정보가 채워진 Member 객체 (아직 DB에는 저장되지 않은 상태)
     */
    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }


    /**
     * 회원가입 성공 테스트
     *
     * [테스트 시나리오]
     * 1. 테스트용 회원 정보를 생성합니다.
     * 2. 회원가입 서비스를 호출하여 DB에 저장합니다.
     * 3. 가입 요청 시 입력한 정보와 실제 DB에 저장된 정보가 일치하는지 하나씩 비교합니다.
     *    (이메일, 이름, 주소, 비밀번호, 권한)
     *
     * 비유:
     *   택배를 보낼 때 운송장에 적은 정보(수신인, 주소, 연락처)가
     *   실제 택배 시스템에 정확히 등록되었는지 확인하는 것과 같습니다.
     *
     * @Test - 이 메서드가 JUnit 테스트 대상임을 표시합니다.
     * @DisplayName - 테스트 결과 화면에 "회원가입 테스트"라고 표시됩니다.
     *   비유: 시험지의 문제 제목과 같습니다. 어떤 테스트인지 쉽게 알 수 있습니다.
     */
    @Test
    @DisplayName("회원가입 테스트")
    public void saveMemberTest(){
        // [준비] 테스트용 회원 생성
        Member member = createMember();

        // [실행] 회원가입 서비스 호출 (DB에 저장)
        Member savedMember = memberService.saveMember(member);

        // [검증] assertEquals(기대값, 실제값)
        //   - 기대값: 가입 요청 시 입력한 값
        //   - 실제값: DB에서 가져온 값
        //   - 두 값이 같으면 테스트 통과, 다르면 실패
        assertEquals(member.getEmail(), savedMember.getEmail());       // 이메일 일치 확인
        assertEquals(member.getName(), savedMember.getName());         // 이름 일치 확인
        assertEquals(member.getAddress(), savedMember.getAddress());   // 주소 일치 확인
        assertEquals(member.getPassword(), savedMember.getPassword()); // 비밀번호 일치 확인
        assertEquals(member.getRole(), savedMember.getRole());         // 권한(역할) 일치 확인
    }

    /**
     * 중복 회원가입 방지 기능 테스트
     *
     * [테스트 시나리오]
     * 1. 같은 이메일(test@email.com)로 회원 2명을 만듭니다.
     * 2. 첫 번째 회원은 정상적으로 가입됩니다.
     * 3. 두 번째 회원이 같은 이메일로 가입하려 하면 에러(IllegalStateException)가 발생해야 합니다.
     * 4. 에러 메시지가 "이미 가입된 회원입니다."인지 확인합니다.
     *
     * 비유:
     *   같은 이메일로 네이버 계정을 두 번 만들려 하면
     *   "이미 사용 중인 이메일입니다"라는 메시지가 나오죠?
     *   이 테스트는 그 동작이 올바르게 작동하는지 확인합니다.
     *
     * assertThrows 설명:
     *   - "이 코드를 실행하면 이런 에러가 반드시 발생해야 한다"고 선언하는 검증 도구입니다.
     *   - 첫 번째 인자: 기대하는 예외(에러) 타입
     *   - 두 번째 인자: 실행할 코드 (람다식)
     *   - 비유: "이 길로 가면 반드시 통행금지 표지판이 나와야 한다"라고 확인하는 것
     */
    @Test
    @DisplayName("중복 회원 가입 테스트")
    public void saveDuplicateMemberTest(){
        // [준비] 같은 이메일로 회원 2명 생성
        Member member1 = createMember();
        Member member2 = createMember();

        // [실행 1] 첫 번째 회원은 정상 가입
        memberService.saveMember(member1);

        // [실행 2 + 검증] 두 번째 회원 가입 시 예외(에러)가 발생하는지 확인
        Throwable e = assertThrows(IllegalStateException.class, () -> {memberService.saveMember(member2);});

        // [검증] 발생한 에러 메시지가 예상한 메시지와 일치하는지 확인
        assertEquals("이미 가입된 회원입니다.", e.getMessage());
    }
}
