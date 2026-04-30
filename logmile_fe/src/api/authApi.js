import api from './axios'

export const authApi = {
  login: (email, password) => api.post('/api/auth/login', { email, password }),
}
