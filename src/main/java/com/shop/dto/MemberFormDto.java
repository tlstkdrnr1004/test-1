package com.shop.dto; // 이 클래스가 속한 패키지를 선언합니다.

import lombok.Getter; // Lombok 라이브러리에서 제공하는 어노테이션으로, 클래스의 모든 필드에 대한 Getter 메서드를 자동으로 생성합니다.
import lombok.Setter; // Lombok 라이브러리에서 제공하는 어노테이션으로, 클래스의 모든 필드에 대한 Setter 메서드를 자동으로 생성합니다.
import org.hibernate.validator.constraints.Length; // Hibernate Validator에서 제공하는 어노테이션으로, 문자열 길이에 대한 유효성을 검사합니다.

import jakarta.validation.constraints.Email; // Jakarta Validation API에서 제공하는 어노테이션으로, 이메일 형식의 유효성을 검사합니다.
import jakarta.validation.constraints.NotBlank; // Jakarta Validation API에서 제공하는 어노테이션으로, 문자열이 null이 아니고, 비어 있지 않으며(empty), 공백 문자만으로 이루어져 있지 않은지 검사합니다.
import jakarta.validation.constraints.NotEmpty; // Jakarta Validation API에서 제공하는 어노테이션으로, 문자열이나 컬렉션이 null이 아니고 비어 있지 않은지 검사합니다. (공백만 있는 경우는 허용)

// MemberFormDto 클래스는 웹사이트 회원가입 폼에서 사용자가 입력한 데이터를 서버로 전달하고, 그 데이터를 유효성 검사하는 데 사용되는 Data Transfer Object (DTO)입니다.
// @Getter와 @Setter 어노테이션 덕분에 각 필드에 대한 Getter와 Setter 메서드가 자동으로 생성되어 편리하게 데이터를 설정하고 가져올 수 있습니다.
@Getter @Setter
public class MemberFormDto {

    // @NotBlank(message = "이름은 필수 입력 값입니다."): 'name' 필드가 반드시 입력되어야 하며 (null이 아니고), 비어있지 않아야 하고, 공백 문자만으로 이루어져 있지 않아야 함을 나타냅니다.
    // 만약 이 조건을 만족하지 못하면, 'message' 속성에 정의된 "이름은 필수 입력 값입니다."라는 오류 메시지가 사용자에게 표시됩니다.
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    // @NotEmpty(message = "이메일은 필수 입력 값입니다."): 'email' 필드가 반드시 입력되어야 하며 (null이 아니고), 비어있지 않아야 함을 나타냅니다.
    // @Email(message = "이메일 형식으로 입력해주세요."): 'email' 필드의 값이 유효한 이메일 주소 형식을 따라야 함을 나타냅니다.
    // 만약 이메일 형식이 올바르지 않으면, "이메일 형식으로 입력해주세요."라는 오류 메시지가 표시됩니다.
    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    // @NotEmpty(message = "비밀번호는 필수 입력 값입니다."): 'password' 필드가 반드시 입력되어야 하며 (null이 아니고), 비어있지 않아야 함을 나타냅니다.
    // @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요"): 'password' 필드의 문자열 길이가 최소 8자 이상이고, 최대 16자 이하여야 함을 나타냅니다.
    // 만약 길이가 이 범위를 벗어나면, "비밀번호는 8자 이상, 16자 이하로 입력해주세요"라는 오류 메시지가 표시됩니다.
    @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
    @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String password;

    // @NotEmpty(message = "주소는 필수 입력 값입니다."): 'address' 필드가 반드시 입력되어야 하며 (null이 아니고), 비어있지 않아야 함을 나타냅니다.
    // 만약 이 조건을 만족하지 못하면, "주소는 필수 입력 값입니다."라는 오류 메시지가 표시됩니다.
    @NotEmpty(message = "주소는 필수 입력 값입니다.")
    private String address;
}