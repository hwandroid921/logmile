import api from './axios'

export const driveHistoryApi = {
  getList: (params) => api.get('/api/drive-history', { params }),
  getDetail: (id) => api.get(`/api/drive-history/${id}`),
}
