<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/authApi'
import AppLogo from '@/components/layout/AppLogo.vue'

const router = useRouter()

const form = ref({
  companyName: '',
  address: '',
  phone: '',
  name: '',
  adminPhone: '',
  email: '',
  password: '',
  passwordConfirm: '',
})

const loading = ref(false)
const errorMsg = ref('')

const passwordMismatch = computed(
  () => form.value.passwordConfirm && form.value.password !== form.value.passwordConfirm
)

async function handleSignup() {
  const { companyName, name, email, password, passwordConfirm } = form.value

  if (!companyName || !name || !email || !password) {
    errorMsg.value = '필수 항목(*)을 모두 입력해주세요.'
    return
  }
  if (password !== passwordConfirm) {
    errorMsg.value = '패스워드가 일치하지 않습니다.'
    return
  }
  if (password.length < 6) {
    errorMsg.value = '패스워드는 6자 이상이어야 합니다.'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const { passwordConfirm: _, ...payload } = form.value
    await authApi.signup(payload)
    router.push({ name: 'pending' })
  } catch (e) {
    const status = e.response?.status
    if (status === 409) errorMsg.value = '이미 사용 중인 이메일 또는 업체명입니다.'
    else errorMsg.value = '회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-screen">
    <div class="auth-bg-deco" aria-hidden="true" />

    <div class="auth-card fade-up">
      <div class="auth-logo">
        <AppLogo :size="26" />
      </div>

      <h1 class="auth-title">업체 등록 신청</h1>
      <p class="auth-sub">가입 후 최상위 관리자 승인이 필요합니다</p>

      <div class="auth-form">
        <!-- 업체 정보 섹션 -->
        <div class="form-section-label">업체 정보</div>

        <div class="field">
          <label class="field-label">업체명 <span class="required">*</span></label>
          <input
            v-model="form.companyName"
            type="text"
            class="field-input"
            placeholder="로그마일운송"
          />
        </div>

        <div class="field-row">
          <div class="field">
            <label class="field-label">업체 주소</label>
            <input
              v-model="form.address"
              type="text"
              class="field-input"
              placeholder="서울특별시 중구"
            />
          </div>
          <div class="field field--short">
            <label class="field-label">업체 연락처</label>
            <input
              v-model="form.phone"
              type="text"
              class="field-input"
              placeholder="02-0000-0000"
            />
          </div>
        </div>

        <!-- 관리자 정보 섹션 -->
        <div class="form-section-label" style="margin-top: 4px">관리자 정보</div>

        <div class="field-row">
          <div class="field">
            <label class="field-label">관리자 이름 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              class="field-input"
              placeholder="홍길동"
            />
          </div>
          <div class="field">
            <label class="field-label">관리자 연락처</label>
            <input
              v-model="form.adminPhone"
              type="text"
              class="field-input"
              placeholder="010-0000-0000"
            />
          </div>
        </div>

        <div class="field">
          <label class="field-label">이메일 <span class="required">*</span></label>
          <input
            v-model="form.email"
            type="email"
            class="field-input"
            placeholder="admin@logmile.com"
            autocomplete="email"
          />
        </div>

        <div class="field">
          <label class="field-label">패스워드 <span class="required">*</span></label>
          <input
            v-model="form.password"
            type="password"
            class="field-input"
            placeholder="6자 이상"
            autocomplete="new-password"
          />
        </div>

        <div class="field">
          <label class="field-label">패스워드 확인 <span class="required">*</span></label>
          <input
            v-model="form.passwordConfirm"
            type="password"
            class="field-input"
            :class="{ 'field-input--error': passwordMismatch }"
            placeholder="패스워드 재입력"
            autocomplete="new-password"
          />
          <span v-if="passwordMismatch" class="field-hint-error">패스워드가 일치하지 않습니다.</span>
        </div>

        <!-- 에러 메시지 -->
        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

        <!-- 신청 버튼 -->
        <button
          class="btn btn-primary auth-submit"
          :disabled="loading || passwordMismatch"
          @click="handleSignup"
        >
          {{ loading ? '신청 중...' : '가입 신청' }}
        </button>
      </div>

      <p class="auth-footer-text">
        이미 계정이 있으신가요?
        <router-link :to="{ name: 'login' }" class="auth-link">로그인</router-link>
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
  padding: 40px 16px;
  position: relative;
  overflow: hidden;
}

.auth-bg-deco {
  position: absolute;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-soft) 0%, transparent 70%);
  bottom: -120px;
  left: -120px;
  pointer-events: none;
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-lg);
  padding: 36px 36px 32px;
  box-shadow: var(--shadow-2);
}

.auth-logo { margin-bottom: 20px; }

.auth-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0 0 4px;
  letter-spacing: 0;
}

.auth-sub {
  font-size: 14px;
  color: var(--text-3);
  margin: 0 0 24px;
}

/* 섹션 레이블 */
.form-section-label {
  font-family: var(--font-mono);
  font-size: 14px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-3);
  padding-bottom: 8px;
  border-bottom: 1px solid var(--line-1);
  margin-bottom: 4px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.field--short { min-width: 0; }

.field {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}

.field-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-3);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.required { color: var(--danger); }

.field-input {
  width: 100%;
  background: var(--bg-1);
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  padding: 8px 12px;
  font-size: 14px;
  color: var(--text-1);
  font-family: var(--font-sans);
  outline: none;
  transition: border-color 0.15s;
  box-sizing: border-box;
}
.field-input::placeholder { color: var(--text-3); }
.field-input:focus { border-color: var(--accent-line); }
.field-input--error { border-color: var(--danger) !important; }

.field-hint-error {
  font-size: 14px;
  color: var(--danger);
}

.auth-error {
  font-size: 14px;
  color: var(--danger);
  margin: 0;
  padding: 8px 12px;
  background: var(--danger-soft);
  border-radius: var(--r-sm);
  border: 1px solid rgba(209, 139, 126, 0.2);
}

.auth-submit {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  margin-top: 4px;
}
.auth-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.auth-footer-text {
  margin: 20px 0 0;
  font-size: 14px;
  color: var(--text-3);
  text-align: center;
}
.auth-link {
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
}
.auth-link:hover { color: var(--accent-hover); }
</style>
