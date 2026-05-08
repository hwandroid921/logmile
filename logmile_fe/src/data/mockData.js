/* logmile · 공통 mock 데이터 (백엔드 API 연동 전 임시)
   - 차량 10대 / 운전자 10명 / 시나리오 A·B·C
   - 피로 점수 0=안전, 100=위험 (NORMAL 0~39 / CAUTION 40~69 / DANGER ≥70)
*/

export const company = {
  id: 1,
  name: '한라물류센터',
  address: '경기 성남시 분당구 야탑동 343',
  phone: '031-705-2840',
  isActive: true,
}

export const vehicles = [
  { id:1, plate:'경기 80바 1024', driver:'김민수', driverId:1, type:'카고 5톤',     license:'1종 대형', score:78, level:'DANGER',  status:'RUNNING',   spd:87,  contMin:384, dailyMin:672, nightMin:204, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'경부고속 · 안성IC', startedAt:'03:18', scenario:'C', driveLogId:'DL-2026-0438' },
  { id:2, plate:'경기 80바 1025', driver:'박정호', driverId:2, type:'윙바디 11톤',  license:'1종 대형', score:56, level:'CAUTION', status:'RUNNING',   spd:78,  contMin:270, dailyMin:480, nightMin:108, restValid:2, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 서산IC',   startedAt:'06:11', scenario:'B', driveLogId:'DL-2026-0437' },
  { id:3, plate:'경기 80바 1026', driver:'이영준', driverId:3, type:'카고 4.5톤',   license:'1종 보통', score:18, level:'NORMAL',  status:'RUNNING',   spd:92,  contMin:108, dailyMin:186, nightMin:0,   restValid:0, restSuff:1, restInvalid:0, restMiss:0, loc:'중부고속 · 음성IC', startedAt:'11:02', scenario:'A', driveLogId:'DL-2026-0436' },
  { id:4, plate:'경기 80바 1027', driver:'최성훈', driverId:4, type:'윙바디 8톤',   license:'1종 대형', score:32, level:'NORMAL',  status:'RUNNING',   spd:81,  contMin:180, dailyMin:342, nightMin:30,  restValid:1, restSuff:1, restInvalid:0, restMiss:0, loc:'영동고속 · 여주IC', startedAt:'08:50', scenario:'A', driveLogId:'DL-2026-0435' },
  { id:5, plate:'경기 80바 1028', driver:'정우석', driverId:5, type:'카고 5톤',     license:'1종 대형', score:12, level:'NORMAL',  status:'RUNNING',   spd:76,  contMin:60,  dailyMin:114, nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'남해고속 · 진주IC', startedAt:'12:39', scenario:'A', driveLogId:'DL-2026-0434' },
  { id:6, plate:'경기 80바 1029', driver:'강지훈', driverId:6, type:'카고 5톤',     license:'1종 보통', score:84, level:'DANGER',  status:'RUNNING',   spd:64,  contMin:342, dailyMin:618, nightMin:168, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'중부내륙 · 점촌IC', startedAt:'04:02', scenario:'C', driveLogId:'DL-2026-0433' },
  { id:7, plate:'경기 80바 1030', driver:'한승연', driverId:7, type:'카고 2.5톤',   license:'1종 보통', score:48, level:'CAUTION', status:'RUNNING',   spd:71,  contMin:228, dailyMin:384, nightMin:72,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서울외곽 · 송내IC', startedAt:'07:45', scenario:'B', driveLogId:'DL-2026-0432' },
  { id:8, plate:'경기 80바 1031', driver:'조영민', driverId:8, type:'윙바디 11톤',  license:'1종 대형', score:41, level:'CAUTION', status:'RUNNING',   spd:84,  contMin:252, dailyMin:426, nightMin:48,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 당진IC',   startedAt:'06:50', scenario:'B', driveLogId:'DL-2026-0431' },
  { id:9, plate:'경기 80바 1032', driver:'윤태경', driverId:9, type:'카고 5톤',     license:'1종 대형', score:0,  level:'NORMAL',  status:'IDLE',      spd:0,   contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지', startedAt:'—',     scenario:'—', driveLogId:null },
  { id:10,plate:'경기 80바 1033', driver:'서동현', driverId:10,type:'윙바디 8톤',   license:'1종 보통', score:0,  level:'NORMAL',  status:'IDLE',      spd:0,   contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지', startedAt:'—',     scenario:'—', driveLogId:null },
]

export const drivers = [
  { id:1,  name:'김민수', phone:'010-2241-9087', license:'1종 대형', vehicleId:1,  plate:'경기 80바 1024', score:78, level:'DANGER',  status:'RUNNING',   joinedAt:'2024.03.12', employedAt:'2024.03.15' },
  { id:2,  name:'박정호', phone:'010-3387-2210', license:'1종 대형', vehicleId:2,  plate:'경기 80바 1025', score:56, level:'CAUTION', status:'RUNNING',   joinedAt:'2024.05.20', employedAt:'2024.05.22' },
  { id:3,  name:'이영준', phone:'010-9921-3344', license:'1종 보통', vehicleId:3,  plate:'경기 80바 1026', score:18, level:'NORMAL',  status:'RUNNING',   joinedAt:'2023.11.08', employedAt:'2023.11.10' },
  { id:4,  name:'최성훈', phone:'010-7745-1118', license:'1종 대형', vehicleId:4,  plate:'경기 80바 1027', score:32, level:'NORMAL',  status:'RUNNING',   joinedAt:'2023.08.01', employedAt:'2023.08.05' },
  { id:5,  name:'정우석', phone:'010-2294-7765', license:'1종 대형', vehicleId:5,  plate:'경기 80바 1028', score:12, level:'NORMAL',  status:'RUNNING',   joinedAt:'2025.01.14', employedAt:'2025.01.16' },
  { id:6,  name:'강지훈', phone:'010-4471-9920', license:'1종 보통', vehicleId:6,  plate:'경기 80바 1029', score:84, level:'DANGER',  status:'RUNNING',   joinedAt:'2024.07.30', employedAt:'2024.08.01' },
  { id:7,  name:'한승연', phone:'010-3367-8841', license:'1종 보통', vehicleId:7,  plate:'경기 80바 1030', score:48, level:'CAUTION', status:'RUNNING',   joinedAt:'2024.09.11', employedAt:'2024.09.13' },
  { id:8,  name:'조영민', phone:'010-2218-7726', license:'1종 대형', vehicleId:8,  plate:'경기 80바 1031', score:41, level:'CAUTION', status:'RUNNING',   joinedAt:'2024.02.27', employedAt:'2024.03.01' },
  { id:9,  name:'윤태경', phone:'010-8841-2207', license:'1종 대형', vehicleId:9,  plate:'경기 80바 1032', score:0,  level:'NORMAL',  status:'IDLE',      joinedAt:'2023.06.15', employedAt:'2023.06.18' },
  { id:10, name:'서동현', phone:'010-2270-1145', license:'1종 보통', vehicleId:10, plate:'경기 80바 1033', score:0,  level:'NORMAL',  status:'IDLE',      joinedAt:'2025.03.04', employedAt:'2025.03.06' },
]

export const thresholds = [
  { key:'CONTINUOUS_DRIVING_90',           value:90,  delta:+10, group:'continuous', label:'연속 운행 90분',   description:'연속 운행 90분 이상 → +10점 (주의 시작)',          updatedAt:'2026.04.21' },
  { key:'CONTINUOUS_DRIVING_120',          value:120, delta:+25, group:'continuous', label:'연속 운행 120분',  description:'연속 운행 2시간 이상 → +25점 (휴식 권고)',        updatedAt:'2026.04.21' },
  { key:'CONTINUOUS_DRIVING_180',          value:180, delta:+45, group:'continuous', label:'연속 운행 180분',  description:'연속 운행 3시간 이상 → +45점 (위험 진입)',        updatedAt:'2026.04.21' },
  { key:'CONTINUOUS_DRIVING_240',          value:240, delta:+65, group:'continuous', label:'연속 운행 240분',  description:'연속 운행 4시간 이상 → +65점 (즉시 휴게 필요)',   updatedAt:'2026.04.21' },
  { key:'DAILY_DRIVING_360',               value:360, delta:+15, group:'daily',      label:'일일 운행 6h',     description:'당일 누적 운행 6시간 이상 → +15점',                updatedAt:'2026.04.21' },
  { key:'DAILY_DRIVING_480',               value:480, delta:+30, group:'daily',      label:'일일 운행 8h',     description:'당일 누적 운행 8시간 이상 → +30점',                updatedAt:'2026.04.21' },
  { key:'DAILY_DRIVING_600',               value:600, delta:+45, group:'daily',      label:'일일 운행 10h',    description:'당일 누적 운행 10시간 이상 → +45점',               updatedAt:'2026.04.21' },
  { key:'NIGHT_DRIVING_30',                value:30,  delta:+10, group:'night',      label:'야간 30분',        description:'야간 운행 누적 30분 이상 → +10점',                 updatedAt:'2026.04.21' },
  { key:'NIGHT_DRIVING_60',                value:60,  delta:+20, group:'night',      label:'야간 1h',          description:'야간 운행 누적 1시간 이상 → +20점',                updatedAt:'2026.04.21' },
  { key:'NIGHT_DRIVING_120',               value:120, delta:+35, group:'night',      label:'야간 2h',          description:'야간 운행 누적 2시간 이상 → +35점',                updatedAt:'2026.04.21' },
  { key:'REST_VALID_MINUTES',              value:15,  delta:0,   group:'rest',       label:'유효 휴식 기준',   description:'speed≤3 상태 15분 이상 → 유효 휴식 (VALID)',       updatedAt:'2026.04.21' },
  { key:'REST_SUFFICIENT_MINUTES',         value:30,  delta:0,   group:'rest',       label:'충분 휴식 기준',   description:'speed≤3 상태 30분 이상 → 충분 휴식 (SUFFICIENT)',  updatedAt:'2026.04.21' },
  { key:'REST_REQUIRED_AFTER',             value:120, delta:0,   group:'rest',       label:'휴식 필요 시점',   description:'연속 2시간 운행 후 15분 이상 휴식 필요',           updatedAt:'2026.04.21' },
  { key:'REST_VIOLATION_ONCE_SCORE',       value:1,   delta:+10, group:'rest',       label:'휴식 누락 1회',    description:'필요 시점 휴식 누락 1회 → +10점',                  updatedAt:'2026.04.21' },
  { key:'REST_VIOLATION_TWICE_SCORE',      value:2,   delta:+25, group:'rest',       label:'휴식 누락 2회+',   description:'필요 시점 휴식 누락 2회 이상 → +25점',             updatedAt:'2026.04.21' },
  { key:'REST_CORRECTION_VALID_SCORE',     value:15,  delta:-10, group:'rest',       label:'유효 휴식 보정',   description:'유효 휴식 1회당 → -10점 (피로 회복)',              updatedAt:'2026.04.21' },
  { key:'REST_CORRECTION_SUFFICIENT_SCORE',value:30,  delta:-20, group:'rest',       label:'충분 휴식 보정',   description:'충분 휴식 1회당 → -20점 (피로 회복)',              updatedAt:'2026.04.21' },
  { key:'LEVEL_NORMAL_MAX',                value:39,  delta:0,   group:'level',      label:'정상 등급 최대',   description:'점수 0~39 → 정상 (NORMAL)',                        updatedAt:'2026.04.21' },
  { key:'LEVEL_CAUTION_MIN',               value:40,  delta:0,   group:'level',      label:'주의 등급 최소',   description:'점수 40 이상 → 주의 진입 (CAUTION)',               updatedAt:'2026.04.21' },
  { key:'LEVEL_CAUTION_MAX',               value:69,  delta:0,   group:'level',      label:'주의 등급 최대',   description:'점수 40~69 → 주의 (CAUTION)',                      updatedAt:'2026.04.21' },
  { key:'LEVEL_DANGER_MIN',                value:70,  delta:0,   group:'level',      label:'위험 등급 최소',   description:'점수 70 이상 → 위험 진입 (DANGER) · 전화 권고',    updatedAt:'2026.04.21' },
]

export const driveLogs = [
  { id:'DL-2026-0438', plate:'경기 80바 1024', driver:'김민수', startedAt:'2026.05.04 03:18', endedAt:null,                totalMin:672, nightMin:204, restCount:1, peak:78, level:'DANGER',  scenario:'C', status:'RUNNING',   ocrConf:0.97, manual:false },
  { id:'DL-2026-0437', plate:'경기 80바 1025', driver:'박정호', startedAt:'2026.05.04 06:11', endedAt:null,                totalMin:480, nightMin:108, restCount:2, peak:56, level:'CAUTION', scenario:'B', status:'RUNNING',   ocrConf:0.94, manual:false },
  { id:'DL-2026-0436', plate:'경기 80바 1026', driver:'이영준', startedAt:'2026.05.04 11:02', endedAt:null,                totalMin:186, nightMin:0,   restCount:1, peak:18, level:'NORMAL',  scenario:'A', status:'RUNNING',   ocrConf:0.99, manual:false },
  { id:'DL-2026-0430', plate:'경기 80바 1027', driver:'최성훈', startedAt:'2026.05.03 19:24', endedAt:'2026.05.04 02:05', totalMin:401, nightMin:246, restCount:2, peak:67, level:'CAUTION', scenario:'B', status:'COMPLETED', ocrConf:0.91, manual:false },
  { id:'DL-2026-0429', plate:'경기 80바 1028', driver:'정우석', startedAt:'2026.05.03 14:11', endedAt:'2026.05.03 23:45', totalMin:574, nightMin:105, restCount:3, peak:72, level:'DANGER',  scenario:'C', status:'COMPLETED', ocrConf:0.96, manual:false },
  { id:'DL-2026-0428', plate:'경기 80바 1029', driver:'강지훈', startedAt:'2026.05.03 09:20', endedAt:'2026.05.03 17:02', totalMin:462, nightMin:0,   restCount:2, peak:38, level:'NORMAL',  scenario:'A', status:'COMPLETED', ocrConf:0.98, manual:false },
  { id:'DL-2026-0427', plate:'경기 80바 1030', driver:'한승연', startedAt:'2026.05.02 22:00', endedAt:'2026.05.03 05:48', totalMin:468, nightMin:372, restCount:1, peak:64, level:'CAUTION', scenario:'B', status:'COMPLETED', ocrConf:0.82, manual:true  },
  { id:'DL-2026-0426', plate:'경기 80바 1031', driver:'조영민', startedAt:'2026.05.02 14:30', endedAt:'2026.05.02 18:14', totalMin:224, nightMin:0,   restCount:1, peak:22, level:'NORMAL',  scenario:'A', status:'COMPLETED', ocrConf:0.95, manual:false },
  { id:'DL-2026-0425', plate:'경기 80바 1024', driver:'김민수', startedAt:'2026.05.02 06:00', endedAt:'2026.05.02 14:18', totalMin:498, nightMin:0,   restCount:2, peak:51, level:'CAUTION', scenario:'B', status:'COMPLETED', ocrConf:0.93, manual:false },
  { id:'DL-2026-0424', plate:'경기 80바 1025', driver:'박정호', startedAt:'2026.05.01 21:00', endedAt:'2026.05.02 04:42', totalMin:462, nightMin:348, restCount:2, peak:75, level:'DANGER',  scenario:'C', status:'COMPLETED', ocrConf:0.89, manual:false },
  { id:'DL-2026-0423', plate:'경기 80바 1029', driver:'강지훈', startedAt:'2026.05.01 12:00', endedAt:'2026.05.01 15:32', totalMin:212, nightMin:0,   restCount:0, peak:28, level:'NORMAL',  scenario:'A', status:'STOPPED',   ocrConf:0.94, manual:false },
]

export const plateTimeline = [
  { source:'DEPARTURE',      t:'03:18', loc:'한라물류 차고지',    conf:0.99, matched:true  },
  { source:'HIGHWAY_CCTV',   t:'04:22', loc:'경부고속 안성 상행', conf:0.96, matched:true  },
  { source:'REST_AREA_CCTV', t:'05:10', loc:'안성 휴게소 입장',   conf:0.93, matched:true  },
  { source:'REST_AREA_CCTV', t:'05:44', loc:'안성 휴게소 출장',   conf:0.91, matched:true  },
  { source:'HIGHWAY_CCTV',   t:'07:30', loc:'경부고속 오산 상행', conf:0.97, matched:true  },
  { source:'ARRIVAL',        t:'09:50', loc:'수원 물류센터',       conf:0.99, matched:true  },
  { source:'DEPARTURE',      t:'11:05', loc:'수원 물류센터',       conf:0.98, matched:true  },
]

export const restEvents = [
  { id:'RE-1', drive:'DL-2026-0438', startAt:'05:08', endAt:'05:37', minutes:29, type:'VALID'      },
  { id:'RE-2', drive:'DL-2026-0438', startAt:'09:14', endAt:'09:52', minutes:38, type:'SUFFICIENT' },
  { id:'RE-3', drive:'DL-2026-0437', startAt:'08:40', endAt:'09:12', minutes:32, type:'SUFFICIENT' },
  { id:'RE-4', drive:'DL-2026-0437', startAt:'11:22', endAt:'11:39', minutes:17, type:'VALID'      },
  { id:'RE-5', drive:'DL-2026-0436', startAt:'13:28', endAt:'14:08', minutes:40, type:'SUFFICIENT' },
]

export const alerts = [
  { t:'14:32', v:'경기 80바 1024', d:'연속 6.4h · 야간 3.4h · 점수 78 → DANGER',      action:'전화 권고 발송',     sev:'danger' },
  { t:'14:18', v:'경기 80바 1029', d:'일일 10.3h · 휴식 누락 2회 · 점수 84 → DANGER', action:'즉시 휴게 안내',     sev:'danger' },
  { t:'14:09', v:'경기 80바 1025', d:'연속 4.5h · 점수 56 → CAUTION 유지',             action:'휴식 권고',          sev:'warn'   },
  { t:'13:47', v:'경기 80바 1030', d:'연속 3.8h · 휴식 누락 1회 · 점수 48 → CAUTION', action:'휴식 권고',          sev:'warn'   },
  { t:'12:55', v:'경기 80바 1026', d:'충분 휴식(34분) · -20점 보정 · 점수 18 (NORMAL)', action:'정상 운행 유지',    sev:'info'   },
  { t:'12:14', v:'경기 80바 1028', d:'DEPARTURE · OCR 0.99 · 차고지 출발',              action:'운행 등록 완료',     sev:'info'   },
]

export const days = (() => {
  const patterns = [
    {dr:198,nt:18,miss:0,sc:24,dn:0},{dr:212,nt:24,miss:1,sc:31,dn:1},
    {dr:228,nt:30,miss:1,sc:34,dn:1},{dr:184,nt:12,miss:0,sc:22,dn:0},
    {dr:241,nt:36,miss:1,sc:38,dn:1},{dr:266,nt:48,miss:2,sc:46,dn:2},
    {dr:294,nt:72,miss:3,sc:54,dn:3},{dr:248,nt:42,miss:2,sc:42,dn:2},
    {dr:222,nt:30,miss:1,sc:35,dn:1},{dr:204,nt:24,miss:1,sc:32,dn:1},
    {dr:268,nt:60,miss:2,sc:51,dn:2},{dr:286,nt:84,miss:3,sc:58,dn:3},
    {dr:312,nt:108,miss:4,sc:64,dn:4},{dr:298,nt:96,miss:3,sc:61,dn:3},
  ]
  return patterns.map((p,i) => {
    const d = new Date(2026, 4, 4 - 13 + i)
    return {
      date: `${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`,
      driveHours: p.dr, nightHours: p.nt, restMiss: p.miss, avgScore: p.sc, danger: p.dn,
    }
  })
})()

// helpers
export function levelOf(score) {
  if (score >= 70) return { label:'DANGER',  color:'var(--danger)', chipClass:'chip chip-danger' }
  if (score >= 40) return { label:'CAUTION', color:'var(--warn)',   chipClass:'chip chip-warn'   }
  return                  { label:'NORMAL',  color:'var(--ok)',     chipClass:'chip chip-ok'     }
}

export function fmtMin(m) {
  if (!m && m !== 0) return '-'
  const h = Math.floor(m / 60)
  const mn = m % 60
  return h > 0 ? `${h}h ${mn}m` : `${mn}m`
}
