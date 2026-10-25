package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    =====================================================================
    WebMvcConfig - 웹 MVC 관련 설정 클래스 (정적 리소스 경로 매핑)
    =====================================================================

    비유: 도서관의 "서가 안내도"와 같습니다.
    도서관에서 "소설 코너"라고 안내판이 붙어있으면 그 방향으로 가서 소설책을 찾듯이,
    웹 브라우저가 "/images/상품이미지.jpg"를 요청하면
    이 설정이 "실제 파일은 C:/shop/ 폴더에 있어!"라고 안내해줍니다.

    즉, URL 경로와 실제 파일 저장 위치를 연결(매핑)해주는 설정입니다.
 */

@Configuration // 이 클래스가 Spring의 설정 파일임을 표시합니다. XML 설정 파일을 대체하는 Java 기반 설정 방식입니다.
public class WebMvcConfig implements WebMvcConfigurer {
    // WebMvcConfigurer: Spring MVC의 기본 설정을 커스터마이징(변경)할 수 있게 해주는 인터페이스입니다.

    @Value("${uploadPath}")
    // @Value: application.properties 파일에서 "uploadPath" 값을 읽어와 이 변수에 주입(대입)합니다.
    // 예) application.properties에 uploadPath=file:///C:/shop/ 으로 설정되어 있으면
    //     uploadPath 변수에 "file:///C:/shop/" 문자열이 들어갑니다.
    String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // addResourceHandlers: 정적 리소스(이미지, CSS, JS 파일 등)의 위치를 설정하는 메서드입니다.

        registry.addResourceHandler("/images/**")
                // addResourceHandler("/images/**"):
                // 웹 브라우저에서 "/images/"로 시작하는 URL을 요청하면 이 핸들러가 처리합니다.
                // "**"는 하위 모든 경로를 의미합니다. (예: /images/item/abc.jpg)

                .addResourceLocations(uploadPath);
                // addResourceLocations(uploadPath):
                // 위에서 매핑된 URL의 실제 파일을 찾을 물리적 경로를 지정합니다.
                // 예) /images/item/abc.jpg 요청 → C:/shop/item/abc.jpg 파일을 찾아 반환
    }

}