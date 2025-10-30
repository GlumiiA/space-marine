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
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in history" :key="item.id">
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
      </tr>
      </tbody>
    </table>

    <div v-if="loading" class="text-muted">Загрузка истории...</div>
    <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  token: String
})

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
const history = ref([])
const loading = ref(false)
const error = ref('')

async function loadHistory() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${apiBaseUrl}/api/import/history`, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    if (!res.ok) throw new Error(`Ошибка: ${res.status}`)
    history.value = await res.json()
  } catch (e) {
    console.error(e)
    error.value = 'Не удалось загрузить историю импорта'
  } finally {
    loading.value = false
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.import-history {
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}
</style>
