<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 왼쪽 : 로고 -->
      <div class="register-logo">
        <img src="/egodaLogo.png" alt="egoda" />
      </div>

      <!-- 오른쪽 : 회원가입 폼 -->
      <div class="register-form">
        <div class="back-link" @click="$router.go(-1)">← 돌아가기</div>

        <h1>회원가입</h1>
        <p class="sub-text">Register</p>

        <!-- 성 & 이름 -->
        <div class="name-row">
          <div class="input-group half">
            <label for="lastName">성</label>
            <input type="text" id="lastName" v-model="lastName" placeholder="성" />
          </div>
          <div class="input-group half">
            <label for="firstName">이름</label>
            <input type="text" id="firstName" v-model="firstName" placeholder="이름" />
          </div>
        </div>

        <!-- 이메일 -->
        <div class="input-group">
          <label for="email">이메일</label>
          <input type="email" id="email" v-model="email" placeholder="이메일" />
        </div>

        <!-- 생일 & 전화번호 -->
        <div class="name-row">
          <div class="input-group half">
            <label for="birthday">생일</label>
            <input type="date" id="birthday" v-model="birthday" />
          </div>
          <div class="input-group half">
            <label for="phoneNumber">전화번호</label>
            <input type="tel" id="phoneNumber" v-model="phoneNumber" placeholder="010-0000-0000" />
          </div>
        </div>

        <!-- 비밀번호 -->
        <div class="input-group">
          <label for="password">비밀번호</label>
          <div class="password-box">
            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password" placeholder="비밀번호" />
            <button type="button" class="toggle-btn" @click="togglePassword">
              {{ showPassword ? '숨김' : '보기' }}
            </button>
          </div>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="input-group">
          <label for="confirmPassword">비밀번호 확인</label>
          <div class="password-box">
            <input :type="showConfirmPassword ? 'text' : 'password'" id="confirmPassword" v-model="confirmPassword" placeholder="비밀번호 확인" />
            <button type="button" class="toggle-btn" @click="toggleConfirmPassword">
              {{ showConfirmPassword ? '숨김' : '보기' }}
            </button>
          </div>
        </div>

        <div class="recaptcha-box">
          <div ref="recaptcha"></div>
          <p v-if="recaptchaError" class="recaptcha-error">{{ recaptchaError }}</p>
        </div>

        <button class="register-btn" @click="register" :disabled="isSubmitting">
          {{ isSubmitting ? '등록 중...' : '계정 생성' }}
        </button>

        <div class="options">
          <input type="checkbox" id="terms" v-model="agree" />
          <label for="terms" class="terms-label">
            회원가입 시,
            <span class="link" role="button" @click="openTerms">트립닷컴 이용약관</span> 및
            <span class="link" role="button" @click="openPrivacy">개인정보 정책</span>에 동의하시게 됩니다.
          </label>
        </div>

        <div class="divider">---------------------------------------- 또는 ---------------------------------------</div>

        <div class="social-login">
          <div class="social-box">
            <img src="/naverLogo.png" alt="네이버 로그인" />
          </div>
          <div class="social-box">
            <img src="/googleLogo.png" alt="구글 로그인" />
          </div>
          <div class="social-box">
            <img src="/kakaoLogo.png" alt="카카오 로그인" />
          </div>
        </div>
      </div>
    </div>

    <transition name="modal">
      <div v-if="showTermsModal" class="modal-overlay" @click.self="closeTerms">
        <div class="modal-content">
          <div class="modal-header">
            <h2>이용약관</h2>
            <button type="button" class="modal-close" @click="closeTerms">×</button>
          </div>
          <div class="modal-body">
            <p>
              <strong>제1조 (목적)</strong><br />
              본 약관은 호텔 예약 플랫폼(이하 "회사")이 제공하는 숙박 예약 및 관련 서비스(이하 "서비스")의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
            </p>
            <p>
              <strong>제2조 (정의)</strong><br />
              1. "서비스"란 회사가 제공하는 숙박 예약, 호텔 정보 제공, 결제 대행 등 플랫폼을 통해 제공되는 모든 서비스를 의미합니다.<br />
              2. "회원"이란 본 약관에 동의하고 회원가입을 완료하여 회사가 제공하는 서비스를 이용하는 자를 말합니다.<br />
              3. "호텔 파트너"란 회사의 플랫폼을 통해 숙박 시설을 등록하고 판매하는 사업자를 의미합니다.<br />
              4. "예약"이란 회원이 플랫폼을 통해 숙박 상품을 선택하고 결제를 완료하여 이용 권한을 확보하는 것을 말합니다.
            </p>
            <p>
              <strong>제3조 (약관의 효력 및 변경)</strong><br />
              1. 본 약관은 서비스 화면에 게시하거나 기타의 방법으로 회원에게 공지함으로써 효력이 발생합니다.<br />
              2. 회사는 합리적인 사유가 발생할 경우 관련 법령에 위배되지 않는 범위에서 본 약관을 변경할 수 있으며, 변경된 약관은 시행일자 7일 전에 공지합니다.<br />
              3. 회원이 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단하고 탈퇴할 수 있습니다.
            </p>
            <p>
              <strong>제4조 (회원가입 및 계정관리)</strong><br />
              1. 회원가입은 이용자가 약관의 내용에 동의하고 회원가입 신청을 한 후 회사가 이를 승낙함으로써 체결됩니다.<br />
              2. 회원은 회원가입 시 제공한 정보에 변경사항이 있을 경우 즉시 수정해야 하며, 정보 미수정으로 인한 불이익은 회원 본인이 부담합니다.<br />
              3. 회원은 자신의 아이디와 비밀번호를 제3자에게 이용하게 해서는 안 되며, 관리 소홀로 인한 책임은 회원 본인에게 있습니다.
            </p>
            <p>
              <strong>제5조 (예약 및 결제)</strong><br />
              1. 회원은 플랫폼에서 제공하는 정보를 확인한 후 예약 신청을 할 수 있으며, 회사는 회원의 예약 신청에 대해 승낙 또는 거절할 수 있습니다.<br />
              2. 예약 확정 후 결제가 완료된 경우, 회원과 호텔 파트너 간 숙박 계약이 성립된 것으로 봅니다.<br />
              3. 결제는 신용카드, 계좌이체, 간편결제 등 회사가 제공하는 방법으로 진행되며, 결제 관련 분쟁은 관련 법령 및 정책에 따릅니다.
            </p>
            <p>
              <strong>제6조 (취소 및 환불)</strong><br />
              1. 예약 취소 및 환불 규정은 각 호텔의 취소 정책에 따르며, 예약 시 해당 정책을 반드시 확인해야 합니다.<br />
              2. 회원의 단순 변심으로 인한 취소 시 호텔의 취소 수수료 정책이 적용됩니다.<br />
              3. 천재지변, 감염병 확산 등 불가피한 사유로 인한 취소는 회사와 호텔 파트너가 협의하여 처리합니다.
            </p>
            <p>
              <strong>제7조 (서비스의 제공 및 중단)</strong><br />
              1. 회사는 연중무휴 24시간 서비스를 제공함을 원칙으로 하나, 시스템 점검, 서버 장애 등 부득이한 사유로 서비스가 일시 중단될 수 있습니다.<br />
              2. 회사는 서비스 개선을 위해 사전 공지 후 서비스의 내용을 변경하거나 추가할 수 있습니다.
            </p>
            <p>
              <strong>제8조 (회원의 의무)</strong><br />
              1. 회원은 관계 법령, 본 약관의 규정, 이용안내 및 서비스와 관련하여 공지한 주의사항을 준수해야 합니다.<br />
              2. 회원은 타인의 명의나 정보를 도용하여 서비스를 이용해서는 안 되며, 허위 정보를 제공해서는 안 됩니다.<br />
              3. 회원은 호텔 파트너 및 다른 회원에게 피해를 주는 행위를 해서는 안 됩니다.
            </p>
            <p>
              <strong>제9조 (개인정보 보호)</strong><br />
              회사는 관련 법령이 정하는 바에 따라 회원의 개인정보를 보호하기 위해 노력하며, 개인정보의 수집, 이용, 제공 등에 관한 사항은 개인정보 처리방침에 따릅니다.
            </p>
            <p>
              <strong>제10조 (면책조항)</strong><br />
              1. 회사는 천재지변, 전쟁, 기간통신사업자의 서비스 중지 등 불가항력으로 인해 서비스를 제공할 수 없는 경우 책임이 면제됩니다.<br />
              2. 회사는 회원 간 또는 회원과 호텔 파트너 간에 발생한 분쟁에 대해 개입할 의무가 없으며, 이로 인한 손해를 배상할 책임이 없습니다.<br />
              3. 회원의 귀책사유로 인한 서비스 이용 장애에 대해서는 회사가 책임을 지지 않습니다.
            </p>
            <p>
              <strong>부칙</strong><br />
              본 약관은 2025년 1월 1일부터 시행됩니다.
            </p>
          </div>
        </div>
      </div>
    </transition>

    <transition name="modal">
      <div v-if="showPrivacyModal" class="modal-overlay" @click.self="closePrivacy">
        <div class="modal-content">
          <div class="modal-header">
            <h2>개인정보 처리방침</h2>
            <button type="button" class="modal-close" @click="closePrivacy">×</button>
          </div>
          <div class="modal-body">
            <p>
              호텔 예약 플랫폼(이하 "회사")은 개인정보보호법 제30조에 따라 정보주체의 개인정보를 보호하고 이와 관련한 고충을 신속하고 원활하게 처리할 수 있도록 하기 위하여 다음과 같이 개인정보 처리방침을 수립·공개합니다.
            </p>
            <p>
              <strong>1. 개인정보의 수집 및 이용목적</strong><br />
              회사는 다음의 목적을 위하여 개인정보를 처리합니다. 처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며, 이용 목적이 변경되는 경우에는 개인정보보호법 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.<br /><br />
              
              가. 회원 가입 및 관리<br />
              - 회원 가입의사 확인, 회원제 서비스 제공에 따른 본인 식별·인증, 회원자격 유지·관리, 서비스 부정이용 방지, 각종 고지·통지 등<br /><br />
              
              나. 숙박 예약 및 서비스 제공<br />
              - 예약 접수 및 확인, 예약 변경 및 취소 처리, 결제 및 환불 처리, 예약 내역 관리, 고객 문의 응대, 서비스 이용 통계 분석<br /><br />
              
              다. 마케팅 및 광고 활용 (선택)<br />
              - 신규 서비스 개발 및 맞춤 서비스 제공, 이벤트 및 프로모션 정보 제공, 접속빈도 파악, 회원의 서비스 이용 통계
            </p>
            <p>
              <strong>2. 수집하는 개인정보 항목</strong><br />
              가. 회원가입 시<br />
              - 필수항목: 이름, 이메일, 비밀번호, 휴대전화번호, 생년월일<br />
              - 선택항목: 주소, 프로필 사진<br /><br />
              
              나. 예약 및 결제 시<br />
              - 필수항목: 투숙자 이름, 연락처, 결제정보(카드번호, 유효기간 등)<br />
              - 선택항목: 특별 요청사항<br /><br />
              
              다. 서비스 이용과정에서 자동 수집되는 정보<br />
              - IP 주소, 쿠키, 서비스 이용 기록, 방문 기록, 불량 이용 기록, 기기정보(OS, 브라우저 버전 등)
            </p>
            <p>
              <strong>3. 개인정보의 처리 및 보유기간</strong><br />
              회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.<br /><br />
              
              - 회원 가입 및 관리: 회원 탈퇴 시까지 (단, 관련 법령 위반에 따른 수사·조사 등이 진행 중인 경우 해당 조사 종료 시까지)<br />
              - 예약 및 결제 정보: 전자상거래법에 따라 5년<br />
              - 소비자 불만 또는 분쟁처리 기록: 전자상거래법에 따라 3년<br />
              - 접속 로그 기록: 통신비밀보호법에 따라 3개월<br />
              - 마케팅 정보 수신 동의 철회 시: 즉시 파기
            </p>
            <p>
              <strong>4. 개인정보의 제3자 제공</strong><br />
              회사는 원칙적으로 정보주체의 개인정보를 수집·이용 목적으로 명시한 범위 내에서만 처리하며, 다음의 경우를 제외하고는 정보주체의 사전 동의 없이 본래의 목적 범위를 초과하여 처리하거나 제3자에게 제공하지 않습니다.<br /><br />
              
              - 호텔 파트너: 예약 처리 및 숙박 서비스 제공을 위해 예약자 정보(이름, 연락처, 예약 내역) 제공<br />
              - 결제대행업체: 결제 처리를 위해 결제 정보 제공<br />
              - 법령에 의한 요구가 있는 경우
            </p>
            <p>
              <strong>5. 개인정보 처리의 위탁</strong><br />
              회사는 원활한 서비스 제공을 위해 다음과 같이 개인정보 처리업무를 위탁하고 있습니다.<br /><br />
              
              - 위탁업체: AWS (Amazon Web Services)<br />
              - 위탁업무: 서버 호스팅 및 데이터 보관<br />
              - 보유 및 이용기간: 회원 탈퇴 시 또는 위탁계약 종료 시까지
            </p>
            <p>
              <strong>6. 정보주체의 권리·의무 및 행사방법</strong><br />
              정보주체는 회사에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.<br /><br />
              
              1) 개인정보 열람 요구<br />
              2) 오류 등이 있을 경우 정정 요구<br />
              3) 삭제 요구<br />
              4) 처리정지 요구<br /><br />
              
              권리 행사는 회사에 대해 서면, 전화, 전자우편 등을 통하여 하실 수 있으며 회사는 이에 대해 지체 없이 조치하겠습니다.
            </p>
            <p>
              <strong>7. 개인정보의 파기</strong><br />
              회사는 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체 없이 해당 개인정보를 파기합니다.<br /><br />
              
              - 파기절차: 이용자가 입력한 정보는 목적 달성 후 별도의 DB에 옮겨져 내부 방침 및 기타 관련 법령에 따라 일정기간 저장된 후 파기됩니다.<br />
              - 파기방법: 전자적 파일 형태의 정보는 복구 및 재생되지 않도록 기술적 방법을 이용하여 완전히 삭제하며, 종이에 출력된 개인정보는 분쇄기로 분쇄하거나 소각합니다.
            </p>
            <p>
              <strong>8. 개인정보 보호책임자</strong><br />
              회사는 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제를 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.<br /><br />
              
              - 개인정보 보호책임자: 홍길동<br />
              - 이메일: privacy@hotel-platform.com<br />
              - 전화번호: 1588-0000
            </p>
            <p>
              <strong>9. 개인정보 처리방침의 변경</strong><br />
              이 개인정보 처리방침은 2025년 1월 1일부터 적용되며, 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 공지사항을 통하여 고지할 것입니다.
            </p>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped src="@/assets/css/login/register.css"></style>

