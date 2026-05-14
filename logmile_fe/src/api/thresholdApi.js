import api from './axios'

export const thresholdApi = {
  getAll: () => api.get('/api/thresholds'),
  update: (id, thresholdValue, description = null) =>
    api.patch(`/api/thresholds/${id}`, { thresholdValue, description }),
}
