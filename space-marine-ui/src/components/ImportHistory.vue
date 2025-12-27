<template>
  <div class="import-history">
    <h3>История импорта</h3>

    <table class="table table-bordered table-striped mt-3">
      <thead class="table-dark">
      <tr>
        <th>ID</th>
        <th>Пользователь</th>
        <th>Статус</th>
        <th>Добавлено объектов</th>
        <th>Дата</th>
        <th>Файл</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in pagedHistory" :key="item.id">
        <td>{{ item.id }}</td>
        <td>{{ item.username }}</td>
        <td>
            <span
                :class="{
                'text-success': item.status === 'SUCCESS',
                'text-danger': item.status === 'FAILED'
              }"
            >
              {{ item.status }}
            </span>
        </td>
        <td>{{ item.status === 'SUCCESS' ? item.addedCount : '-' }}</td>
        <td>{{ new Date(item.timestamp).toLocaleString() }}</td>
        <td>
          <button class="btn btn-sm btn-primary" @click.prevent="download(item.id)" :disabled="isDownloading(item.id) || item.status !== 'SUCCESS'">
            <span v-if="isDownloading(item.id)">Скачивается...</span>
            <span v-else>Скачать</span>
          </button>
        </td>
      </tr>
      </tbody>
    </table>

    <nav v-if="totalPages > 1" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button class="page-link" @click="changePage(currentPage - 1)">Prev</button>
        </li>
        <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: currentPage === p - 1 }">
          <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <button class="page-link" @click="changePage(currentPage + 1)">Next</button>
        </li>
      </ul>
    </nav>

    <div v-if="loading" class="text-muted">Загрузка истории...</div>
    <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const props = defineProps({
  token: String
})

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
const history = ref([])
const loading = ref(false)
const error = ref('')
const downloading = ref([])

const pageSize = ref(5)
const currentPage = ref(0)
const totalPages = ref(1)

function changePage(page) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  loadHistory()
}

const pagedHistory = computed(() => history.value)

async function loadHistory() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${apiBaseUrl}/api/import/history?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    if (!res.ok) throw new Error(`Ошибка: ${res.status}`)
    const data = await res.json()
    history.value = data.content || []
    totalPages.value = data.totalPages || 1
    currentPage.value = data.number || currentPage.value
  } catch (e) {
    console.error(e)
    error.value = 'Не удалось загрузить историю импорта'
  } finally {
    loading.value = false
  }
}

onMounted(loadHistory)

function isDownloading(id) {
  return downloading.value.includes(id)
}

async function download(id) {
  if (isDownloading(id)) return
  downloading.value.push(id)
  error.value = ''
  try {
    const res = await fetch(`${apiBaseUrl}/api/imports/${id}/download`, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    if (!res.ok) {
      let msg = `Ошибка: ${res.status}`
      try { const j = await res.json(); msg = j.message || j.code || msg } catch (e) {}
      error.value = msg
      return
    }
    const data = await res.json()
    if (data && data.code === 'SUCCESS' && data.message) {
      window.open(data.message, '_blank')
    } else {
      error.value = data?.message || 'Не удалось получить ссылку на файл'
    }
  } catch (e) {
    console.error(e)
    error.value = 'Ошибка при скачивании файла'
  } finally {
    downloading.value = downloading.value.filter(x => x !== id)
  }
}
</script>

<style scoped>
.import-history {
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}
</style>
