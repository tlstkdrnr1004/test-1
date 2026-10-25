package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shop.entity.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    // @GetMapping(value = "/new") 어노테이션은 HTTP GET 요청 중 "/new" 경로로 들어오는 요청을
    // 이 메서드가 처리하도록 매핑합니다. 이는 주로 웹 브라우저에서 URL을 직접 입력하거나
    // 링크를 클릭하여 회원가입 페이지로 이동할 때 사용됩니다.
    @GetMapping(value = "/new")
    public String memberForm(Model model){ // 'Model' 객체는 컨트롤러에서 뷰(HTML 템플릿)로 데이터를 전달하는 데 사용됩니다.

        // 'model.addAttribute("memberFormDto", new MemberFormDto())' 코드는:
        // 1. "memberFormDto"라는 이름으로 새로운 'MemberFormDto' 객체를 생성하여 Model에 추가합니다.
        // 2. 이 빈(empty) 'MemberFormDto' 객체는 회원가입 폼(HTML)에서 입력 필드들을 바인딩(연결)하는 데 사용됩니다.
        //    사용자가 폼을 처음 로드할 때, 입력 필드가 비어있는 상태로 표시될 수 있도록 돕습니다.
        //    만약 이 객체가 없다면, Thymeleaf와 같은 템플릿 엔진은 폼 필드에 접근할 때 오류를 발생시킬 수 있습니다.
        model.addAttribute("memberFormDto", new MemberFormDto());

        // "member/memberForm"은 반환될 뷰(View)의 논리적인 이름입니다.
        // Spring MVC는 이 이름을 기반으로 'src/main/resources/templates/member/memberForm.html'과 같은
        // 실제 HTML 템플릿 파일을 찾아 클라이언트(브라우저)에게 렌더링하여 반환합니다.
        // 즉, 이 메서드는 회원가입 폼을 보여주는 HTML 페이지로 이동시키는 역할을 합니다.
        return "member/memberForm";
    }

    // @PostMapping(value = "/new") 어노테이션은 HTTP POST 요청 중 "/new" 경로로 들어오는 요청을
    // 이 메서드가 처리하도록 매핑합니다. 주로 HTML 폼 제출(회원가입 폼 제출) 시 사용됩니다.
    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, // 1. @Valid: 'memberFormDto' 객체의 유효성을 검증하도록 지시합니다.
                            //    MemberFormDto 클래스에 정의된 유효성 검사 규칙(예: @NotBlank, @Email 등)에 따라 데이터가 올바른지 확인합니다.
                            BindingResult bindingResult,        // 2. 'BindingResult': @Valid 어노테이션에 의한 유효성 검사 결과가 여기에 담깁니다.
                            //    폼 입력 데이터와 DTO 객체 간의 바인딩(매핑) 오류나 유효성 검사 오류가 발생하면,
                            //    그 오류 정보들이 이 객체에 저장됩니다.
                            Model model){                       // 3. 'Model': 뷰(HTML 템플릿)로 데이터를 전달하는 데 사용되는 Spring UI 모델 객체입니다.
        //    컨트롤러에서 처리된 결과를 뷰에 표시할 때 여기에 데이터를 담아 전달합니다.

        // 유효성 검사 오류 확인:
        // 'bindingResult.hasErrors()'는 유효성 검사(@Valid) 과정에서 하나 이상의 오류가 발생했는지 확인합니다.
        // 예를 들어, 필수 입력 필드가 비어있거나, 이메일 형식이 잘못되었을 경우 true를 반환합니다.
        if(bindingResult.hasErrors()){
            // 오류가 있을 경우:
            // 'member/memberForm' 뷰(HTML 파일)의 경로를 반환합니다.
            // 이 경우, 사용자는 다시 회원가입 폼 페이지로 돌아가며, BindingResult에 담긴 오류 정보가
            // 폼 필드 옆에 표시되어 어떤 입력이 잘못되었는지 알려줄 수 있습니다 (Thymeleaf 등을 통해).
            return "member/memberForm";
        }

        try {
            // 회원 생성 및 저장:
            // 1. 'Member.createMember(memberFormDto, passwordEncoder)':
            //    MemberFormDto(사용자 입력 데이터)와 passwordEncoder(비밀번호 암호화 도구)를 사용하여
            //    'Member' 엔티티 객체를 생성합니다. 이 과정에서 비밀번호는 안전하게 암호화됩니다.
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            // 2. 'memberService.saveMember(member)':
            //    생성된 Member 객체를 데이터베이스에 저장하기 위해 'memberService'의 'saveMember' 메서드를 호출합니다.
            //    이 메서드 내에서는 주로 중복 회원 검사 등의 비즈니스 로직이 수행될 수 있습니다.
            memberService.saveMember(member);
        } catch (IllegalStateException e){
            // 예외(오류) 처리:
            // 'IllegalStateException'은 일반적으로 예상치 못한 비즈니스 로직 오류(예: 중복 이메일 가입 시도)가 발생했을 때
            // 던져지는 예외입니다.
            // 'model.addAttribute("errorMessage", e.getMessage())':
            //    발생한 예외 메시지를 'errorMessage'라는 이름으로 Model에 추가하여 뷰(HTML)로 전달합니다.
            //    이를 통해 사용자에게 "이미 가입된 이메일입니다"와 같은 오류 메시지를 표시할 수 있습니다.
            model.addAttribute("errorMessage", e.getMessage());
            // 오류 메시지를 포함하여 다시 회원가입 폼 페이지를 반환합니다.
            return "member/memberForm";
        }

        // 성공적인 회원가입 처리:
        // 모든 유효성 검사와 비즈니스 로직이 성공적으로 처리되면,
        // 'redirect:/'를 반환하여 사용자를 웹 애플리케이션의 루트 경로(홈페이지)로 리다이렉트시킵니다.
        // 'redirect:' 접두사는 클라이언트(브라우저)에게 지정된 URL로 새로운 요청을 보내도록 지시합니다.
        return "redirect:/";
    }

    // 로그인 페이지 요청 처리:
    // 사용자가 "/members/login" URL로 접속하면 로그인 폼(HTML 페이지)을 보여줍니다.
    // 비유: 도서관 출입구에서 회원증을 보여달라는 안내판을 띄워주는 것과 같습니다.
    @GetMapping(value = "/login")
    public String loginMember(){
        return "/member/memberLoginForm";
    }

    // 로그인 실패 시 처리:
    // Spring Security가 로그인 실패를 감지하면 "/members/login/error" URL로 리다이렉트합니다.
    // 이 메서드는 오류 메시지를 Model에 담아 로그인 페이지에 다시 전달하여,
    // 사용자에게 "아이디 또는 비밀번호를 확인해주세요"라는 안내를 표시합니다.
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }

}