import api from './axios'

export const simulationApi = {
  start: (data) => api.post('/api/simulation/start', data),
  stop: (driveLogId, data = {}) => api.patch(`/api/simulation/${driveLogId}/stop`, data),
  createPlateEvent: (data) => api.post('/api/simulation/plate-events', data),
  createRestEvent: (data) => api.post('/api/simulation/rest-events', data),
  createFatigueEvent: (data) => api.post('/api/simulation/fatigue-events', data),
}
