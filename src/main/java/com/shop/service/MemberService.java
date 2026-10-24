package com.shop.service;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/*
참고.
UserDetailsService 인터페이스
- UserDetailService 인터페이스는 데이터베이스에서 회원 정보를 가져오는 역할을 담당 합니다.
- loadUserByUsername() 메소드가 존재하며, 회원 정보를 조회한 사용자의 정보와 권한을 갖는 UserDetails 인터페이스를 반환합니다.
- 스프링 시큐리티에서 UserDetailService 인터페이스를 구현하고 있는 클래스를 통해 로그인 기능을 구현한다고 생각하면 됩니다.
참고.
UserDetails 인터페이스
- 스프링 시큐리티에서 회원의 정보를 담기 위해서 사용하는 인터페이스.
- 이 인터페이스를 직접 구현하거나 스프링 시큐리티에서 제공하는 User클래스를 사용합니다.
  User 클래스는 UserDetails인터페이스를 구현하고 있는 자식 클래스 입니다.

 */


@Service
//@Transactional 을 붙여 아래의 모든 메소드에서 회원관련 DB작업 비즈니스로직을 처리하다가 에러가 발생하면 변경된 DB의 데이터를 롤백(취소)
@Transactional
//@RequiredArgsConstructor 을 붙여 아래의 private final MemberRepository memberRepository; 의 코드에서
//final 이나 @NotNull이 붙은 변수에 생성자를 생성 해 서 의존 주입 해줍니다.
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;//<---- @RequiredArgsConstructor 로 생성된 생성자로 의존주입 자동으로 됨

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    //이미 가입된 회원의 경우   throw new IllegalStateException("이미 가입된 회원입니다."); 강제로 예외를 발생시키는 메소드
    private void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

   /*
  UserDetailsService인터페이스 내부에 만들어져 있는 아래의  loadUserByUsername메소드를 왜 구현해야 하나요?

    # Spring Security는 로그인 시 내부적으로 이렇게 동작합니다:

        순서1. 사용자가 로그인 폼에 ID/PW 입력 → Spring Security가 UserDetailsService의 loadUserByUsername() 호출

        순서2. 우리가 구현한 MemberService가 해당 email 사용자 정보(DB 등에서)를 조회함

        순서3. 조회한 정보로 UserDetails 구현체 User객체를 만들어서 반환

        순서4. Spring Security가 이 정보를 기반으로 인증 처리 진행

    loadUserByUsername(String username)은 Spring Security가 로그인할 때 loadUserByUsername 메소드를 자동으로 호출합니다.
    여기서 username은 이메일이 될 수도, 아이디가 될 수도 있지만, 우리가 이메일을 로그인 ID로 사용하기로 했기 때문에 email이라는 이름을 사용했습니다.
    반환하는 User 객체는 Spring Security가 내부적으로 사용하는 사용자 정보 객체입니다.
  */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                                        //로그인 요청한 클라이언트의 이메일을 매개변수로 받습니다

        // 1. 데이터베이스에서 이메일(email)로 회원(Member) 정보를 조회합니다.
        //    만약 사용자가 로그인할 때 입력한 이메일이 DB에 없다면 null이 반환됩니다.
        Member member = memberRepository.findByEmail(email);

        // 2. 위에서 찾은 회원 정보가 없으면 (= 이메일로 가입한 사용자가 없으면)
        //    Spring Security에서 요구하는 방식으로 예외를 던져 로그인 실패 처리합니다.
        if (member == null) {
            // UsernameNotFoundException: 사용자 정보가 없다는 예외
            throw new UsernameNotFoundException(email);
        }

        // 3. 회원 정보가 존재하는 경우, Spring Security가 인증을 처리할 수 있도록
        //    UserDetails인터페이스를 구현한 자식 User객체를  생성해서 반환합니다.
        //    여기서 User는 Spring Security에서 제공하는 기본 구현체입니다.
        return User.builder()
                // 3-1. username 설정 → 보통은 이메일을 사용합니다.
                .username(member.getEmail())

                // 3-2. password 설정 → DB에 저장된 암호화된 비밀번호
                .password(member.getPassword())

                // 3-3. 역할(Role) 설정 → ROLE_USER, ROLE_ADMIN 등 권한을 문자열로 지정
                //      toString()으로 문자열로 바꿔서 전달해야 합니다.
                .roles(member.getRole().toString())

                // 3-4. 최종적으로 UserDetails 구현체인 User 객체를 생성합니다.
                .build();
    }


}