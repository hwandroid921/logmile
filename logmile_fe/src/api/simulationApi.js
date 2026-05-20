import api from './axios'

/**
 * 시뮬레이션 API — 시뮬레이션 뷰에서 사용하는 엔드포인트만 호출한다.
 *
 *   POST   /api/simulation/start                    → DriveLog 생성
 *   POST   /api/simulation/rest-events              → 휴식 이벤트 수동 등록
 *   POST   /api/simulation/fatigue-events           → 피로도 이벤트 수동 등록
 *   PATCH  /api/simulation/{driveLogId}/stop        → 운행 종료
 *
 * NOTE: /api/simulation/plate-events 엔드포인트는 백엔드에 존재하지만
 *       현재 시뮬레이션 뷰는 번호판 관측 이벤트를 별도 저장하지 않고
 *       start() 호출 시 recognizedPlateNo 하나만 전달한다.
 */
export const simulationApi = {
  start: (data) => api.post('/api/simulation/start', data),
  stop: (driveLogId, data = {}) => api.patch(`/api/simulation/${driveLogId}/stop`, data),
  createRestEvent: (data) => api.post('/api/simulation/rest-events', data),
  createFatigueEvent: (data) => api.post('/api/simulation/fatigue-events', data),
}
