import api from './axios'

export const driveHistoryApi = {
  getList: (params) => api.get('/api/drive-logs', { params }),
  getDetail: (id) => api.get(`/api/drive-logs/${id}`),
}
