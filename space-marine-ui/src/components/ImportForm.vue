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

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    statusMessage.value = 'Загрузка файла...'
    const response = await axios.post(`${apiBaseUrl}/api/space-marines/import`, formData, {
      headers: {
        'Authorization': `Bearer ${props.token}`,
        'Content-Type': 'multipart/form-data'
      }
    })
    statusMessage.value = response.data || 'Импорт успешно завершён!'
    emit('import-complete', statusMessage.value)
  } catch (err) {
    console.error(err)
    statusMessage.value =
        'Ошибка при импорте: ' +
        (err.response?.data?.message || err.response?.data || err.message)
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
