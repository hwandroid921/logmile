import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSimulationStore = defineStore('simulation', () => {
  const isRunning = ref(false)
  const driveLogId = ref(null)
  const scenario = ref(null)
  const fatigueScore = ref(0)
  const fatigueLevel = ref('NORMAL')

  function start(logId, scenarioType) {
    isRunning.value = true
    driveLogId.value = logId
    scenario.value = scenarioType
    fatigueScore.value = 0
    fatigueLevel.value = 'NORMAL'
  }

  function stop() {
    isRunning.value = false
    driveLogId.value = null
    scenario.value = null
  }

  function updateFatigue(score, level) {
    fatigueScore.value = score
    fatigueLevel.value = level
  }

  return { isRunning, driveLogId, scenario, fatigueScore, fatigueLevel, start, stop, updateFatigue }
})
