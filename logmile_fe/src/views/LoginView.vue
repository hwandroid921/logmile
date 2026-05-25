<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/authApi'
import AppLogo from '@/components/layout/AppLogo.vue'

const router = useRouter()
const authStore = useAuthStore()

const role      = ref('ADMIN')   // ADMIN | SUPER_ADMIN
const email     = ref('')
const password  = ref('')
const showPw    = ref(false)
const loading   = ref(false)
const errorMsg  = ref('')

const SEED = {
  ADMIN:       { email: 'c1_admin1@logmile.com', pw: 'admin1234' },
  SUPER_ADMIN: { email: 'admin@logmile.com',     pw: 'admin1234' },
}

function fillSeed() {
  email.value    = SEED[role.value].email
  password.value = SEED[role.value].pw
}

async function submit() {
  if (!email.value || !password.value) {
    errorMsg.value = '이메일과 패스워드를 입력해주세요.'
    return
  }
  loading.value  = true
  errorMsg.value = ''
  try {
    const res = await authApi.login(email.value, password.value)
    const { accessToken, name, role: r, status: s, companyId: cId } = res.data
    authStore.setAuth(accessToken, name, r, s, cId)
    router.push({ name: r === 'ROLE_SUPER_ADMIN' ? 'superHome' : 'dashboard' })
  } catch (e) {
    const status = e.response?.status
    const msg = e.response?.data?.message || ''
    if (status === 401) errorMsg.value = '이메일 또는 패스워드가 올바르지 않습니다.'
    else if (status === 403 && msg.includes('승인 대기')) {
      router.push({ name: 'pending' })
    } else if (status === 403) {
      errorMsg.value = msg || '접근이 제한된 계정입니다.'
    } else {
      errorMsg.value = '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
    }
  } finally {
    loading.value = false
  }
}

function onKey(e) { if (e.key === 'Enter') submit() }
</script>

