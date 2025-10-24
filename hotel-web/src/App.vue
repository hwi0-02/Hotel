<script setup>
import { ref, onMounted } from 'vue'
import * as Sentry from "@sentry/vue";
import "flatpickr/dist/flatpickr.css";

const showTestPanel = ref(false)

// 테스트용 에러 발생 함수들
const throwError = () => {
  throw new Error("🔴 App.vue에서 발생한 테스트 에러입니다!");
}

const throwUndefinedError = () => {
  const obj = undefined;
  obj.property.method(); // TypeError 발생
}

const throwAsyncError = async () => {
  try {
    throw new Error("❌ 비동기 처리 중 발생한 에러입니다!");
  } catch (error) {
    Sentry.captureException(error);
    alert("에러가 Sentry로 전송되었습니다!");
  }
}

const sendCustomErrorToSentry = () => {
  Sentry.captureMessage("⚠️ 커스텀 메시지: Sentry 테스트 데코 웹훅 검증 중입니다.", "warning");
  alert("커스텀 메시지가 Sentry로 전송되었습니다!");
}

const sendPerformanceWarning = () => {
  Sentry.captureMessage("⏱️ 성능 경고: 응답 시간이 지나치게 길어졌습니다.", "info");
  alert("성능 경고가 Sentry로 전송되었습니다!");
}

const createDecoWebhookEvent = () => {
  // Sentry 대시보드에서 webhook을 받을 수 있도록 이벤트 생성
  Sentry.captureException(new Error("🎯 Sentry 데코 웹훅 테스트 - " + new Date().toISOString()));
  alert("데코 웹훅 테스트 이벤트가 Sentry로 전송되었습니다!");
}

onMounted(() => {
  // 개발 환경에서만 테스트 패널 표시
  if (import.meta.env.DEV) {
    showTestPanel.value = true;
  }
})
</script>

<template>
  <!-- Sentry 테스트 패널 -->
  <div v-if="showTestPanel" class="sentry-test-panel">
    <div class="panel-header">
      <h3>🔬 Sentry 테스트 패널</h3>
      <button @click="showTestPanel = false" class="close-btn">×</button>
    </div>
    
    <div class="panel-content">
      <h4>에러 발생 테스트</h4>
      <div class="button-group">
        <button @click="throwError" class="btn btn-error">일반 에러 발생</button>
        <button @click="throwUndefinedError" class="btn btn-error">TypeError 발생</button>
        <button @click="throwAsyncError" class="btn btn-warning">비동기 에러</button>
      </div>

      <h4>Sentry 직접 전송 테스트</h4>
      <div class="button-group">
        <button @click="sendCustomErrorToSentry" class="btn btn-info">커스텀 메시지 전송</button>
        <button @click="sendPerformanceWarning" class="btn btn-info">성능 경고 전송</button>
        <button @click="createDecoWebhookEvent" class="btn btn-success">데코 웹훅 테스트</button>
      </div>

      <div class="info-box">
        <p>💡 <strong>사용 방법:</strong></p>
        <ul>
          <li>버튼을 클릭하면 에러가 발생합니다</li>
          <li>자동으로 Sentry로 전송됩니다</li>
          <li>Sentry 대시보드에서 실시간으로 확인할 수 있습니다</li>
          <li>데코 웹훅이 설정되면 Discord/Slack 등으로 알림이 옵니다</li>
        </ul>
      </div>
    </div>
  </div>

  <RouterView />
</template>

<style scoped>
.sentry-test-panel {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 420px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  color: white;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  z-index: 9999;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateY(100px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.panel-content {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.panel-content h4 {
  margin: 12px 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.btn {
  flex: 1;
  min-width: 100px;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-error {
  background: #ff6b6b;
  color: white;
}

.btn-error:hover {
  background: #ff5252;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.btn-warning {
  background: #ffa940;
  color: white;
}

.btn-warning:hover {
  background: #ff9c1f;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 169, 64, 0.4);
}

.btn-info {
  background: #1890ff;
  color: white;
}

.btn-info:hover {
  background: #0050b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.btn-success {
  background: #52c41a;
  color: white;
}

.btn-success:hover {
  background: #389e0d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
}

.info-box {
  background: rgba(255, 255, 255, 0.1);
  border-left: 3px solid rgba(255, 255, 255, 0.5);
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
  font-size: 12px;
  line-height: 1.5;
}

.info-box p {
  margin: 0 0 8px 0;
}

.info-box ul {
  margin: 0;
  padding-left: 18px;
}

.info-box li {
  margin: 4px 0;
}
</style>