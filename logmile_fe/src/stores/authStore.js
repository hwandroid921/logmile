import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const name = ref(localStorage.getItem('adminName') || null)
  const role = ref(localStorage.getItem('adminRole') || null)
  const status = ref(localStorage.getItem('adminStatus') || null)
  const companyId = ref(localStorage.getItem('adminCompanyId') ? Number(localStorage.getItem('adminCompanyId')) : null)

  const isLoggedIn = computed(() => !!token.value)
  const isSuperAdmin = computed(() => role.value === 'ROLE_SUPER_ADMIN')

  function setAuth(authToken, adminName, adminRole, adminStatus, adminCompanyId) {
    token.value = authToken
    name.value = adminName
    role.value = adminRole
    status.value = adminStatus ?? null
    companyId.value = adminCompanyId ?? null
    localStorage.setItem('token', authToken)
    localStorage.setItem('adminName', adminName)
    localStorage.setItem('adminRole', adminRole)
    if (adminStatus != null) localStorage.setItem('adminStatus', adminStatus)
    if (adminCompanyId != null) localStorage.setItem('adminCompanyId', String(adminCompanyId))
  }

  function clearAuth() {
    token.value = null
    name.value = null
    role.value = null
    status.value = null
    companyId.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('adminName')
    localStorage.removeItem('adminRole')
    localStorage.removeItem('adminStatus')
    localStorage.removeItem('adminCompanyId')
  }

  return { token, name, role, status, companyId, isLoggedIn, isSuperAdmin, setAuth, clearAuth }
})
