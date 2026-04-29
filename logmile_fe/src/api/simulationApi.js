import api from './axios'

export const simulationApi = {
  start: (data) => api.post('/api/simulation/start', data),
  stop: (driveLogId) => api.post('/api/simulation/stop', { driveLogId }),
}
