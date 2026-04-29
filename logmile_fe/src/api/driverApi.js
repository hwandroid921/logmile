import api from './axios'

export const driverApi = {
  getAll: () => api.get('/api/drivers'),
  create: (data) => api.post('/api/drivers', data),
  update: (id, data) => api.put(`/api/drivers/${id}`, data),
  remove: (id) => api.delete(`/api/drivers/${id}`),
}
