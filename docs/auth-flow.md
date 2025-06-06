# Authentication 문서 (MoneyBuddy)

## 1. 전체 인증 흐름 설명

MoneyBuddy의 인증 시스템은 **JWT 기반 토큰 인증 방식**으로 작동하며, 주요 흐름은 다음과 같습니다:

1. **회원가입**

   * `POST /api/v1/users`
   * 사용자 정보(email, password 등)를 입력 → DB에 사용자 및 기본 설정(UserSettings) 저장

2. **로그인**

   * `POST /api/v1/users/login`
   * 이메일/비밀번호 입력 → 사용자 인증 → JWT 토큰 생성 후 응답으로 반환

3. **JWT 인증 처리**

   * 클라이언트는 이후 요청 시 `Authorization: Bearer {token}` 형식으로 JWT를 전송
   * `JwtAuthenticationFilter`가 토큰 유효성 검증 후 `SecurityContext`에 인증 객체 주입

4. **권한 기반 접근 제어**

   * 다음 요청은 인증 없이 허용 (permitAll):

     * `/api/v1/users`
     * `/api/v1/users/login`
     * `/swagger-ui/**`, `/v3/api-docs/**`
   * 그 외 모든 요청은 인증 필요 (authenticated)

5. **회원 탈퇴 처리**

   * 유저 삭제 요청 시 `is_deleted` 필드 true 처리
   * 이후 접근 시 예외 발생 및 `403 Forbidden` 반환

---

## 2. API 요청/응답 예시

### 2.1 회원가입

**POST** `/api/v1/users`

**Request Body:**

```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "테스트유저",
  "phone": "010-1234-5678",
  "loginMethod": "EMAIL"
}
```

**Response Body:**

```json
{
  "id": 1,
  "email": "test@example.com",
  "nickname": "테스트유저"
}
```

### 2.2 로그인

**POST** `/api/v1/users/login`

**Request Body:**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Response Body:**

```json
{
  "token": "<JWT-TOKEN>",
  "email": "test@example.com",
  "nickname": "테스트유저"
}
```

### 2.3 사용자 정보 조회

**GET** `/api/v1/users/{id}`

**Headers:**

```http
Authorization: Bearer <JWT-TOKEN>
```

**Response Body:**

```json
{
  "id": 1,
  "email": "test@example.com",
  "nickname": "테스트유저",
  "phone": "010-1234-5678"
}
```

### 2.4 탈퇴 계정 복구

**POST** `/api/v1/users/recover`

**Request Body:**

```json
{
  "email": "test@example.com"
}
```
**Response Body:**
```
json
{
  "id": 1,
  "email": "test@example.com",
  "nickname": "테스트유저"
}
```

**탈퇴한 지 30일 이내의 계정만 복구할 수 있으며, 이후에는 복구 불가 오류가 반환됩니다.**

### 2.5 탈퇴 유저 영구 삭제 (스케줄러)

탈퇴 처리된 유저(`is_deleted = true`)는 30일 동안 복구가 가능하며,  
이후에는 **Scheduler**를 통해 DB에서 영구 삭제됩니다.

- 스케줄 주기: 매일 새벽 3시
- 구현 클래스: `UserCleanupScheduler`

---

## 3. 오류 코드 및 예외 처리 방식

모든 예외는 `GlobalExceptionHandler`에서 공통 형식으로 처리됩니다.

**공통 에러 응답 형식:**

```json
{
  "status": 400,
  "error": "INVALID_INPUT_VALUE",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다."
}
```

**주요 오류 코드 예시:**

| Error Code              | HttpStatus | Message                                     |
| ----------------------- | ---------- |---------------------------------------------|
| INVALID_INPUT_VALUE     | 400        | 이메일 또는 비밀번호가 올바르지 않습니다.                     |
| USER_NOT_FOUND          | 404        | 탈퇴한지 30일이 지나 복구할 수 없는 계정이거나, 존재하지 않는 계정입니다. |
| USER_ALREADY_EXISTS     | 400        | 이미 가입된 이메일입니다.                              |
| NICKNAME_ALREADY_EXISTS | 400        | 중복된 닉네임입니다.                                 |
| PHONE_ALREADY_EXISTS    | 400        | 중복된 전화번호입니다.                                |
| USER_DELETED            | 403        | 탈퇴한 계정입니다. 복구가 가능합니다.                       |
| USER_DELETION_EXPIRED   | 410        | 복구 가능한 기간(30일)이 지났습니다.                      |

---


## 미구현

* OAuth2.0 기반 소셜 로그인 확장
