<script setup>
import { ref, reactive, computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { useBoardStore } from '@/stores/boardStore'

const boardStore = useBoardStore()

const STATUS_LIST = ['접수', '검토 중', '답변 완료', '확인']
const categories  = ['전체', '일반 문의', '기술 문의', '피드백', '기타']

// 필터
const filterCat    = ref('전체')
const filterStatus = ref('전체')

const allStatuses = computed(() => ['전체', ...STATUS_LIST])

const filtered = computed(() => {
  return boardStore.posts.filter(p => {
    const catOk    = filterCat.value    === '전체' || p.category === filterCat.value
    const statusOk = filterStatus.value === '전체' || p.status   === filterStatus.value
    return catOk && statusOk
  })
})

// 상태 색상
function statusColor(s) {
  if (s === '답변 완료') return 'var(--ok)'
  if (s === '검토 중')   return 'var(--warn)'
  if (s === '접수')      return 'var(--accent)'
  return 'var(--text-3)'
}

// 상세 / 답변 모달
const selected  = ref(null)
const replyDraft = ref('')
const saving     = ref(false)

function openDetail(post) {
  selected.value  = post
  replyDraft.value = post.reply || ''
}

function closeDetail() {
  selected.value = null
  replyDraft.value = ''
}

function changeStatus(id, status) {
  boardStore.updateStatus(id, status)
  if (selected.value?.id === id) selected.value = boardStore.posts.find(p => p.id === id)
}

function saveReply() {
  if (!selected.value) return
  saving.value = true
  setTimeout(() => {
    boardStore.updateReply(selected.value.id, replyDraft.value.trim())
    selected.value = boardStore.posts.find(p => p.id === selected.value.id)
    saving.value = false
  }, 300)
}

// 통계
const stats = computed(() => ({
  total:    boardStore.posts.length,
  pending:  boardStore.posts.filter(p => p.status === '접수').length,
  review:   boardStore.posts.filter(p => p.status === '검토 중').length,
  answered: boardStore.posts.filter(p => p.status === '답변 완료').length,
}))
</script>

<template>
  <div class="fade-up view">

    <!-- 페이지 헤더 -->
    <div class="pg-header">
      <div>
        <div class="eyebrow">게시판 관리</div>
        <h1 class="pg-title">문의 관리</h1>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-label mono">전체</div>
        <div class="stat-val">{{ stats.total }}</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-label mono">접수</div>
        <div class="stat-val" style="color: var(--accent);">{{ stats.pending }}</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-label mono">검토 중</div>
        <div class="stat-val" style="color: var(--warn);">{{ stats.review }}</div>
      </div>
      <div class="stat-card ok">
        <div class="stat-label mono">답변 완료</div>
        <div class="stat-val" style="color: var(--ok);">{{ stats.answered }}</div>
      </div>
    </div>

    <!-- 필터 영역 -->
    <div class="filter-area">
      <div class="filter-group">
        <span class="filter-label mono">카테고리</span>
        <div class="filter-pills">
          <button
            v-for="c in categories" :key="c"
            class="pill" :class="{ active: filterCat === c }"
            @click="filterCat = c"
          >{{ c }}</button>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-label mono">상태</span>
        <div class="filter-pills">
          <button
            v-for="s in allStatuses" :key="s"
            class="pill" :class="{ active: filterStatus === s }"
            @click="filterStatus = s"
          >{{ s }}</button>
        </div>
      </div>
      <span class="mono" style="margin-left:auto;font-size: 14px;color: var(--text-3);align-self:flex-end;">{{ filtered.length }}건</span>
    </div>

    <!-- 목록 테이블 -->
    <div class="table-wrap">
      <div class="tbl-header">
        <span style="flex:1;">제목</span>
        <span style="width:90px;text-align:center;">카테고리</span>
        <span style="width:90px;text-align:center;">상태 변경</span>
        <span style="width:60px;text-align:center;">작성자</span>
        <span style="width:90px;text-align:right;">날짜</span>
        <span style="width:70px;text-align:center;">답변</span>
      </div>

      <div style="display:flex;flex-direction:column;gap:3px;">
        <div
          v-for="p in filtered" :key="p.id"
          class="tbl-row"
          :class="{ 'has-reply': p.reply }"
        >
          <!-- 제목 (클릭 시 상세) -->
          <span class="post-subject" @click="openDetail(p)">
            <span v-if="p.reply" class="reply-dot" title="답변 완료">●</span>
            {{ p.subject }}
          </span>

          <!-- 카테고리 -->
          <span style="width:90px;text-align:center;">
            <span class="chip chip-mute">{{ p.category }}</span>
          </span>

          <!-- 상태 드롭다운 -->
          <span style="width:90px;text-align:center;">
            <select
              class="status-select"
              :value="p.status"
              :style="{ color: statusColor(p.status) }"
              @change="changeStatus(p.id, $event.target.value)"
            >
              <option v-for="s in STATUS_LIST" :key="s" :value="s">{{ s }}</option>
            </select>
          </span>

          <!-- 작성자 -->
          <span style="width:60px;text-align:center;font-size: 14px;color: var(--text-3);">{{ p.name }}</span>

          <!-- 날짜 -->
          <span class="mono" style="width:90px;text-align:right;font-size: 14px;color: var(--text-3);">{{ p.date }}</span>

          <!-- 답변 버튼 -->
          <span style="width:70px;text-align:center;">
            <button class="btn-reply" @click="openDetail(p)">
              {{ p.reply ? '수정' : '답변' }}
            </button>
          </span>
        </div>

        <div v-if="filtered.length === 0" class="empty-state">
          <AppIcon name="list" :size="28" />
          조건에 맞는 문의가 없습니다.
        </div>
      </div>
    </div>

    <!-- ══ 상세 / 답변 모달 ══ -->
    <Teleport to="body">
      <div v-if="selected" class="modal-overlay" @click.self="closeDetail">
        <div class="modal-box">

          <!-- 모달 헤더 -->
          <div class="modal-head">
            <div style="flex:1;min-width:0;">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px;flex-wrap:wrap;">
                <span class="chip chip-mute">{{ selected.category }}</span>
                <select
                  class="status-select"
                  :value="selected.status"
                  :style="{ color: statusColor(selected.status) }"
                  @change="changeStatus(selected.id, $event.target.value)"
                >
                  <option v-for="s in STATUS_LIST" :key="s" :value="s">● {{ s }}</option>
                </select>
              </div>
              <div style="font-size:17px;font-weight:700;color: var(--text-1);">{{ selected.subject }}</div>
              <div class="mono" style="font-size: 14px;color: var(--text-3);margin-top:5px;">
                {{ selected.name }}
                <span v-if="selected.email"> · {{ selected.email }}</span>
                · {{ selected.date }}
              </div>
            </div>
            <button class="icon-close" @click="closeDetail"><AppIcon name="x" :size="16" /></button>
          </div>

          <!-- 문의 내용 -->
          <div style="padding:20px;font-size: 14px;color: var(--text-2);line-height:1.75;white-space:pre-wrap;border-bottom:1px solid var(--line-1);">{{ selected.content }}</div>

          <!-- 답변 작성 -->
          <div class="reply-area">
            <div class="reply-area-label mono">
              <AppIcon name="arrow" :size="12" />
              관리자 답변
              <span v-if="selected.replyDate" style="margin-left:auto;font-size: 14px;color: var(--text-3);">최종 수정 {{ selected.replyDate }}</span>
            </div>
            <textarea
              v-model="replyDraft"
              class="reply-textarea"
              placeholder="답변 내용을 입력하세요. 저장하면 상태가 자동으로 '답변 완료'로 변경됩니다."
            />
            <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:10px;">
              <button class="btn-cancel" @click="closeDetail">닫기</button>
              <button class="btn-save" :disabled="saving || !replyDraft.trim()" @click="saveReply">
                <AppIcon name="check" :size="13" />
                {{ saving ? '저장 중...' : '답변 저장' }}
              </button>
            </div>
          </div>

        </div>
      </div>
    </Teleport>

  </div>
</template>

<style scoped>
.view { padding: 32px 32px 40px; }

.eyebrow { font-family: var(--font-mono); font-size: 14px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); margin-bottom: 6px; }
.pg-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
.pg-title  { font-size: 24px; font-weight: 700; margin: 0; letter-spacing: 0; color: var(--text-1); }

