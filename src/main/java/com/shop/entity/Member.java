package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.*;


// 회원 정보를 저장하는 Member 엔티티 클래스 작성
// 비유: Member 클래스는 "회원 카드 양식"과 같습니다.
//       회원 카드에 이름, 이메일, 비밀번호, 주소, 등급(역할)이 적혀 있듯이,
//       이 클래스도 회원 한 명의 정보를 담는 틀이며, 데이터베이스의 member 테이블 한 행(row)에 대응합니다.

// @Entity : 이 클래스가 JPA가 관리하는 엔티티(= DB 테이블과 연결되는 자바 객체)임을 선언합니다.
@Entity
// @Table(name="member") : 이 엔티티가 데이터베이스의 "member"라는 이름의 테이블과 매핑(연결)됨을 지정합니다.
@Table(name="member")
// @Getter @Setter : Lombok이 모든 필드의 getter/setter 메서드를 자동 생성합니다.
@Getter @Setter
// @ToString : Lombok이 toString() 메서드를 자동 생성하여 객체 내용을 문자열로 확인할 수 있게 합니다.
@ToString
public class Member extends BaseEntity {

    // @Id : 이 필드가 테이블의 기본키(Primary Key)임을 나타냅니다.
    @Id
    // @Column(name="member_id") : 이 변수가 DB 테이블의 "member_id" 컬럼과 연결됩니다.
    @Column(name="member_id")
    // @GeneratedValue : 기본키 값을 자동으로 생성합니다. AUTO 전략은 JPA가 DBMS에 맞게 자동 선택합니다.
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    //회원은 이메일을 통해 유일하게 구문해야 하기때문에, 동일한 값이 데이터베이스에 들어올수 없도록
    //unique 제약 조건 설정
    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    //자바의 enum 타입의 Role클래스를 엔티티의 속성으로 지정할 수 있습니다.
    //Enum을 사용할때 기본적으로 순서가 저장되는데,
    //enum의 순서가 바뀔 경우 문제가 발생할 수 있으므로 "EnumType.STRING" 옵션을 사용해서
    //String으로 지정 하기를 권장 합니다.
    @Enumerated(EnumType.STRING)
    private Role role;

    // Member 엔티티를 생성하는 정적 팩토리 메소드입니다.
    // 비유: 회원가입 신청서(MemberFormDto)를 받아서, 실제 회원 카드(Member 엔티티)를 발급해주는 "발급 창구" 역할입니다.
    //       신청서에 적힌 이름, 이메일, 주소를 회원 카드에 옮겨 적고, 비밀번호는 금고에 넣듯 암호화해서 저장합니다.
    // Member 엔티티에 회원을 생성하는 메소드를 만들어서 관리한다면 코드가 변경되더라도 한군데만 수정하면 되는 이점이 있습니다.
    // MemberFormDto -> Member 엔티티로 변환해서 반환
    // static(정적) 메소드인 이유: new Member()로 객체를 먼저 만들지 않아도 Member.createMember(...)로 바로 호출할 수 있기 때문입니다.
    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        //스프링 시큐리티 설정 SecurityConfig 클래스에 등록한 BCyptPasswordCendoder 객체를 위 PasswordEncoder passwordEncoder매개변수로 받아서
        //비밀번호를 암호화 합니다.
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);// Member엔티티 생성시  USER Role로 생성하던 권한을 ADMIN Role로 생성하도록 설정
        return member;
    }

}
