package com.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
    =====================================================================
    ShopApplication - 쇼핑몰 애플리케이션의 시작점(진입점) 클래스
    =====================================================================

    비유: 자동차의 "시동 버튼"과 같습니다.
    자동차를 운전하려면 시동을 걸어야 하듯이,
    스프링 부트 애플리케이션도 이 클래스의 main 메서드를 실행해야 서버가 시작됩니다.

    이 클래스가 실행되면:
    1) 내장 웹 서버(Tomcat)가 자동으로 시작됩니다.
    2) com.shop 패키지 하위의 모든 클래스를 자동으로 스캔하여 Spring Bean으로 등록합니다.
    3) 데이터베이스 연결, 보안 설정 등 application.properties에 정의된 설정이 적용됩니다.
 */

@SpringBootApplication
// @SpringBootApplication은 3가지 어노테이션을 합친 것입니다:
//   - @SpringBootConfiguration : 이 클래스가 스프링 설정 클래스임을 표시
//   - @EnableAutoConfiguration : 스프링 부트가 필요한 설정을 자동으로 해줌 (DB, 웹 서버 등)
//   - @ComponentScan : com.shop 패키지 아래의 모든 컴포넌트(@Controller, @Service 등)를 자동으로 찾아 등록
public class ShopApplication {

	// main 메서드: Java 프로그램의 시작점. JVM(자바 가상머신)이 가장 먼저 이 메서드를 실행합니다.
	public static void main(String[] args) {
		// SpringApplication.run() : 스프링 부트 애플리케이션을 실행하는 핵심 메서드
		// ShopApplication.class를 넘겨서 "이 클래스를 기준으로 설정을 읽어라"고 알려줍니다.
		SpringApplication.run(ShopApplication.class, args);
	}

}