/* 통계 카드 */
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.stat-card {
  background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg);
  padding: 16px 20px; display: flex; flex-direction: column; gap: 6px;
}
.stat-card.accent { background: var(--accent-soft); border-color: var(--accent-line); }
.stat-card.warn   { background: var(--warn-soft);   border-color: rgba(197,138,58,0.25); }
.stat-card.ok     { background: var(--ok-soft);     border-color: rgba(94,138,111,0.25); }
.stat-label { font-size: 14px; letter-spacing: 0.1em; color: var(--text-3); text-transform: uppercase; }
.stat-val   { font-size: 28px; font-weight: 800; color: var(--text-1); line-height: 1; }

/* 필터 */
.filter-area  { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 14px; margin-bottom: 16px; }
.filter-group { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.filter-label { font-size: 14px; letter-spacing: 0.1em; color: var(--text-3); text-transform: uppercase; white-space: nowrap; }
.filter-pills { display: flex; gap: 5px; flex-wrap: wrap; }
.pill {
  padding: 4px 11px; border-radius: 999px; font-size: 14px; font-weight: 500;
  color: var(--text-3); background: var(--bg-2); border: 1px solid var(--line-2);
  cursor: pointer; transition: all 0.15s;
}
.pill:hover  { background: var(--bg-3); color: var(--text-1); }
.pill.active { background: var(--accent-soft); color: var(--accent); border-color: var(--accent-line); }

/* 테이블 */
.table-wrap { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); overflow: hidden; }
.tbl-header {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 18px;
  font-size: 16px; font-weight: 600; color: var(--text-3);
  font-family: var(--font-mono); letter-spacing: 0.06em; text-transform: uppercase;
  border-bottom: 1px solid var(--line-1);
  background: var(--bg-3);
}
.tbl-row {
  display: flex; align-items: center; gap: 12px;
  padding: 13px 18px;
  border-bottom: 1px solid var(--line-1);
  transition: background 0.12s;
}
.tbl-row:last-child { border-bottom: none; }
.tbl-row:hover { background: var(--bg-3); }
.tbl-row.has-reply { border-left: 3px solid var(--ok); }

