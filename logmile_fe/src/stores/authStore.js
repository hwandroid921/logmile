import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const name = ref(localStorage.getItem('adminName') || null)
  const role = ref(localStorage.getItem('adminRole') || null)

  const isLoggedIn = computed(() => !!token.value)
  const isSuperAdmin = computed(() => role.value === 'ROLE_SUPER_ADMIN')

  function setAuth(authToken, adminName, adminRole) {
    token.value = authToken
    name.value = adminName
    role.value = adminRole
    localStorage.setItem('token', authToken)
    localStorage.setItem('adminName', adminName)
    localStorage.setItem('adminRole', adminRole)
  }

  function clearAuth() {
    token.value = null
    name.value = null
    role.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('adminName')
    localStorage.removeItem('adminRole')
  }

  return { token, name, role, isLoggedIn, isSuperAdmin, setAuth, clearAuth }
})
