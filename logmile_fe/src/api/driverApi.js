import api from './axios'

export const driverApi = {
  getAll: () => api.get('/api/drivers'),
  getById: (id) => api.get(`/api/drivers/${id}`),
  create: (data) => api.post('/api/drivers', data),
  update: (id, data) => api.patch(`/api/drivers/${id}`, data),
  remove: (id) => api.delete(`/api/drivers/${id}`),
  assignVehicle: (driverId, vehicleId) => api.patch(`/api/drivers/${driverId}/assign/${vehicleId}`),
  unassign: (driverId) => api.patch(`/api/drivers/${driverId}/unassign`),
}
