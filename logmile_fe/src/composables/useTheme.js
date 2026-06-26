import { ref, watch } from 'vue'

const STORAGE_KEY = 'logmile-theme'

// 모듈 수준 싱글턴 — 앱 전역에서 같은 상태 공유
const isDark = ref(localStorage.getItem(STORAGE_KEY) === 'dark')

function applyTheme(dark) {
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
  localStorage.setItem(STORAGE_KEY, dark ? 'dark' : 'light')
}

// 초기 적용
applyTheme(isDark.value)

// 상태 변경 시 자동 반영
watch(isDark, applyTheme)

export function useTheme() {
  function toggle() { isDark.value = !isDark.value }
  return { isDark, toggle }
}
