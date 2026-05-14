import api from './axios'

export const driveHistoryApi = {
  getList: () => api.get('/api/drive-history'),
  getDetail: (id) => api.get(`/api/drive-history/${id}`),
}
