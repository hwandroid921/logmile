import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
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
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login' }
  }
  if (to.name === 'login' && authStore.isLoggedIn) {
    return { name: 'dashboard' }
  }
})

export default router
