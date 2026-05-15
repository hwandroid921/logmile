import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useBoardStore = defineStore('board', () => {
  const posts = ref([
    {
      id: 5, name: '김**', email: 'kim@example.com',
      category: '기술 문의', subject: '번호판 인식 정확도가 낮습니다',
      content: '야간 CCTV 환경에서 OCR 인식률이 떨어지는 것 같습니다. 개선 예정이 있나요?',
      date: '2026.05.10', status: '답변 완료',
      reply: 'YOLO11n 모델의 저조도 환경 성능 개선을 위해 데이터 증강 및 재훈련을 검토 중입니다. 빠른 시일 내 개선 예정입니다.',
      replyDate: '2026.05.11',
    },
    {
      id: 4, name: '박**', email: 'park@example.com',
      category: '피드백',    subject: '피로도 점수 기준이 궁금합니다',
      content: '0~100점 기준에서 40점이 주의 기준인데 어떻게 산정되었는지 자료가 있을까요?',
      date: '2026.05.09', status: '답변 완료',
      reply: '피로도 점수는 연속 운행 시간, 속도 변동성, 급감속 횟수, 야간 운행 여부 등을 가중합산하여 산출합니다. 상세 산출식은 기술 문서에서 확인하실 수 있습니다.',
      replyDate: '2026.05.10',
    },
    {
      id: 3, name: '이**', email: 'lee@example.com',
      category: '일반 문의', subject: '소스코드를 공유해주실 수 있나요?',
      content: '프로젝트 레포지토리 링크가 있으면 공유해 주시면 감사하겠습니다.',
      date: '2026.05.07', status: '검토 중',
      reply: '',
      replyDate: '',
    },
    {
      id: 2, name: '최**', email: 'choi@example.com',
      category: '기술 문의', subject: 'API 응답 포맷 문서가 있나요?',
      content: 'Spring Boot REST API의 응답 포맷이나 Swagger 문서 위치를 알고 싶습니다.',
      date: '2026.05.05', status: '답변 완료',
      reply: 'Swagger UI는 /swagger-ui/index.html 경로에서 확인하실 수 있습니다. 로컬 실행 후 접속해 보세요.',
      replyDate: '2026.05.06',
    },
    {
      id: 1, name: '정**', email: 'jeong@example.com',
      category: '피드백',    subject: '대시보드 UI가 깔끔합니다',
      content: '디자인이 전체적으로 세련되고 가독성이 좋습니다. 좋은 프로젝트 완성하세요!',
      date: '2026.05.01', status: '확인',
      reply: '',
      replyDate: '',
    },
  ])

  function addPost(post) {
    posts.value.unshift({
      ...post,
      id: Date.now(),
      status: '접수',
      reply: '',
      replyDate: '',
    })
  }

  function updateStatus(id, status) {
    const post = posts.value.find(p => p.id === id)
    if (post) post.status = status
  }

  function updateReply(id, reply) {
    const post = posts.value.find(p => p.id === id)
    if (post) {
      post.reply = reply
      post.replyDate = new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace('.', '.')
      post.status = '답변 완료'
    }
  }

  return { posts, addPost, updateStatus, updateReply }
})
