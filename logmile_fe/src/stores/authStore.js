import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const name = ref(localStorage.getItem('adminName') || null)

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(authToken, adminName) {
    token.value = authToken
    name.value = adminName
    localStorage.setItem('token', authToken)
    localStorage.setItem('adminName', adminName)
  }

  function clearAuth() {
    token.value = null
    name.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('adminName')
  }

  return { token, name, isLoggedIn, setAuth, clearAuth }
})
