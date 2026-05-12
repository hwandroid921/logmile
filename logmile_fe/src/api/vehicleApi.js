import api from './axios'

export const vehicleApi = {
  getAll: () => api.get('/api/vehicles'),
  create: (data) => api.post('/api/vehicles', data),
  update: (id, data) => api.patch(`/api/vehicles/${id}`, data),
  remove: (id) => api.delete(`/api/vehicles/${id}`),
}
