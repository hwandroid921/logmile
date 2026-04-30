import api from './axios'

export const fatigueStatsApi = {
  getStats: (params) => api.get('/api/fatigue/stats', { params }),
}
