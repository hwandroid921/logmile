import api from './axios'

export const approvalApi = {
  // PENDING 관리자 목록 조회
  getPendingAdmins: () => api.get('/api/admin/approvals/pending'),

  // 가입 승인
  approve: (adminId) => api.patch(`/api/admin/approvals/${adminId}/approve`),

  // 가입 거절
  reject: (adminId) => api.patch(`/api/admin/approvals/${adminId}/reject`),

  // 계정 정지
  suspend: (adminId) => api.patch(`/api/admin/approvals/${adminId}/suspend`),

  // 정지 해제
  unsuspend: (adminId) => api.patch(`/api/admin/approvals/${adminId}/unsuspend`),
}
