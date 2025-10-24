# 변경 파일 

- `LoginController.java`
- `User.java`
- `EmailService.java`
- `UserAdminDto.java`
- `AdminUserService.java`
- `AdminUserController.java`
- `UserManagement.vue`
- `user-management`

---

## 변경 내역

### 1. 캡챠 적용
- 로그인
- 회원가입

### 2. 로그인 실패 시 패널티

1) 사용자 실패 횟수(15분) >= 3이면 → 캡챠 요구
2) 사용자 실패 횟수(15분) >= 5이면 → 계정 15분 잠금 및 안내 이메일 발송
3) 사용자 실패 횟수(24시간) >= 10이면 → 24시간 차단 또는 관리자 검토(관리자가 사용자 관리 목록에서 잠금 및 차단 볼 수 있고 요약 패널에서 잠금해제 가능)
4) 로그인 성공 시 → 해당 사용자와 IP의 실패 카운트 초기화

### 3. env에 캡챠 키 값 추가
# reCAPTCHA 설정
VITE_RECAPTCHA_SITE_KEY=
RECAPTCHA_SECRET=
RECAPTCHA_VERIFY_URL=https://www.google.com/recaptcha/api/siteverify

### 4. 기타 버그 수정
회원가입 시 이용약관이나 정책 부분 누를 시 페이지로 열리면서 회원가입 입력 정보 다 날라가길래 수정


