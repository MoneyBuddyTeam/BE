# MoneyBuddy Backend 📊

> 챌린지 기반 경제 상담 서비스의 백엔드 API 서버

## 🚀 프로젝트 개요
**MoneyBuddy**는 전문가와 실시간 상담을 통해 개인 맞춤형 경제 미션을 제공하고, 사용자의 성취를 추적하여 경제적 성장을 돕는 서비스입니다.

### 🛠️ 기술 스택
- **Backend**: Spring Boot 3.5, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0, Redis(채팅)
- **Authentication**: JWT, OAuth2 (Google, Naver)
- **Documentation**: Swagger UI, OpenAPI 3.0
- **Build Tool**: Gradle 8.x
- **Testing**: JUnit 5, MockMvc, H2 Database
- **Deployment**: Docker, Docker Compose

---

## 📋 주차별 개발 체크리스트

### 2주차 (5/28 - 5/31) - 기초 환경 구축 및 인증 시스템 ✅ 완료

#### 🏗️ 개발 환경 & 프로젝트 설정
- [x] Spring Boot 프로젝트 초기화
- [x] GitHub 레포지토리 설정 및 초대 수락
- [x] 패키지 구조 설계 (도메인 기반)
- [x] application.yml 환경 설정 완료
- [x] 데이터베이스 연결 설정 (MySQL + Docker)
- [x] 기본 의존성 추가 및 테스트

#### 👤 사용자 인증 시스템 (김도현)
- [x] User Entity 및 Repository 구현
- [x] 로그인 API 구현
- [x] 로그아웃 API 구현
- [x] JWT 토큰 관리
- [x] Spring Security 설정
- [x] 소셜 로그인 연동 (Google, Naver)

#### 🔧 공통 기능 (김도현)
- [x] 글로벌 예외 처리 핸들러
- [x] 공통 응답 구조
- [x] JPA Auditing 설정

### 3주차 (6/1 - 6/7) - 카테고리 및 전문가 시스템 ✅ 완료

#### 🏷️ 카테고리 관리 시스템 (김수민)
- [x] Category Entity 및 Repository 구현
- [x] 카테고리 전체 조회 API
- [x] 카테고리 타입별 조회 API
- [x] 카테고리 상세 조회 API
- [x] 카테고리 단위 테스트 작성

#### 👨‍💼 전문가 관리 시스템 (김수민)
- [x] Advisor Entity 및 Repository 구현
- [x] AdvisorTag Entity (전문가-카테고리 연관)
- [x] 전문가 목록 조회 API (필터링/검색/정렬/페이징)
- [x] **전문가 상세 정보 API** 🆕
- [x] **추천 전문가 기능** 🆕
- [x] **사용자 ID 기반 전문가 조회 API** 🆕
- [x] 전문가 온라인 상태 관리 API
- [x] 전문가 상담 가능 여부 관리 API

#### 📖 API 문서화 (김수민)
- [x] Swagger 설정 및 API 문서 작성
- [x] OpenAPI 3.0 기반 상세 명세서
- [x] API 사용 가이드 문서화

---

## 📊 이번 주 진행률
```md
전체 진행률: ▓▓▓▓▓▓▓▓░░ 85% (17/20 completed)

개발 환경 설정: ▓▓▓▓▓▓▓▓▓▓ 100% (6/6)
사용자 인증: ▓▓▓▓▓▓▓▓▓▓ 100% (6/6) ✅
공통 기능: ▓▓▓▓▓▓▓▓▓▓ 100% (3/3) ✅
카테고리 관리: ▓▓▓▓▓▓▓▓▓▓ 100% (4/4) ✅
전문가 관리: ▓▓▓▓▓▓▓▓░░ 83% (5/6) 🆕
문서화: ▓▓▓▓▓▓▓▓▓▓ 100% (3/3) ✅
```

### 🎯 이번 주 주요 성과 (6/10-6/13)
- ✅ **전문가 상세 정보 API 완성**: 전문가 기본 정보 + 카테고리 + 추천 전문가
- ✅ **추천 알고리즘 구현**: 동일 카테고리 기반 최대 4명 추천
- ✅ **고급 검색 기능**: 키워드, 카테고리, 가격대, 온라인 상태 필터링
- ✅ **완전한 API 문서화**: Swagger UI 기반 상세 명세서

---

