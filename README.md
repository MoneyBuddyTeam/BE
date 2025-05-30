# MoneyBuddy Backend 📊

> 챌린지 기반 경제 상담 서비스의 백엔드 API 서버

## 🚀 프로젝트 개요
**MoneyBuddy**는 전문가와 실시간 상담을 통해 개인 맞춤형 경제 미션을 제공하고, 사용자의 성취를 추적하여 경제적 성장을 돕는 서비스입니다.

### 🛠️ 기술 스택
- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL, Redis(채팅)
- **Authentication**: JWT, OAuth2
- **Documentation**: Swagger, Postman
- **Build Tool**: Gradle
- **Deplyment**: 미정
---
## 📋 주차별 개발 체크리스트
### 2주차 (5/28 - 5/31) - 기초 환경 구축 및 인증 시스템
#### 🏗️ 개발 환경 & 프로젝트 설정
- [x] Spring Boot 프로젝트 초기화
- [x] GitHub 레포지토리 설정 및 초대 수락
- [x] 패키지 구조 설계 (도메인 기반)
- [x] application.yml 환경 설정 완료
- [x] 데이터베이스 연결 설정
- [x] 기본 의존성 추가 및 테스트

#### 👤 사용자 인증 시스템 (김도현)
- [x] User Entity 및 Repository 구현
- [x] 로그인 API 구현
- [ ] 로그아웃 API 구현
- [x] JWT 토큰 관리
- [x] Spring Security 설정

#### 🔧 공통 기능 (김도현)
- [x] 글로벌 예외 처리 핸들러
- [x] 공통 응답 구조

#### 👨‍💼 전문가 관리 시스템 (김수민)
- [ ] Advisor Entity 및 Repository 구현
- [ ] 전문가 목록 조회 API
- [ ] 전문가 상세 정보 API
- [ ] 전문가 태그 시스템

#### 📖 API 문서화 (김수민)
- [ ] Swagger 설정
- [ ] API 명세서 작성
---
## 📊 이번 주 진행률
```md
전체 진행률: ▓▓▓░░░░░░░ 32% (6/19 completed)

개발 환경 설정: ▓▓▓▓▓▓▓▓▓▓ 100% (6/6)
사용자 인증: ▓▓▓▓▓▓▓▓░░ 80% (4/5)
공통 기능: ▓▓▓▓▓▓▓▓▓ 100% (2/2)
전문가 관리: ░░░░░░░░░░ 0% (0/4)
문서화: ░░░░░░░░░░ 0% (0/2)
```
---
## 👥 팀원 및 역할
|이름|역할|담당 영역|
|-|-|-|
|김도현|Backend Developer|사용자 인증, 보안, 예외처리|
|김수민|Backend Developer|전문가 관리, API 문서화|
---
## 📝 개발 규칙
- **브랜치 전략**: Git Flow 기반
  - `main`: 배포 브랜치
  - `develop`: 개발 통합 브랜지
  - `feature/기능명`: 기능 개발 브랜치
- **커밋 컨벤션**: `타입: 제목` (예: `feat: 회원가입 API 구현`)
- **코드 컨벤션**: [Google Java Style Guide](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
- **코드 리뷰**: PR 생성 시 상대방 리뷰 필수
- **일일 체크인**: 매일 개발 진척도 슬랙 공유
---
## 🔗 관련 링크
- [팀 노션](https://confusion-sprint-93a.notion.site/1fbee1fb4f78800db3bafc6a92b26a0e?v=1fbee1fb4f7880648d29000c84e02e78&pvs=4)
- [API 문서 (Swagger)](링크 예정)
- [ERD 설계서](https://dbdiagram.io/d/MoneyBuddy_ERD-6831bb93b9f7446da3f7d230)
---
**Last Updated**: 2025-05-28
