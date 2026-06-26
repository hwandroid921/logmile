<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppLogo from '@/components/layout/AppLogo.vue'

const router = useRouter()
const authStore = useAuthStore()

function goLogin() {
  authStore.clearAuth()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="auth-screen">
    <div class="auth-bg-deco" aria-hidden="true" />

    <div class="pending-card fade-up">
      <AppLogo :size="26" />

      <!-- 아이콘 -->
      <div class="pending-icon-wrap">
        <svg class="pending-icon" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="24" cy="24" r="22" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
          <circle cx="24" cy="24" r="22" stroke="currentColor" stroke-width="1.5" stroke-dasharray="30 108" stroke-linecap="round">
            <animateTransform attributeName="transform" type="rotate" from="0 24 24" to="360 24 24" dur="2.4s" repeatCount="indefinite"/>
          </circle>
          <!-- clock hands -->
          <circle cx="24" cy="24" r="2" fill="currentColor"/>
          <line x1="24" y1="24" x2="24" y2="14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <line x1="24" y1="24" x2="31" y2="28" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>

      <h1 class="pending-title">가입 신청 완료</h1>
      <p class="pending-desc">
        회원가입 신청이 접수되었습니다.<br />
        최상위 관리자 승인 후 서비스를 이용할 수 있습니다.
      </p>

      <div class="pending-info">
        <div class="info-row">
          <span class="info-key mono">상태</span>
          <span class="chip chip-warn">승인 대기 중</span>
        </div>
        <div class="info-row">
          <span class="info-key mono">안내</span>
          <span class="info-val">승인 완료 후 등록된 이메일로 로그인하세요.</span>
        </div>
      </div>

      <button class="btn btn-ghost pending-btn" @click="goLogin">
        로그인 페이지로 돌아가기
      </button>
    </div>
  </div>
</template>

<style scoped>
.auth-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-0);
  position: relative;
  overflow: hidden;
}

.auth-bg-deco {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(214, 181, 106, 0.08) 0%, transparent 70%);
  top: -100px;
  left: -100px;
  pointer-events: none;
}

.pending-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-lg);
  padding: 40px 36px;
  box-shadow: var(--shadow-2);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
}

.pending-icon-wrap {
  margin: 28px 0 20px;
  align-self: center;
}
.pending-icon {
  width: 56px;
  height: 56px;
  color: var(--warn);
}

.pending-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0 0 10px;
  letter-spacing: 0;
}

.pending-desc {
  font-size: 14px;
  color: var(--text-3);
  line-height: 1.7;
  margin: 0 0 24px;
}

.pending-info {
  width: 100%;
  background: var(--bg-3);
  border: 1px solid var(--line-1);
  border-radius: var(--r-md);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-key {
  font-size: 14px;
  color: var(--text-3);
  min-width: 36px;
  letter-spacing: 0.04em;
}

.info-val {
  font-size: 14px;
  color: var(--text-2);
}

.pending-btn {
  width: 100%;
  padding: 10px;
  font-size: 14px;
}
</style>
