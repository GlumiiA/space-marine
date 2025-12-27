<template>
  <div class="import-container">
    <h3>Импорт Space Marines из JSON</h3>

    <input
        type="file"
        accept=".json"
        @change="onFileChange"
        class="form-control mb-3"
    />

    <div class="d-flex gap-2">
      <button class="btn btn-primary" @click="uploadFile" :disabled="!selectedFile">
        Загрузить
      </button>
      <button class="btn btn-secondary" @click="$emit('import-complete', 'Импорт отменён')">
        Отмена
      </button>
    </div>

    <div v-if="statusMessage" class="alert alert-info mt-3">
      {{ statusMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { decodeJwt } from '../utils/decodeJwt'

const props = defineProps({
  token: String
})
const emit = defineEmits(['import-complete'])

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

const selectedFile = ref(null)
const statusMessage = ref('')

function onFileChange(e) {
  selectedFile.value = e.target.files[0]
}

async function uploadFile() {
  if (!selectedFile.value) return

  // 1. prepare: upload file to Minio temp storage
  const username = decodeJwt(props.token)?.sub || decodeJwt(props.token)?.username || 'unknown'
  const prepareForm = new FormData()
  prepareForm.append('file', selectedFile.value)
  prepareForm.append('username', username)

  try {
    statusMessage.value = 'Подготовка файла...'
    const prepRes = await axios.post(`${apiBaseUrl}/api/imports/prepare`, prepareForm, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    const prepMsg = prepRes.data?.message || ''
    const m = prepMsg.match(/id=(\d+)/)
    const opId = m ? m[1] : null
    if (!opId) throw new Error('Не удалось получить id операции подготовки')

    // 2. import: parse and save to DB, link with importOperationId
    statusMessage.value = 'Импорт данных...'
    const importForm = new FormData()
    importForm.append('file', selectedFile.value)
    await axios.post(`${apiBaseUrl}/api/space-marines/import?importOperationId=${opId}`, importForm, {
      headers: { Authorization: `Bearer ${props.token}` }
    })

    // 3. get download link (backend already commits when importOperationId provided)
    statusMessage.value = 'Получаю ссылку на файл...'
    try {
      const dl = await axios.get(`${apiBaseUrl}/api/imports/${opId}/download`, {
        headers: { Authorization: `Bearer ${props.token}` }
      })
      const url = dl.data?.message
      if (url) {
        statusMessage.value = 'Импорт завершён.'
      } else {
        statusMessage.value = 'Импорт завершён, ссылка на файл недоступна'
      }
      emit('import-complete', statusMessage.value)
    } catch (e) {
      statusMessage.value = 'Импорт завершён, но ссылка на файл недоступна'
      emit('import-complete', statusMessage.value)
    }
  } catch (err) {
    console.error(err)
    statusMessage.value = 'Ошибка при импорте: ' + (err.response?.data?.message || err.message)
  }
}
</script>

<style scoped>
.import-container {
  background: #fff;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 0 10px rgba(0,0,0,0.2);
  max-width: 500px;
  margin: 2rem auto;
}
</style>
