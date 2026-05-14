import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Auth (레이아웃 없음) ──────────────────────────
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('@/views/SignupView.vue'),
    },
    {
      path: '/pending',
      name: 'pending',
      component: () => import('@/views/PendingView.vue'),
    },

    // ── 공개 페이지 (로그인 불필요, AppLayout 사용) ───
    {
      path: '/',
      name: 'publicHome',
      component: () => import('@/views/public/PublicHomeView.vue'),
    },
    {
      path: '/intro',
      name: 'publicIntro',
      component: () => import('@/views/public/PublicIntroView.vue'),
    },
    {
      path: '/board',
      name: 'publicBoard',
      component: () => import('@/views/public/BoardView.vue'),
    },
    {
      path: '/features',
      name: 'publicFeatures',
      component: () => import('@/views/public/PublicFeaturesView.vue'),
    },
    {
      path: '/contact',
      name: 'publicContact',
      component: () => import('@/views/public/PublicContactView.vue'),
    },

    // ── 공개 데모 (로그인 불필요, mockData 사용) ────────
    {
      path: '/demo/dashboard',
      name: 'demoBoard',
      component: () => import('@/views/DashboardView.vue'),
    },
    {
      path: '/demo/simulation',
      name: 'demoSim',
      component: () => import('@/views/SimulationView.vue'),
    },
    {
      path: '/demo/thresholds',
      name: 'demoThresholds',
      component: () => import('@/views/ThresholdView.vue'),
    },

    // ── Super Admin (레이아웃 있음, ROLE_SUPER_ADMIN) ──
    {
      path: '/super',
      name: 'superHome',
      component: () => import('@/views/SuperHomeView.vue'),
      meta: { requiresAuth: true, requiresSuper: true },
    },
    {
      path: '/super/approval',
      name: 'superApproval',
      component: () => import('@/views/SuperApprovalView.vue'),
      meta: { requiresAuth: true, requiresSuper: true },
    },
    {
      path: '/super/company',
      name: 'superCompany',
      component: () => import('@/views/SuperCompanyView.vue'),
      meta: { requiresAuth: true, requiresSuper: true },
    },

    // ── Admin (레이아웃 있음, ROLE_ADMIN) ─────────────
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/simulation',
      name: 'simulation',
      component: () => import('@/views/SimulationView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/vehicles',
      name: 'vehicles',
      component: () => import('@/views/VehicleView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/drivers',
      name: 'drivers',
      component: () => import('@/views/DriverView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/thresholds',
      name: 'thresholds',
      component: () => import('@/views/ThresholdView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/drive-history',
      name: 'driveHistory',
      component: () => import('@/views/DriveHistoryView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/drive-history/:id',
      name: 'driveHistoryDetail',
      component: () => import('@/views/DriveHistoryDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/stats',
      name: 'stats',
      component: () => import('@/views/FatigueStatsView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  // 1. 비로그인 → 로그인 필요 페이지 접근 차단
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login' }
  }

  // 2. 계정 상태 체크 (로그인은 됐지만 상태 이상) — 보호 경로 접근 시에만 적용
  if (authStore.isLoggedIn && to.meta.requiresAuth) {
    const status = authStore.status

    // PENDING → 승인 대기 페이지로
    if (status === 'PENDING' && to.name !== 'pending') {
      return { name: 'pending' }
    }

    // REJECTED · SUSPENDED → 로그인 페이지로 (토큰 제거)
    if (status === 'REJECTED' || status === 'SUSPENDED') {
      authStore.clearAuth()
      return { name: 'login' }
    }
  }

  // 3. Super Admin 전용 페이지 — 일반 관리자 접근 차단
  if (to.meta.requiresSuper && !authStore.isSuperAdmin) {
    return { name: 'dashboard' }
  }

  // 4. 로그인 상태에서 login/signup/pending 접근 → 역할별 홈으로
  const authOnlyRoutes = ['login', 'signup', 'pending']
  if (authOnlyRoutes.includes(to.name) && authStore.isLoggedIn) {
    // PENDING 상태이면 pending 페이지 유지
    if (authStore.status === 'PENDING' && to.name === 'pending') return
    return authStore.isSuperAdmin ? { name: 'superHome' } : { name: 'dashboard' }
  }

  // 5. 로그인 상태에서 공개 메인('/') 접근 → 역할별 홈으로
  if (to.name === 'publicHome' && authStore.isLoggedIn) {
    return authStore.isSuperAdmin ? { name: 'superHome' } : { name: 'dashboard' }
  }
})

export default router
