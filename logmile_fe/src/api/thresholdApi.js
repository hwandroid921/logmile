import api from './axios'

export const thresholdApi = {
  getAll: () => api.get('/api/fatigue/thresholds'),
  update: (key, thresholdValue) => api.put(`/api/fatigue/thresholds/${key}`, { thresholdValue }),
}
