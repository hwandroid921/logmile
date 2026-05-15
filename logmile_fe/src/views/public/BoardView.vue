<script setup>
import { ref, reactive, computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { useBoardStore } from '@/stores/boardStore'

const boardStore = useBoardStore()
const categories = ['일반 문의', '기술 문의', '피드백', '기타']

const posts = computed(() => boardStore.posts)

// 필터
const filterCat = ref('전체')
const allCats   = computed(() => ['전체', ...categories])
const filtered  = computed(() =>
  filterCat.value === '전체' ? posts.value : posts.value.filter(p => p.category === filterCat.value)
)

function statusColor(s) {
  if (s === '답변 완료') return 'var(--ok)'
  if (s === '검토 중')   return 'var(--warn)'
  if (s === '접수')      return 'var(--accent)'
  return 'var(--text-4)'
}

// 상세 모달
const selected = ref(null)

// 작성 모달
const showWrite = ref(false)
const form = reactive({ name: '', email: '', category: '일반 문의', subject: '', content: '' })
const submitted = ref(false)
const errorMsg  = ref('')

function openWrite() { showWrite.value = true; errorMsg.value = '' }
function closeWrite() { showWrite.value = false }

function validate() {
  if (!form.name.trim())    return '이름을 입력해주세요.'
  if (!form.email.trim())   return '이메일을 입력해주세요.'
  if (!/\S+@\S+\.\S+/.test(form.email)) return '올바른 이메일 형식이 아닙니다.'
  if (!form.subject.trim()) return '제목을 입력해주세요.'
  if (!form.content.trim()) return '내용을 입력해주세요.'
  return ''
}

function submit() {
  const err = validate()
  if (err) { errorMsg.value = err; return }
  errorMsg.value = ''
  boardStore.addPost({
    name: form.name, email: form.email, category: form.category,
    subject: form.subject, content: form.content,
    date: new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace(/\.$/, ''),
  })
  Object.assign(form, { name: '', email: '', category: '일반 문의', subject: '', content: '' })
  closeWrite()
  submitted.value = true
  setTimeout(() => (submitted.value = false), 3000)
}
</script>

<template>
  <div class="fade-up">

    <!-- ══ 헤더 ══ -->
    <section class="section-border" style="padding:48px 0 36px;">
      <div class="page-inner">
        <div class="eyebrow" style="margin-bottom:10px;">BOARD</div>
        <div style="display:flex;justify-content:space-between;align-items:flex-end;gap:16px;">
          <div>
            <h1 class="page-title">문의 게시판</h1>
            <p style="font-size:14px;color:var(--text-3);margin:8px 0 0;">logmile 프로젝트에 대한 질문·피드백·기술 문의를 남겨주세요.</p>
          </div>
          <button class="btn-write" @click="openWrite">
            <AppIcon name="plus" :size="13" /> 문의 작성
          </button>
        </div>
      </div>
    </section>

    <!-- ══ 목록 ══ -->
    <div class="page-inner" style="padding-top:32px;padding-bottom:60px;">

      <!-- 접수 완료 토스트 -->
      <div v-if="submitted" class="toast-ok" style="margin-bottom:16px;">
        <AppIcon name="check" :size="13" /> 문의가 접수되었습니다. 감사합니다!
      </div>

      <!-- 필터 바 -->
      <div class="filter-bar">
        <button v-for="c in allCats" :key="c" class="filter-btn" :class="{ active: filterCat === c }" @click="filterCat = c">{{ c }}</button>
        <span class="mono" style="margin-left:auto;font-size:11px;color:var(--text-4);">{{ filtered.length }}건</span>
      </div>

      <!-- 게시글 목록 헤더 -->
      <div class="list-header">
        <span style="flex:1;">제목</span>
        <span style="width:80px;text-align:center;">카테고리</span>
        <span style="width:80px;text-align:center;">상태</span>
        <span style="width:60px;text-align:center;">작성자</span>
        <span style="width:90px;text-align:right;">날짜</span>
      </div>

      <!-- 게시글 rows -->
      <div style="display:flex;flex-direction:column;gap:4px;">
        <div v-for="p in filtered" :key="p.id" class="post-row" @click="selected = p">
          <span class="post-subject">{{ p.subject }}</span>
          <span style="width:80px;text-align:center;">
            <span class="chip chip-mute">{{ p.category }}</span>
          </span>
          <span style="width:80px;text-align:center;">
            <span class="mono" style="font-size:11px;font-weight:700;" :style="{color:statusColor(p.status)}">● {{ p.status }}</span>
          </span>
          <span style="width:60px;text-align:center;font-size:12px;color:var(--text-4);">{{ p.name }}</span>
          <span class="mono" style="width:90px;text-align:right;font-size:11px;color:var(--text-4);">{{ p.date }}</span>
        </div>
        <div v-if="filtered.length === 0" class="empty-state">
          <AppIcon name="list" :size="28" />
          해당 카테고리의 문의가 없습니다.
        </div>
      </div>
    </div>

    <!-- ══ 상세 모달 ══ -->
    <Teleport to="body">
      <div v-if="selected" class="modal-overlay" @click.self="selected = null">
        <div class="modal-box">
          <div class="modal-head">
            <div>
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px;">
                <span class="chip chip-mute">{{ selected.category }}</span>
                <span class="mono" style="font-size:10.5px;font-weight:700;" :style="{color:statusColor(selected.status)}">● {{ selected.status }}</span>
              </div>
              <div style="font-size:17px;font-weight:700;">{{ selected.subject }}</div>
              <div class="mono" style="font-size:11px;color:var(--text-4);margin-top:5px;">{{ selected.name }} · {{ selected.date }}</div>
            </div>
            <button class="icon-close" @click="selected = null"><AppIcon name="x" :size="16" /></button>
          </div>
          <div style="padding:20px;font-size:14px;color:var(--text-2);line-height:1.75;white-space:pre-wrap;">{{ selected.content }}</div>
          <!-- 관리자 답변 -->
          <div v-if="selected.reply" class="reply-section">
            <div class="reply-label">
              <AppIcon name="arrow" :size="12" /> 관리자 답변
              <span class="mono" style="margin-left:auto;font-size:10.5px;color:var(--text-4);">{{ selected.replyDate }}</span>
            </div>
            <div style="font-size:13.5px;color:var(--text-2);line-height:1.75;white-space:pre-wrap;">{{ selected.reply }}</div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ 문의 작성 모달 ══ -->
    <Teleport to="body">
      <div v-if="showWrite" class="modal-overlay" @click.self="closeWrite">
        <div class="modal-box modal-write">
          <div class="modal-head">
            <div style="font-size:16px;font-weight:700;">문의 작성</div>
            <button class="icon-close" @click="closeWrite"><AppIcon name="x" :size="16" /></button>
          </div>
          <form @submit.prevent="submit" class="form-body">
            <div class="row-2">
              <div class="fgroup">
                <label class="flabel">이름 <span style="color:var(--danger);">*</span></label>
                <input v-model="form.name" type="text" class="finput" placeholder="홍길동" />
              </div>
              <div class="fgroup">
                <label class="flabel">이메일 <span style="color:var(--danger);">*</span></label>
                <input v-model="form.email" type="email" class="finput" placeholder="you@example.com" />
              </div>
            </div>
            <div class="fgroup">
              <label class="flabel">카테고리</label>
              <select v-model="form.category" class="finput">
                <option v-for="c in categories" :key="c">{{ c }}</option>
              </select>
            </div>
            <div class="fgroup">
              <label class="flabel">제목 <span style="color:var(--danger);">*</span></label>
              <input v-model="form.subject" type="text" class="finput" placeholder="문의 제목을 입력해주세요" />
            </div>
            <div class="fgroup">
              <label class="flabel">내용 <span style="color:var(--danger);">*</span></label>
              <textarea v-model="form.content" class="finput ftextarea" placeholder="문의 내용을 상세히 작성해주세요." />
            </div>
            <div v-if="errorMsg" class="err-msg">
              <AppIcon name="info" :size="13" /> {{ errorMsg }}
            </div>
            <div style="display:flex;gap:8px;justify-content:flex-end;">
              <button type="button" class="btn-cancel" @click="closeWrite">취소</button>
              <button type="submit" class="btn-submit">
                <AppIcon name="arrow" :size="13" /> 문의 접수
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<style scoped>
.page-inner { max-width: 1280px; margin: 0 auto; padding: 0 28px; }
.section-border { border-bottom: 1px solid var(--line-1); }
.mono { font-family: var(--font-mono); }
.eyebrow { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); }
.page-title { font-size: 34px; font-weight: 800; margin: 0; letter-spacing: -0.022em; color: var(--text-1); }

