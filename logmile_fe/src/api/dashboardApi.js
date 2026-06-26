import api from './axios'

export const dashboardApi = {
  getSummary:   (params = {}) => api.get('/api/dashboard/summary', { params }),
  getVehicles:  (params = {}) => api.get('/api/dashboard/vehicles', { params }),
  getEvents:    (params = {}) => api.get('/api/dashboard/events', { params }),
  createAction: (data)        => api.post('/api/dashboard/actions', data),
}
