# test-1
깃허브를 처음 사용하기 위한 연습하는 공간 입니다.

어떻고 저떻고~ 이 저장소를 소개하기 위한 내용입니다.

여기에 중요한 내용을 입력하세요.

마크다운 문법으로 이내용 작성했습니다.

## 쇼핑몰(shop) 팀 프로젝트
- 프로젝트: Spring Boot 3.3.2 + JPA + Thymeleaf + Spring Security, 빌드 도구는 **Maven → Gradle** 로 전환했습니다.
- 실행: `./gradlew bootRun` (Windows: `gradlew.bat bootRun`), 주소 http://localhost:8081
- 테스트: `./gradlew test` (MySQL 8.0, `localhost:3306`, root/0000, DB `shop` 필요)
- 자동 검사: `.github/workflows/shop-ci.yml` (PR 과 main push 마다 `./gradlew test`)
- 단계별 자습서: [docs/shop-tutorial.html](docs/shop-tutorial.html) (IntelliJ, Git, GitHub, GitHub Actions 팀 프로젝트)
- 강사 변경 이력(원본 README): [docs/shop-changelog.md](docs/shop-changelog.md)
