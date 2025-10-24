import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import http from './api/http'
import * as Sentry from "@sentry/vue"
import SafeHtml from '@/components/common/SafeHtml.vue'

// 전역(사용자 사이트) 스타일만
import '@/assets/css/hotel_detail/app.css'
import '@/assets/css/hotel_detail/hotel_detail.css'
import '@/assets/css/homepage/calendar.css'

import 'flatpickr/dist/flatpickr.css'
 
const app = createApp(App) // 👈 [수정] 앱 인스턴스를 변수에 할당합니다.

// Sentry 초기화
Sentry.init({
  app,
  dsn: "https://acacef884a7f193be8308f7eb4cbdb98@o4510221734772736.ingest.us.sentry.io/4510221763543041",
  // Setting this option to true will send default PII data to Sentry.
  // For example, automatic IP address collection on events
  sendDefaultPii: true
})

// 👇 [추가] 앱에 전역 속성으로 $axios를 설정합니다.
app.config.globalProperties.$axios = http;
app.component('SafeHtml', SafeHtml);

// 👇 [수정] 설정이 끝난 후 라우터를 사용하고 마운트합니다.
app.use(router).mount('#app')