/* 작성 버튼 */
.btn-write {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 8px 18px; background: var(--accent); border: none;
  border-radius: var(--r-md); font-size: 13px; font-weight: 600;
  color: var(--accent-ink); cursor: pointer; flex-shrink: 0;
  transition: background 0.15s;
}
.btn-write:hover { background: var(--accent-hover); }

/* 토스트 */
.toast-ok { display: flex; align-items: center; gap: 7px; padding: 10px 16px; background: var(--ok-soft); border: 1px solid rgba(138,186,154,0.3); border-radius: var(--r-md); font-size: 13px; color: var(--ok); font-weight: 500; }

/* 필터 바 */
.filter-bar { display: flex; align-items: center; gap: 6px; margin-bottom: 14px; flex-wrap: wrap; }
.filter-btn { padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 500; color: var(--text-3); background: var(--bg-2); border: 1px solid var(--line-2); cursor: pointer; transition: all 0.15s; }
.filter-btn:hover { background: var(--bg-3); color: var(--text-1); }
.filter-btn.active { background: var(--accent-soft); color: var(--accent); border-color: var(--accent-line); }

/* 목록 헤더 */
.list-header {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 16px; margin-bottom: 6px;
  font-size: 11px; font-weight: 600; color: var(--text-4);
  font-family: var(--font-mono); letter-spacing: 0.06em; text-transform: uppercase;
  border-bottom: 1px solid var(--line-1);
}

