import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDashboardStore = defineStore('dashboard', () => {
  const summary = ref(null)
  const vehicles = ref([])
  const selectedVehicleId = ref(null)

  function setSummary(data) {
    summary.value = data
  }

  function setVehicles(data) {
    vehicles.value = data
  }

  function selectVehicle(id) {
    selectedVehicleId.value = id
  }

  return { summary, vehicles, selectedVehicleId, setSummary, setVehicles, selectVehicle }
})
