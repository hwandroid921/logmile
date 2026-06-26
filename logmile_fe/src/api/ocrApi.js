import axios from 'axios'

const ocrClient = axios.create({
  baseURL: import.meta.env.VITE_AI_BASE_URL || 'http://localhost:8000',
  timeout: 30000,
})

export const ocrApi = {
  recognize(file, config = {}) {
    const formData = new FormData()
    formData.append('file', file)

    return ocrClient.post('/api/ocr/recognize', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      ...config,
    })
  },
}
