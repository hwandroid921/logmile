import api from './axios'

export const dashboardApi = {
  getSummary: () => api.get('/api/dashboard/summary'),
  getVehicles: () => api.get('/api/dashboard/vehicles'),
}
