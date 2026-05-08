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
      path: '/',
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

  // 비로그인 → 로그인 필요 페이지 접근 차단
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login' }
  }

  // Super Admin 전용 페이지 — 일반 관리자 접근 차단
  if (to.meta.requiresSuper && !authStore.isSuperAdmin) {
    return { name: 'dashboard' }
  }

  // 로그인 상태에서 login/signup/pending 접근 → 역할별 홈으로
  const publicRoutes = ['login', 'signup', 'pending']
  if (publicRoutes.includes(to.name) && authStore.isLoggedIn) {
    return authStore.isSuperAdmin ? { name: 'superHome' } : { name: 'dashboard' }
  }
})

export default router
