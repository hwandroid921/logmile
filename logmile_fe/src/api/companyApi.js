import api from './axios'

export const companyApi = {
  getAll: () => api.get('/api/companies'),
  getById: (companyId) => api.get(`/api/companies/${companyId}`),
  activate: (companyId) => api.patch(`/api/companies/${companyId}/activate`),
  deactivate: (companyId) => api.patch(`/api/companies/${companyId}/deactivate`),
}
