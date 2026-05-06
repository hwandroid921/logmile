<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/authApi'
import AppLogo from '@/components/layout/AppLogo.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (!email.value || !password.value) {
    errorMsg.value = '이메일과 패스워드를 입력해주세요.'
    return
  }
  loading.value = true
  errorMsg.value = ''

  try {
    const res = await authApi.login(email.value, password.value)
    const { accessToken, name, role } = res.data
    authStore.setAuth(accessToken, name, role)

    if (role === 'ROLE_SUPER_ADMIN') {
      router.push({ name: 'superHome' })
    } else {
      router.push({ name: 'dashboard' })
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 401) errorMsg.value = '이메일 또는 패스워드가 올바르지 않습니다.'
    else if (status === 403) errorMsg.value = '승인 대기 중이거나 접근이 제한된 계정입니다.'
    else errorMsg.value = '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}

function onKeydown(e) {
  if (e.key === 'Enter') handleLogin()
}
</script>

<template>
  <div class="auth-screen">
    <!-- 배경 장식 -->
    <div class="auth-bg-deco" aria-hidden="true" />

    <div class="auth-card fade-up">
      <!-- 로고 -->
      <div class="auth-logo">
        <AppLogo :size="28" />
      </div>

      <h1 class="auth-title">관리자 로그인</h1>
      <p class="auth-sub">화물차 피로도 관제 시스템</p>

      <!-- 폼 -->
      <div class="auth-form">
        <!-- 이메일 -->
        <div class="field">
          <label class="field-label">이메일</label>
          <input
            v-model="email"
            type="email"
            class="field-input"
            placeholder="admin@logmile.com"
            autocomplete="email"
            @keydown="onKeydown"
          />
        </div>

        <!-- 패스워드 -->
        <div class="field">
          <label class="field-label">패스워드</label>
          <div class="field-password">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="패스워드 입력"
              autocomplete="current-password"
              @keydown="onKeydown"
            />
            <button
              class="pw-toggle"
              type="button"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? '숨기기' : '보기'"
            >
              {{ showPassword ? '숨기기' : '보기' }}
            </button>
          </div>
        </div>

        <!-- 에러 메시지 -->
        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

        <!-- 로그인 버튼 -->
        <button
          class="btn btn-primary auth-submit"
          :disabled="loading"
          @click="handleLogin"
        >
          <span v-if="loading" class="dot dot-ok blink" style="animation: blink-soft 0.8s infinite" />
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </div>

      <!-- 회원가입 링크 -->
      <p class="auth-footer-text">
        계정이 없으신가요?
        <router-link :to="{ name: 'signup' }" class="auth-link">업체 등록 신청</router-link>
      </p>
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

/* 배경 원형 장식 */
.auth-bg-deco {
  position: absolute;
  width: 560px;
  height: 560px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-soft) 0%, transparent 70%);
  top: -140px;
  right: -140px;
  pointer-events: none;
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-lg);
  padding: 40px 36px 36px;
  box-shadow: var(--shadow-2);
}

.auth-logo {
  margin-bottom: 24px;
}

.auth-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}

.auth-sub {
  font-size: 12.5px;
  color: var(--text-4);
  margin: 0 0 28px;
  font-family: var(--font-mono);
  letter-spacing: 0.02em;
}

/* 폼 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--text-3);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.field-input {
  width: 100%;
  background: var(--bg-1);
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  padding: 9px 12px;
  font-size: 13.5px;
  color: var(--text-1);
  font-family: var(--font-sans);
  outline: none;
  transition: border-color 0.15s;
  box-sizing: border-box;
}
.field-input::placeholder {
  color: var(--text-4);
}
.field-input:focus {
  border-color: var(--accent-line);
}

/* 패스워드 wrapper */
.field-password {
  position: relative;
}
.field-password .field-input {
  padding-right: 56px;
}
.pw-toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 11px;
  color: var(--text-4);
  cursor: pointer;
  padding: 2px 4px;
  font-family: var(--font-sans);
  transition: color 0.15s;
}
.pw-toggle:hover {
  color: var(--text-2);
}

/* 에러 */
.auth-error {
  font-size: 12px;
  color: var(--danger);
  margin: 0;
  padding: 8px 12px;
  background: var(--danger-soft);
  border-radius: var(--r-sm);
  border: 1px solid rgba(209, 139, 126, 0.2);
}

/* 제출 버튼 */
.auth-submit {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.auth-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 하단 링크 */
.auth-footer-text {
  margin: 20px 0 0;
  font-size: 12.5px;
  color: var(--text-4);
  text-align: center;
}
.auth-link {
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.15s;
}
.auth-link:hover {
  color: var(--accent-hover);
}
</style>
