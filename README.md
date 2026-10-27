# test-1
깃허브를 처음 사용하기 위한 연습하는 공간 입니다.

어떻고 저떻고~ 이 저장소를 소개하기 위한 내용입니다.

여기에 중요한 내용을 입력하세요.

마크다운 문법으로 이내용 작성했습니다.

## 쇼핑몰(shop) 팀 프로젝트
- 프로젝트: Spring Boot 3.3.2 + JPA + Thymeleaf + Spring Security, 빌드 도구는 **Maven → Gradle** 로 전환했습니다.
- 필요한 것: **JDK 25** (Amazon Corretto 25 권장). Gradle 은 `gradlew` 가 9.2.1 을 자동으로 받습니다.
- 실행: `./gradlew bootRun` (Windows: `gradlew.bat bootRun`), 주소 http://localhost:8081
- 테스트: `./gradlew test` (MySQL 8.0, `localhost:3306`, root/0000, DB `shop` 필요)
- 자동 검사: `.github/workflows/shop-ci.yml` (PR 과 main push 마다 `./gradlew test`)
- 처음부터 완성 자습서 (한 파일): [docs/shop-tutorial.html](docs/shop-tutorial.html)
  - start.spring.io 로 빈 프로젝트를 만들고, 요청·응답 흐름 순서(공통 → 회원 → 상품 → 메인 → 주문 → 장바구니 → 테스트)대로 파일 95개를 복사 버튼으로 만들어 완성합니다.
  - IntelliJ 설정, GitHub · GitHub Actions, 팀(팀장 1 · 팀원 3) 작업 순서, 문제 해결 표가 함께 들어 있습니다.
  - 예제 상품 사진: [docs/sample-images.zip](docs/sample-images.zip)
- 알려진 제한: Spring Boot 3.3.2 의 Gradle 플러그인은 Gradle 9 에서 `bootJar`(실행 jar 만들기)가 실패합니다. 컴파일 · 테스트 · 실행(`bootRun`)에는 영향이 없습니다. 배포용 jar 가 필요하면 Spring Boot 3.4 이상이 필요합니다.
- 강사 변경 이력(원본 README): [docs/shop-changelog.md](docs/shop-changelog.md)