/* 게시글 row */
.post-row {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border-radius: var(--r-md);
  cursor: pointer; transition: background 0.12s;
  border: 1px solid transparent;
}
.post-row:hover { background: var(--bg-2); border-color: var(--line-2); }
.post-subject { flex: 1; font-size: 13.5px; font-weight: 600; color: var(--text-1); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 64px; color: var(--text-4); font-size: 13px; }

/* Chips */
.chip { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 500; }
.chip-mute { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }

/* 모달 공통 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.55); display: flex; align-items: center; justify-content: center; z-index: 200; padding: 24px; }
.modal-box { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); width: 100%; max-width: 540px; box-shadow: 0 24px 60px rgba(0,0,0,0.4); overflow: hidden; }
.modal-write { max-width: 560px; }
.modal-head { display: flex; justify-content: space-between; align-items: flex-start; padding: 18px 20px; border-bottom: 1px solid var(--line-1); }
.icon-close { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: var(--r-sm); color: var(--text-3); cursor: pointer; flex-shrink: 0; transition: background 0.15s, color 0.15s; }
.icon-close:hover { background: var(--bg-3); color: var(--text-1); }

/* 폼 */
.form-body { padding: 20px; display: flex; flex-direction: column; gap: 14px; }
.row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.fgroup { display: flex; flex-direction: column; gap: 5px; }
.flabel { font-size: 12px; font-weight: 600; color: var(--text-3); }
.finput { background: var(--bg-1); border: 1px solid var(--line-2); border-radius: var(--r-md); padding: 8px 12px; font-size: 13px; color: var(--text-1); font-family: var(--font-sans); outline: none; width: 100%; box-sizing: border-box; transition: border-color 0.15s; }
.finput:focus { border-color: var(--accent); box-shadow: 0 0 0 2px var(--accent-soft); }
.finput::placeholder { color: var(--text-4); }
.ftextarea { resize: vertical; min-height: 120px; }
.err-msg { display: flex; align-items: center; gap: 6px; padding: 8px 12px; background: var(--danger-soft); border: 1px solid rgba(209,139,126,0.25); border-radius: var(--r-md); font-size: 12px; color: var(--danger); }
.btn-submit { display: inline-flex; align-items: center; gap: 7px; padding: 9px 20px; background: var(--accent); border: none; border-radius: var(--r-md); font-size: 13px; font-weight: 600; color: var(--accent-ink); cursor: pointer; transition: background 0.15s; }
.btn-submit:hover { background: var(--accent-hover); }
.btn-cancel { display: inline-flex; align-items: center; padding: 9px 16px; background: transparent; border: 1px solid var(--line-2); border-radius: var(--r-md); font-size: 13px; font-weight: 500; color: var(--text-3); cursor: pointer; transition: background 0.15s; }
.btn-cancel:hover { background: var(--bg-3); }

/* 관리자 답변 */
.reply-section {
  margin: 0 20px 20px;
  padding: 14px 16px;
  background: var(--accent-soft);
  border: 1px solid var(--accent-line);
  border-radius: var(--r-md);
}
.reply-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; color: var(--accent);
  margin-bottom: 8px; font-family: var(--font-mono); letter-spacing: 0.06em; text-transform: uppercase;
}

.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
