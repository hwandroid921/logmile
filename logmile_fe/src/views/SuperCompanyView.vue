<script setup>
import AppIcon from '@/components/common/AppIcon.vue'
</script>

<template>
  <div class="view">
    <div class="view-header">
      <div>
        <h2 class="view-title">업체 관리</h2>
        <p class="view-sub">등록된 운송 업체 목록을 확인하고 관리합니다.</p>
      </div>
    </div>

    <!-- API 준비 중 안내 카드 -->
    <div class="card notice-card">
      <div class="notice-icon">
        <AppIcon name="building" :size="28" />
      </div>
      <div class="notice-body">
        <h3 class="notice-title">업체 관리 API 준비 중</h3>
        <p class="notice-desc">
          업체 목록 조회 · 수정 · 상태 변경 기능은 백엔드 API 연동 후 제공될 예정입니다.<br />
          현재는 가입 승인 과정에서 업체가 자동으로 등록됩니다.
        </p>
        <div class="notice-meta">
          <span class="label-sm">예정 엔드포인트</span>
          <code class="mono endpoint">GET /api/companies</code>
          <code class="mono endpoint">PATCH /api/companies/:id/deactivate</code>
        </div>
      </div>
    </div>

    <!-- 플레이스홀더 테이블 -->
    <div class="card table-card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>업체 ID</th>
              <th>업체명</th>
              <th>주소</th>
              <th>연락처</th>
              <th>등록일</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td colspan="6" class="placeholder-row">
                <AppIcon name="building" :size="20" class="ph-icon" />
                <span>업체 목록을 불러올 수 없습니다. API 연동 후 이용 가능합니다.</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.view-header { display: flex; align-items: flex-start; justify-content: space-between; }
.view-title { font-size: 18px; font-weight: 700; color: var(--text-1); margin: 0 0 4px; }
.view-sub { font-size: 12.5px; color: var(--text-4); margin: 0; }

/* 안내 카드 */
.notice-card {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  background: var(--warn-soft);
  border-color: rgba(214, 181, 106, 0.25);
}
.notice-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--r-md);
  background: rgba(214, 181, 106, 0.15);
  color: var(--warn);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.notice-body { flex: 1; }
.notice-title { font-size: 14px; font-weight: 600; color: var(--text-1); margin: 0 0 6px; }
.notice-desc { font-size: 13px; color: var(--text-3); line-height: 1.6; margin: 0 0 12px; }
.notice-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.endpoint {
  display: inline-block;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: var(--r-sm);
  padding: 2px 8px;
  font-size: 11.5px;
  color: var(--accent);
}

/* 테이블 */
.table-card { padding: 0; overflow: hidden; }
.table-wrap { overflow-x: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th {
  padding: 11px 16px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-4);
  background: var(--bg-3);
  border-bottom: 1px solid var(--line-1);
}
.placeholder-row {
  padding: 48px 16px;
  text-align: center;
  color: var(--text-4);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.ph-icon { opacity: 0.4; }
</style>
