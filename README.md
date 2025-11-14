# HOTEL – 클라우드 기반 숙소 예약 플랫폼

## 1. 프로젝트 소개
HOTEL은 사용자·호스트·관리자용 업무 흐름을 한곳에서 처리할 수 있는 Vue 3 + Spring Boot 기반 숙소 예약 서비스입니다. OAuth 로그인, 실시간 재고 관리, 결제/영수증 발송, 문의 응대, 통계 대시보드를 포함해 상용 서비스 수준의 기능을 제공합니다.

## 2. 전체 아키텍처
- **프런트엔드 (hotel-web)**: Vite + Vue 3 SPA, Nginx를 통해 정적 호스팅 및 API 프록시
- **백엔드 (my-backend)**: Spring Boot 3, MariaDB, JWT + OAuth2 인증, SendGrid 메일, Toss Payments 연동
- **인프라**: EC2(Front/Back) + RDS(MariaDB) + Route53 + Let’s Encrypt + Prometheus/Grafana 모니터링

```
브라우저 ─▶ Nginx(Front EC2) ─▶ Spring Boot API(Back EC2) ─▶ RDS(MariaDB)
															└▶ 정적 자산(dist)
```

## 3. 주요 기능
| 구분 | 기능 |
| --- | --- |
| 사용자 | 호텔 검색/필터, 객실 상세, 10분 홀드 예약, Toss 결제, 예약 내역 & 영수증, 리뷰 작성 |
| 호스트 | 객실 등록·수정, 예약 현황, 매출 차트, 리뷰 & 문의 관리, 쿠폰 발급 |
| 관리자 | 호텔 승인, 사용자/결제/리뷰/문의/매출 관리, 공지 및 통계 |
| 공통 | Google/Naver/Kakao OAuth, JWT 세션, reCAPTCHA, Rate Limiting(bucket4j), Safe HTML 렌더링 |

## 4. 기술 스택
| 영역 | 사용 기술 |
| --- | --- |
| 프런트 | Vue 3, Vite, Vue Router, Axios, Bootstrap 5, Chart.js, FullCalendar, Sentry |
| 백엔드 | Spring Boot 3, Spring Security, Spring Data JPA, MariaDB, Redis Cache(Caffeine), Bucket4j, JWT, SendGrid, Toss Payments SDK |
| 인프라 | AWS EC2, RDS(MariaDB), Route53, Nginx, Certbot, Prometheus, Grafana |

## 5. 로컬 개발 환경
### 5.1 필수 도구
- Node.js 20.x 이상 (engines: `^20.19.0 || >=22.12.0`)
- npm 10.x 이상
- JDK 17
- Maven Wrapper(`mvnw`)
- MariaDB 10.6+ (또는 RDS 인스턴스)

### 5.2 프로젝트 설치
```bash
git clone <repo-url>
cd hotel

# 프런트 의존성 설치
cd hotel-web
npm install
cd ..

# 백엔드는 Spring Boot Maven wrapper 사용 (빌드시 자동 다운로드)
```

### 5.3 환경 변수 설정
- 루트: `.env.example` 참고하여 `/opt/my-backend/.env` 또는 로컬 `.env` 작성
- 프런트: `hotel-web/.env.production` 또는 `.env.local`에 `VITE_API_BASE`, `VITE_RECAPTCHA_SITE_KEY` 등 추가
- 필수 키
	- DB 접속 정보
	- OAuth (Google/Naver/Kakao)
	- `JWT_SECRET`
	- SendGrid SMTP (`MAIL_*`)
	- Toss Payments (`TOSS_PAYMENTS_*`)
	- reCAPTCHA (`VITE_RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET`)

## 6. 실행 방법
### 6.1 개발 모드 (Windows)
```bash
cd hotel-web
npm run dev:win
```

### 6.2 개발 모드 (macOS/Linux)
```bash
cd hotel-web
npm run dev:nix
```

위 스크립트는 프런트(`vite dev`)와 백엔드(`./mvnw spring-boot:run`)를 동시에 실행합니다.

### 6.3 단독 실행
```bash
# Front
cd hotel-web
npm run dev

# Back
cd ../my-backend
./mvnw spring-boot:run
```

### 6.4 빌드 & 배포용 아티팩트
```bash
# Front build (dist 생성)
cd hotel-web
npm run build

# Back build (app.jar 생성)
cd ../my-backend
./mvnw clean package -DskipTests
```
- 프런트 산출물은 `/hotel-web/dist` → Nginx `/var/www/hotel`
- 백엔드 JAR은 `/opt/my-backend/app.jar`로 업로드 후 systemd 서비스 (`my-backend.service`) 재시작

## 7. 테스트
```bash
# 프런트 단위 테스트 (Vitest)
cd hotel-web
npm run test

# 백엔드 테스트 (JUnit)
cd ../my-backend
./mvnw test
```

## 8. 배포 요약
1. **RDS**: 손쉬운 생성(Easy Create), Public Access Yes, SG-rds로 보호
2. **Back EC2**: t2.micro, SG-backend (8888 포트는 Front SG만 허용)
3. **Front EC2**: t2.micro, Nginx + Certbot로 HTTPS 처리, `/api`, `/uploads` 프록시
4. **도메인**: Route53 또는 Cloudflare에서 Front IP로 A 레코드
5. **CI/CD 없음**: 수동으로 `dist/` rsync, `app.jar` 업로드 및 systemd 재시작
6. **모니터링**: `/actuator/prometheus` → Prometheus → Grafana 대시보드

자세한 단계는 `ec_2_front_back_rds_배포_완성_가이드.md` 문서를 참고하세요.

## 9. 폴더 구조 하이라이트
```
hotel/
├── hotel-web/            # Vue 3 SPA
│   ├── src/
│   │   ├── components/   # admin/owner/user 공용 컴포넌트
│   │   ├── api/          # Axios 래퍼, Hotel/Reservation/User API
│   │   └── router/       # Vue Router 설정
│   └── public/           # 정적 에셋 (이미지 등)
├── my-backend/           # Spring Boot 서버
│   ├── src/main/java     # 도메인, 서비스, 컨트롤러
│   ├── src/main/resources
│   │   ├── application.yml
│   │   └── templates/    # 메일/문서 템플릿
│   └── pom.xml
├── .env.example
└── README.md
```

## 10. 트러블슈팅 팁
- **OAuth 404**: Nginx `/login/oauth2/` 프록시, Redirect URI 일치 여부 확인
- **Authorization 헤더 누락**: 프런트 토큰 저장/주입 또는 Nginx `proxy_set_header Authorization` 확인
- **파일 업로드 실패**: `/opt/my-backend/uploads` 권한 및 Nginx `client_max_body_size` 값 확인
- **이메일 미전송**: SendGrid SMTP 자격 증명과 `MAIL_FROM` 주소 검증, 백엔드 `journalctl -u my-backend -f` 로그 확인

---
문의나 개선 제안은 Issue로 남겨 주세요. 🙌
