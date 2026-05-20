<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import '@/composables/useTheme' // 앱 시작 시 저장된 테마 즉시 적용
import { useSimulationStore } from '@/stores/simulationStore'

const route = useRoute()

// 페이지 진입 시 localStorage에서 운행 상태 복원
onMounted(() => {
  useSimulationStore().hydrate()
})

// 레이아웃 없이 풀스크린으로 보여줄 화면 이름
const noLayoutRoutes = ['login', 'signup', 'pending']

const useLayout = computed(() => !noLayoutRoutes.includes(route.name))
</script>

<template>
  <!-- 레이아웃 필요 없는 화면 (로그인, 회원가입, 승인대기) -->
  <RouterView v-if="!useLayout" />

  <!-- 어드민 레이아웃 (사이드바 + 상단바) -->
  <AppLayout v-else>
    <RouterView />
  </AppLayout>
</template>