.post-subject {
  flex: 1; font-size: 14px; font-weight: 600; color: var(--text-1);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  cursor: pointer; display: flex; align-items: center; gap: 6px;
}
.post-subject:hover { color: var(--accent); }
.reply-dot { color: var(--ok); font-size: 14px; flex-shrink: 0; }

/* 상태 드롭다운 */
.status-select {
  background: transparent; border: none; outline: none;
  font-size: 14px; font-weight: 700; cursor: pointer;
  font-family: var(--font-mono); padding: 2px 4px;
}

/* Chips */
.chip { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 999px; font-size: 14px; font-weight: 700; }
.chip-mute { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }

/* 답변 버튼 */
.btn-reply {
  padding: 4px 10px; border-radius: var(--r-sm);
  font-size: 14px; font-weight: 600; cursor: pointer;
  background: var(--accent-soft); color: var(--accent);
  border: 1px solid var(--accent-line);
  transition: all 0.15s; white-space: nowrap;
}
.btn-reply:hover { background: var(--accent); color: var(--accent-ink); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 64px; color: var(--text-3); font-size: 14px; }

/* 모달 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.55); display: flex; align-items: center; justify-content: center; z-index: 200; padding: 24px; }
.modal-box {
  background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg);
  width: 100%; max-width: 620px; box-shadow: 0 24px 60px rgba(0,0,0,0.4); overflow: hidden;
  max-height: 90vh; display: flex; flex-direction: column;
}
.modal-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; padding: 18px 20px; border-bottom: 1px solid var(--line-1); flex-shrink: 0; }
.icon-close { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: var(--r-sm); color: var(--text-3); cursor: pointer; flex-shrink: 0; transition: background 0.15s, color 0.15s; }
.icon-close:hover { background: var(--bg-3); color: var(--text-1); }

/* 답변 영역 */
.reply-area { padding: 16px 20px; flex-shrink: 0; }
.reply-area-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; font-weight: 700; color: var(--accent);
  margin-bottom: 10px; letter-spacing: 0.08em; text-transform: uppercase;
}
.reply-textarea {
  width: 100%; min-height: 120px; resize: vertical;
  background: var(--bg-1); border: 1px solid var(--line-2); border-radius: var(--r-md);
  padding: 10px 12px; font-size: 14px; color: var(--text-1);
  font-family: var(--font-sans); outline: none; box-sizing: border-box;
  transition: border-color 0.15s;
}
.reply-textarea:focus { border-color: var(--accent); box-shadow: 0 0 0 2px var(--accent-soft); }
.reply-textarea::placeholder { color: var(--text-3); }

.btn-save {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 8px 18px; background: var(--accent); border: none;
  border-radius: var(--r-md); font-size: 14px; font-weight: 600;
  color: var(--accent-ink); cursor: pointer; transition: background 0.15s;
}
.btn-save:hover:not(:disabled) { background: var(--accent-hover); }
.btn-save:disabled { opacity: 0.45; cursor: not-allowed; }
.btn-cancel { display: inline-flex; align-items: center; padding: 8px 16px; background: transparent; border: 1px solid var(--line-2); border-radius: var(--r-md); font-size: 14px; font-weight: 500; color: var(--text-3); cursor: pointer; transition: background 0.15s; }
.btn-cancel:hover { background: var(--bg-3); }

.mono { font-family: var(--font-mono); }

.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