<script>
import http from "@/api/http";

export default {
  name: "RegisterPage",
  data() {
    return {
      firstName: "",
      lastName: "",
      email: "",
      birthday: "",
      phoneNumber: "",
      password: "",
      confirmPassword: "",
      agree: false,
      showPassword: false,
      showConfirmPassword: false,
      recaptchaToken: "",
      recaptchaVerified: false,
      recaptchaError: "",
      recaptchaWidgetId: null,
      isSubmitting: false,
      recaptchaSiteKey: "",
      showTermsModal: false,
      showPrivacyModal: false,
    };
  },
  async mounted() {
    await this.fetchRecaptchaSiteKey();
    if (!this.recaptchaSiteKey) {
      return;
    }
    this.loadRecaptchaScript();
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    toggleConfirmPassword() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
    openTerms() {
      this.showTermsModal = true;
    },
    closeTerms() {
      this.showTermsModal = false;
    },
    openPrivacy() {
      this.showPrivacyModal = true;
    },
    closePrivacy() {
      this.showPrivacyModal = false;
    },
    async register() {
      // 필수 필드 검증
      if (!this.firstName || this.firstName.trim() === "") {
        alert("이름을 입력해주세요.");
        return;
      }
      
      if (!this.lastName || this.lastName.trim() === "") {
        alert("성을 입력해주세요.");
        return;
      }
      
      if (!this.email || this.email.trim() === "") {
        alert("이메일을 입력해주세요.");
        return;
      }
      
      if (!this.birthday || this.birthday.trim() === "") {
        alert("생일을 입력해주세요.");
        return;
      }
      
      if (!this.phoneNumber || this.phoneNumber.trim() === "") {
        alert("전화번호를 입력해주세요.");
        return;
      }
      
      if (!this.password || this.password.trim() === "") {
        alert("비밀번호를 입력해주세요.");
        return;
      }
      
      if (!this.confirmPassword || this.confirmPassword.trim() === "") {
        alert("비밀번호 확인을 입력해주세요.");
        return;
      }
      
      // 이메일 형식 검증
      const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
      if (!emailRegex.test(this.email)) {
        alert("올바른 이메일 형식을 입력해주세요.");
        return;
      }
      
      // 전화번호 형식 검증 (한국 형식: 010-0000-0000 또는 01000000000)
      const phoneRegex = /^(010|011|016|017|018|019)-?\d{3,4}-?\d{4}$/;
      if (!phoneRegex.test(this.phoneNumber.replace(/\s/g, ''))) {
        alert("올바른 전화번호 형식을 입력해주세요. (예: 010-0000-0000)");
        return;
      }
      
      // 비밀번호 최소 길이 검증
      if (this.password.length < 6) {
        alert("비밀번호는 최소 6자 이상이어야 합니다.");
        return;
      }
      
      // 약관 동의 확인
      if (!this.agree) {
        alert("약관에 동의해야 회원가입이 가능합니다.");
        return;
      }
      
      // 비밀번호 일치 확인
      if (this.password !== this.confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }

      if (!this.recaptchaSiteKey) {
        this.recaptchaError = "reCAPTCHA 설정을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.";
        alert("reCAPTCHA 설정을 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
        return;
      }

      if (!this.recaptchaToken) {
        this.recaptchaError = "reCAPTCHA 인증이 필요합니다.";
        return;
      }

      try {
        this.isSubmitting = true;
        const response = await http.post("/users/register", {
          name: this.lastName + this.firstName,
          email: this.email,
          password: this.password,
          phone: this.phoneNumber,
          address: "서울",
          dateOfBirth: this.birthday,
          recaptchaToken: this.recaptchaToken,
        });
        alert("회원가입 성공!");
        this.resetRecaptcha();
        this.$router.push("/login");
      } catch (error) {
        console.error("회원가입 실패:", error.response?.data || error.message);
        
        // 백엔드에서 온 에러 메시지가 있으면 그것을 사용, 없으면 기본 메시지
        const errorMessage = error.response?.data || "회원가입에 실패했습니다. 다시 시도해주세요.";
        alert(errorMessage);
        this.resetRecaptcha();
      } finally {
        this.isSubmitting = false;
      }
    },
    renderRecaptcha() {
      if (!this.recaptchaSiteKey) {
        this.recaptchaError = "reCAPTCHA 사이트 키가 설정되지 않았습니다.";
        return;
      }

      if (!this.$refs.recaptcha || !window.grecaptcha) {
        this.recaptchaError = "reCAPTCHA를 불러오지 못했습니다. 새로고침 후 다시 시도해주세요.";
        return;
      }

      this.recaptchaWidgetId = window.grecaptcha.render(this.$refs.recaptcha, {
        sitekey: this.recaptchaSiteKey,
        callback: (token) => this.onCaptchaSuccess(token),
        'expired-callback': () => this.onCaptchaExpired(),
        'error-callback': () => this.onCaptchaError(),
      });
    },
    onCaptchaSuccess(token) {
      this.recaptchaToken = token;
      this.recaptchaVerified = true;
      this.recaptchaError = "";
    },
    onCaptchaExpired() {
      this.recaptchaToken = "";
      this.recaptchaVerified = false;
      this.recaptchaError = "reCAPTCHA 인증이 만료되었습니다. 다시 인증해주세요.";
    },
    onCaptchaError() {
      this.recaptchaToken = "";
      this.recaptchaVerified = false;
      this.recaptchaError = "reCAPTCHA 로딩 중 오류가 발생했습니다.";
    },
    resetRecaptcha() {
      this.recaptchaToken = "";
      this.recaptchaVerified = false;
      if (window.grecaptcha && this.recaptchaWidgetId !== null) {
        window.grecaptcha.reset(this.recaptchaWidgetId);
      }
    },
    async fetchRecaptchaSiteKey() {
      try {
        const response = await http.get("/config/recaptcha-site-key");
        if (response?.data?.siteKey) {
          this.recaptchaSiteKey = response.data.siteKey;
          this.recaptchaError = "";
        } else {
          this.recaptchaError = "reCAPTCHA 사이트 키를 불러오지 못했습니다.";
        }
      } catch (error) {
        console.error("reCAPTCHA 사이트 키 로드 실패:", error.response?.data || error.message);
        this.recaptchaError = error.response?.data?.message || "reCAPTCHA 설정을 불러오는 중 오류가 발생했습니다.";
      }
    },
    loadRecaptchaScript() {
      if (!this.recaptchaSiteKey) {
        return;
      }

      if (window.grecaptcha) {
        this.renderRecaptcha();
        return;
      }

      window.onRecaptchaLoad = () => {
        this.renderRecaptcha();
      };

      if (!document.getElementById('recaptcha-script')) {
        const script = document.createElement('script');
        script.id = 'recaptcha-script';
        script.src = 'https://www.google.com/recaptcha/api.js?render=explicit&onload=onRecaptchaLoad';
        script.async = true;
        script.defer = true;
        document.head.appendChild(script);
      }
    },
  },
};
</script>