<template>
  <div class="login-shell">
    <!-- LEFT: 브랜드 패널 -->
    <div class="brand-panel">
      <div class="brand-inner">
        <AppLogo :size="32" />
        <div class="brand-copy">
          <h1 class="brand-title">화물차 피로도<br/>관제 플랫폼</h1>
          <p class="brand-sub">실시간 운전자 상태 모니터링<br/>및 피로도 산정 시스템</p>
        </div>
        <div class="brand-stats">
          <div class="bs-item"><span class="bs-val mono">10</span><span class="bs-lbl">차량</span></div>
          <div class="bs-item"><span class="bs-val mono">10</span><span class="bs-lbl">운전자</span></div>
          <div class="bs-item"><span class="bs-val mono">21</span><span class="bs-lbl">임계값</span></div>
        </div>
      </div>
      <div class="brand-deco-1" aria-hidden="true"/>
      <div class="brand-deco-2" aria-hidden="true"/>
    </div>

    <!-- RIGHT: 로그인 폼 -->
    <div class="form-panel">
      <div class="form-card fade-up">
        <!-- 역할 탭 -->
        <div class="role-tabs">
          <button
            v-for="r in ['ADMIN', 'SUPER_ADMIN']"
            :key="r"
            class="role-tab"
            :class="{ active: role === r }"
            @click="role = r; errorMsg = ''"
          >
            {{ r === 'ADMIN' ? '업체 관리자' : '최상위 관리자' }}
          </button>
        </div>

        <h2 class="form-title">관리자 로그인</h2>
        <p class="form-sub">화물차 피로도 관제 시스템 · logmile</p>

        <div class="field">
          <label class="field-label">이메일</label>
          <input v-model="email" type="email" class="field-input mono"
            :placeholder="SEED[role].email" autocomplete="email" @keydown="onKey" />
        </div>

        <div class="field">
          <label class="field-label">패스워드</label>
          <div class="pw-wrap">
            <input v-model="password" :type="showPw ? 'text' : 'password'"
              class="field-input mono" placeholder="••••••••••••••••"
              autocomplete="current-password" @keydown="onKey" />
            <button class="pw-toggle" type="button" @click="showPw = !showPw">
              {{ showPw ? '숨기기' : '보기' }}
            </button>
          </div>
        </div>

        <p v-if="errorMsg" class="err-msg">{{ errorMsg }}</p>

        <button class="btn btn-primary submit-btn" :disabled="loading" @click="submit">
          <span v-if="loading" class="dot dot-ok" style="animation:blink-soft .8s infinite"/>
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>

        <div class="seed-box">
          <div class="seed-label mono">데모 계정</div>
          <div class="seed-row">
            <span class="mono seed-val">{{ SEED[role].email }}</span>
            <button class="seed-fill" @click="fillSeed">자동 입력</button>
          </div>
          <div class="mono seed-pw">비밀번호: {{ SEED[role].pw }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-shell { display: grid; grid-template-columns: 1fr 1fr; height: 100vh; overflow: hidden; }

.brand-panel {
  position: relative;
  background: linear-gradient(135deg, #1A2230 0%, #2C3851 55%, #515F7A 100%);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; padding: 48px;
}
.brand-inner { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 32px; }
.brand-title { font-size: 32px; font-weight: 700; color: #FFFFFF; line-height: 1.25; letter-spacing: 0; margin: 0 0 12px; }
.brand-sub { font-size: 14px; color: rgba(255,255,255,0.55); line-height: 1.65; margin: 0; }
.brand-stats { display: flex; gap: 24px; }
.bs-item { display: flex; flex-direction: column; gap: 3px; }
.bs-val { font-size: 28px; font-weight: 800; color: #FFFFFF; line-height: 1; }
.bs-lbl { font-size: 14px; color: rgba(255,255,255,0.45); letter-spacing: 0.06em; }
.brand-deco-1, .brand-deco-2 { position: absolute; border-radius: 50%; pointer-events: none; }
.brand-deco-1 { width: 400px; height: 400px; border: 1px solid rgba(255,255,255,0.06); bottom: -80px; right: -80px; }
.brand-deco-2 { width: 240px; height: 240px; border: 1px solid rgba(255,255,255,0.06); top: 40px; left: -60px; }

.form-panel { background: var(--bg-1); display: flex; align-items: center; justify-content: center; padding: 40px; overflow-y: auto; }
.form-card { width: 100%; max-width: 380px; }

.role-tabs { display: flex; gap: 4px; background: var(--bg-3); border: 1px solid var(--line-2); border-radius: var(--r-md); padding: 3px; margin-bottom: 28px; }
.role-tab { flex: 1; padding: 7px 12px; border-radius: var(--r-sm); font-size: 14px; font-weight: 500; color: var(--text-3); background: none; border: none; cursor: pointer; transition: background .12s, color .12s; font-family: inherit; }
.role-tab.active { background: var(--bg-1); color: var(--accent); font-weight: 600; box-shadow: 0 1px 3px rgba(81,95,122,0.12); }

.form-title { font-size: 22px; font-weight: 700; color: var(--text-1); margin: 0 0 4px; letter-spacing: 0; }
.form-sub { font-size: 14px; color: var(--text-3); margin: 0 0 28px; font-family: var(--font-mono); letter-spacing: 0.02em; }

.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.field-label { font-size: 14px; font-weight: 600; color: var(--text-3); letter-spacing: 0.08em; text-transform: uppercase; }
.field-input { width: 100%; box-sizing: border-box; background: var(--bg-0); border: 1px solid var(--line-2); border-radius: var(--r-md); padding: 9px 12px; font-size: 14px; color: var(--text-1); font-family: var(--font-mono); outline: none; transition: border-color .15s; }
.field-input::placeholder { color: var(--text-3); }
.field-input:focus { border-color: var(--accent-line); }
.pw-wrap { position: relative; }
.pw-wrap .field-input { padding-right: 60px; }
.pw-toggle { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); font-size: 14px; color: var(--text-3); cursor: pointer; font-family: var(--font-sans); transition: color .12s; }
.pw-toggle:hover { color: var(--text-2); }

.err-msg { font-size: 14px; color: var(--danger); margin: 0 0 12px; padding: 8px 12px; background: var(--danger-soft); border-radius: var(--r-sm); border: 1px solid rgba(181,84,74,0.2); }

.submit-btn { width: 100%; padding: 11px; font-size: 14px; justify-content: center; margin-bottom: 20px; gap: 8px; }
.submit-btn:disabled { opacity: .6; cursor: not-allowed; }

.seed-box { background: var(--bg-2); border: 1px solid var(--line-1); border-radius: var(--r-md); padding: 12px 14px; display: flex; flex-direction: column; gap: 6px; }
.seed-label { font-size: 14px; letter-spacing: 0.12em; color: var(--text-3); text-transform: uppercase; }
.seed-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.seed-val { font-size: 14px; color: var(--text-2); }
.seed-fill { font-size: 14px; color: var(--accent); cursor: pointer; font-family: var(--font-sans); transition: color .12s; white-space: nowrap; }
.seed-fill:hover { color: var(--accent-hover); }
.seed-pw { font-size: 14px; color: var(--text-3); }
</style>
