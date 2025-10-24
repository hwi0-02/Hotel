<template>
  <header class="main-header">
    <div class="header-content">
      <div class="header-left">
        <button class="hamburger-menu" @click="toggleSidebar">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M3 6h18M3 12h18M3 18h18"
              stroke="rgba(0,0,0,0.54)" stroke-width="2" stroke-linecap="round" />
          </svg>
        </button>

        <div class="logo-container" @click="go('/')" role="button" tabindex="0">
          <img src="/egodaLogo2.png" alt="egoda" class="egoda-logo" decoding="async" fetchpriority="high" />
        </div>
      </div>

      <div class="header-right">
        <template v-if="!isLoggedIn">
          <span class="login-text" @click="go('/login')">로그인</span>
          <span>|</span>
          <span class="register-text" @click="go('/register')">회원가입</span>
        </template>
        <template v-else>
          <span class="user-name" @click="go('/myaccount')">{{ user.name }}님</span>
          <span>|</span>
          <span class="logout-text" @click="logout">로그아웃</span>
        </template>
      </div>
    </div>
  </header>

  <!-- ✅ 헤더 바깥(문서 루트)로 텔레포트 -->
  <teleport to="body">
    <!-- 사이드바 (항상 오버레이 위) -->
    <aside class="sidebar-drawer" :class="{ open: isSidebarOpen }" role="dialog" aria-modal="true">
      <button class="close-btn" @click="toggleSidebar">×</button>
      <ul>
        <li @click="go('/')">홈</li>

        <template v-if="!isLoggedIn">
          <li @click="go('/login')">로그인</li>
          <li @click="go('/register')">회원가입</li>
        </template>

        <li @click="go('/search')">호텔 검색</li>
        <li @click="go('/mywishlist')">찜 목록</li>
        <li @click="go('/myaccount')">마이페이지</li>

        <template v-if="isLoggedIn && user?.role === 'USER'">
          <li @click="go('/business/apply')">사업자 등록 신청</li>
        </template>

        <li @click="go('/websitesupport')">고객센터</li>

        <template v-if="isLoggedIn">
          <li @click="logout">로그아웃</li>
        </template>
      </ul>
    </aside>

    <!-- 오버레이 -->
    <div class="sidebar-backdrop" v-if="isSidebarOpen" @click="toggleSidebar" aria-hidden="true"></div>
  </teleport>
</template>

<!-- 기존 header.css는 그대로 두고, 새 클래스만 여기에 정의 -->
<style>
/* 레이어 고정: backdrop(10000) < drawer(10001) */
.sidebar-backdrop{
  position: fixed; inset: 0;
  background: rgba(0,0,0,.6);
  z-index: 10000;
}
.sidebar-drawer{
  position: fixed; top: 0; left: 0;
  height: 100vh; width: min(80vw, 320px);
  background: #fff;                  /* 완전 불투명 */
  box-shadow: 2px 0 24px rgba(0,0,0,.24);
  transform: translateX(-100%);
  transition: transform .3s ease;
  z-index: 10001;                    /* 항상 오버레이 위 */
  will-change: transform;
  -webkit-backdrop-filter: none; backdrop-filter: none;
}
.sidebar-drawer.open{ transform: translateX(0); }
.sidebar-drawer .close-btn{
  font-size: 24px; border: none; background: none; cursor: pointer;
  margin: 16px 16px 8px 16px; display: block;
}
.sidebar-drawer ul{ list-style: none; padding: 0 20px 20px; margin: 0; }
.sidebar-drawer ul li{
  padding: 12px 0; font-size: 16px; font-weight: 600;
  cursor: pointer; transition: color .2s ease;
}
.sidebar-drawer ul li:hover{ color: #4ec89f; }
</style>

<style scoped src="@/assets/css/homepage/header.css"></style>

<script>
export default {
  name: "Header",
  props: {
    isLoggedIn: { type: Boolean, default: false },
    user: { type: Object, default: () => ({ name: "홍길동" }) }
  },
  data(){ return { isSidebarOpen: false }; },
  methods:{
    toggleSidebar(){
      this.isSidebarOpen = !this.isSidebarOpen;
      document.body.style.overflow = this.isSidebarOpen ? 'hidden' : '';
    },
    go(path){
      this.$router.push(path);
      this.isSidebarOpen = false;
      document.body.style.overflow = '';
    },
    logout(){
      this.$emit('logout');
      alert("로그아웃 되었습니다.");
    }
  }
};
</script>