## 🎯 주요 API 엔드포인트

### 🏷️ 카테고리 API
```http
GET /api/v1/categories              # 전체 카테고리 조회
GET /api/v1/categories/type/{type}  # 타입별 카테고리 조회  
GET /api/v1/categories/{id}         # 카테고리 상세 조회
```

### 👨‍💼 전문가 API
```http
GET /api/v1/advisors                     # 전문가 목록 조회 (필터링/검색/정렬)
GET /api/v1/advisors/{advisorId}         # 전문가 상세 조회 + 추천 전문가 🆕
GET /api/v1/advisors/user/{userId}       # 사용자 ID로 전문가 조회 🆕
GET /api/v1/advisors/exists/user/{userId} # 전문가 존재 여부 확인 🆕
PUT /api/v1/advisors/{id}/online-status  # 온라인 상태 업데이트
PUT /api/v1/advisors/{id}/availability   # 상담 가능 여부 업데이트
```

### 🔍 카테고리 타입 정의
| 타입 | 코드 | 설명 |
|------|------|------|
| 소비관리 | `SPENDING` | 가계부, 소비 패턴 분석 |
| 저축계획 | `SAVINGS` | 적금, 예금 상품 추천 |
| 투자전략 | `INVESTMENT` | 주식, 펀드, 부동산 투자 |
| 부채관리 | `DEBT` | 대출 상환, 신용 관리 |
| 기타상담 | `ETC` | 보험, 세금, 기타 재정 |

### 📝 전문가 상세 API 응답 예시 🆕
```json
{
  "id": 1,
  "userId": 101,
  "name": "김경제",
  "bio": "10년 경력의 투자 전문가입니다. 주식, 부동산 투자 전문...",
  "certificationFile": "investment_cert.pdf",
  "price": 45000,
  "isOnline": true,
  "available": true,
  "createdAt": "2025-05-20T10:00:00",
  "categories": ["투자전략", "저축계획"],
  "recommendedAdvisors": [
    {
      "id": 2,
      "name": "박투자",
      "bio": "주식 투자 전문가로 5년간 개인 투자자들에게...",
      "price": 40000,
      "isOnline": false
    },
    {
      "id": 3,
      "name": "이재테크",
      "bio": "부동산 투자 및 재테크 전문가입니다...",
      "price": 50000,
      "isOnline": true
    }
  ]
}
```

---

## 👥 팀원 및 역할
|이름|역할|담당 영역|진행 상황|
|-|-|-|-|
|김도현|Backend Developer|사용자 인증, 보안, 예외처리|✅ 완료|
|김수민|Backend Developer|전문가 관리, API 문서화, 카테고리|✅ 4단계 완료|

---

## 📝 개발 규칙
- **브랜치 전략**: Git Flow 기반
  - `main`: 배포 브랜치
  - `develop`: 개발 통합 브랜치
  - `feature/기능명`: 기능 개발 브랜치 (완료 후 새 브랜치 생성)
- **커밋 컨벤션**: `타입: 제목` (예: `feat: 전문가 상세 API 구현`)
- **코드 컨벤션**: [Google Java Style Guide](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
- **코드 리뷰**: PR 생성 시 상대방 리뷰 필수
- **단위 테스트**: PR 올릴 때 필수 (커버리지 80% 이상)
- **PR 규칙**: 최소 기능 단위, 1000줄 이하, 상세한 설명 필수

---

## 🔗 관련 링크
- [팀 노션](https://confusion-sprint-93a.notion.site/1fbee1fb4f78800db3bafc6a92b26a0e?v=1fbee1fb4f7880648d29000c84e02e78&pvs=4)
- [API 문서 (Swagger)](http://localhost:8080/swagger-ui/index.html)
- [ERD 설계서](https://dbdiagram.io/d/MoneyBuddy_ERD-6831bb93b9f7446da3f7d230)

---

## 📅 다음 주 계획 (6/17-6/21)
- 🗨️ **실시간 채팅 시스템 구현**
  - WebSocket 설정 및 채팅방 관리
  - 메시지 저장 및 실시간 알림
- 🏆 **미션 시스템 기초 구조**
  - Mission Entity 설계
  - 전문가 미션 생성 API
- 📊 **관리자 대시보드 API**
  - 전문가 승인 시스템
  - 통계 조회 API

---

**Last Updated**: 2025-06-13
