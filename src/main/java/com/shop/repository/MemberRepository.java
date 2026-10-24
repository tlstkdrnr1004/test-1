package com.shop.repository;

import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// Member 엔티티를 데이터베이스에 저장할 수 있도록 MemberRepository 리파지터리를 만듭니다.
// 비유: MemberRepository는 "회원 명부 관리 창구"와 같습니다.
//       회원 등록(save), 조회(find), 삭제(delete) 등의 업무를 처리하는 창구이며,
//       JpaRepository를 상속받으면 이러한 기본 CRUD 메소드를 직접 구현하지 않아도 자동으로 제공받습니다.
//       <Member, Long> : 관리 대상은 Member 엔티티이고, 기본키(PK) 타입은 Long입니다.
public interface MemberRepository extends JpaRepository<Member, Long> {

    //회원 가입시 중복된 회원이 있는지 검사하기 위해서 이메일로 회원을 조회해서 검사할수 있도록 쿼리 메소드 작성
    Member findByEmail(String email);

}