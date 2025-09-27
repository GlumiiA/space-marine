<template>
  <div class="container mt-4">
    <h1 class="space-marines-title">Space Marines</h1>

    <div class="d-flex gap-2 mb-3 flex-wrap align-items-center">
      <button class="btn btn-primary" @click="openCreateModal">
        Create SpaceMarine
      </button>

      <button class="btn btn-secondary" @click="sumHealth">Sum health</button>
      <button class="btn btn-secondary" @click="avgHealth">Avg health</button>
      <button class="btn btn-secondary" @click="minCoordinates">Min coordinates</button>
      <button class="btn btn-warning" @click="openAddToChapter">
        Add marine to chapter
      </button>
      <button class="btn btn-danger" @click="openDissolveChapter">
        Dissolve chapter
      </button>

      <button
          v-if="isLoggedIn"
          class="btn btn-outline-danger ms-auto"
          @click="handleLogout"
      >
        Logout
      </button>
    </div>

    <!-- таблица -->
    <MarineTable
        :marines="filteredMarines"
        @edit="openEditModal"
        @delete="confirmDelete"
        @view="viewDetails"
    />

    <!-- пагинация -->
    <nav v-if="totalPages > 1" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button class="page-link" @click="changePage(currentPage - 1)">Prev</button>
        </li>
        <li
            v-for="page in totalPages"
            :key="page"
            class="page-item"
            :class="{ active: currentPage === page - 1 }"
        >
          <button class="page-link" @click="changePage(page - 1)">
            {{ page }}
          </button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <button class="page-link" @click="changePage(currentPage + 1)">Next</button>
        </li>
      </ul>
    </nav>

    <!-- форма -->
    <MarineForm
        v-if="showForm"
        :marine="selectedMarine"
        :chapters="chapters"
        :token="token"
        @close="closeForm"
        @save="saveMarine"
    />

    <!-- модалки (Add to Chapter, Dissolve Chapter) -->
    <div v-if="showAddToChapter" class="modal-overlay">
      <div class="modal-window">
        <h5>Add Marine to Chapter</h5>
        <select v-model="selectedMarineId" class="form-select mb-2">
          <option v-for="m in marines" :key="m.id" :value="m.id">{{ m.name }} (ID: {{ m.id }})</option>
        </select>
        <select v-model="selectedChapterId" class="form-select mb-2">
          <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.name }} (ID: {{ c.id }})</option>
        </select>
        <div class="d-flex gap-2">
          <button class="btn btn-primary" @click="assignMarine">Assign</button>
          <button class="btn btn-secondary" @click="showAddToChapter = false">Close</button>
        </div>
      </div>
    </div>

    <div v-if="showDissolveChapter" class="modal-overlay">
      <div class="modal-window">
        <h5>Dissolve Chapter</h5>
        <select v-model="selectedChapterId" class="form-select mb-2">
          <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.name }} (ID: {{ c.id }})</option>
        </select>
        <div class="d-flex gap-2">
          <button class="btn btn-danger" @click="dissolveChapter">Dissolve</button>
          <button class="btn btn-secondary" @click="showDissolveChapter = false">Close</button>
        </div>
      </div>
    </div>

    <!-- уведомления -->
    <div v-if="operationResult" class="alert alert-info mt-3">
      {{ operationResult }}
      <button class="btn-close float-end" @click="operationResult = ''"></button>
    </div>
  </div>

  <div v-if="showDetailsModal" class="modal-overlay">
    <div class="modal-window">
      <div class="modal-header">
        <h5>Space Marine Details</h5>
        <button class="btn-close" @click="showDetailsModal = false"></button>
      </div>
      <div class="modal-body">
        <p><strong>Name:</strong> {{ selectedMarineDetails.name }}</p>
        <p><strong>Coordinates:</strong> X={{ selectedMarineDetails.coordinates.x }}, Y={{ selectedMarineDetails.coordinates.y }}</p>
        <p><strong>Health:</strong> {{ selectedMarineDetails.health }}</p>
        <p><strong>Loyal:</strong> {{ selectedMarineDetails.loyal ? "Yes" : "No" }}</p>
        <p><strong>Category:</strong> {{ selectedMarineDetails.category }}</p>
        <p><strong>Achievements:</strong> {{ selectedMarineDetails.achievements }}</p>

        <hr>

        <p><strong>Chapter Name:</strong> {{ selectedMarineDetails.chapter?.name || "N/A" }}</p>
        <p><strong>Parent Legion:</strong> {{ selectedMarineDetails.chapter?.parentLegion || "N/A" }}</p>
        <p><strong>World:</strong> {{ selectedMarineDetails.chapter?.world || "N/A" }}</p>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, computed, watch, onMounted, toRaw } from 'vue';
import { useRouter } from 'vue-router';
import MarineTable from '../components/MarineTable.vue';
import MarineForm from '../components/MarineForm.vue';

const router = useRouter();

const props = defineProps({
  isLoggedIn: Boolean,
  token: String,
  currentUser: String
});

const emit = defineEmits(['logout']);

// Реактивные данные
const marines = ref([]);
const chapters = ref([]);
const search = ref('');
const showForm = ref(false);
const selectedMarine = ref(null);
const showAddToChapter = ref(false);
const showDissolveChapter = ref(false);
const selectedMarineId = ref(null);
const selectedChapterId = ref(null);
const operationResult = ref('');
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);
const selectedMarineDetails = ref(null);
const showDetailsModal = ref(false);

async function loadChapter(chapterId) {
  try {
    const res = await fetch(`http://localhost:8080/api/chapters/${chapterId}`, {
      headers: { "Authorization": `Bearer ${props.token}` }
    });
    if (!res.ok) throw new Error('Failed to load chapter');
    return await res.json();
  } catch (err) {
    console.error(err);
    return null;
  }
}

function viewDetails(marine) {
  selectedMarineDetails.value = { ...toRaw(marine) };
  showDetailsModal.value = true;
}
// Computed properties
const filteredMarines = computed(() => {
  if (!search.value) return marines.value;
  return marines.value.filter(m =>
      m.name?.includes(search.value) ||
      m.chapter?.name?.includes(search.value) ||
      m.chapter?.parentLegion?.includes(search.value) ||
      m.chapter?.world?.includes(search.value) ||
      m.achievements?.includes(search.value)
  );
});

// Methods
async function refreshMarines() {
  if (!props.token) {
    console.error('No token available');
    return;
  }

  try {
    console.log('Refreshing marines with token:', props.token.substring(0, 20) + '...');
    const res = await fetch(`http://localhost:8080/api/space-marines?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: {
        "Authorization": `Bearer ${props.token}`,
        "Content-Type": "application/json"
      }
    });

    console.log('Response status:', res.status);

    if (res.status === 403) {
      handleLogout();
      return;
    }

    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }

    const data = await res.json();
    console.log('Marines data received:', data);
    marines.value = data.content || [];
    totalPages.value = data.totalPages || 0;

  } catch (err) {
    console.error('Error loading marines:', err);
    operationResult.value = "Ошибка загрузки Space Marines";
  }
}

async function refreshChapters() {
  if (!props.token) {
    console.error('No token available for chapters');
    return;
  }

  try {
    const res = await fetch('http://localhost:8080/api/chapters', {
      headers: {
        "Authorization": `Bearer ${props.token}`,
        "Content-Type": "application/json"
      }
    });

    if (res.status === 403) {
      handleLogout();
      return;
    }

    if (res.ok) {
      chapters.value = await res.json();
      console.log('Chapters loaded:', chapters.value.length);
    }
  } catch (err) {
    console.error('Error loading chapters:', err);
    operationResult.value = "Ошибка загрузки глав";
  }
}

function handleLogout() {
  emit('logout');
}

function changePage(page) {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page;
    refreshMarines();
  }
}

function openCreateModal() {
  selectedMarine.value = null;
  showForm.value = true;
}

function openEditModal(marine) {
  selectedMarine.value = {
    ...marine,
    coordinates: { ...marine.coordinates }
  };
  showForm.value = true;
}

function closeForm() {
  showForm.value = false;
}

async function saveMarine(payload) {
  try {
    const url = payload.id
        ? `http://localhost:8080/api/space-marines/${payload.id}`
        : 'http://localhost:8080/api/space-marines';
    const method = payload.id ? 'PUT' : 'POST';

    const res = await fetch(url, {
      method,
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${props.token}`
      },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to save marine: ${res.status} - ${text}`);
    }

    operationResult.value = payload.id ? "Marine updated successfully" : "Marine created successfully";
    showForm.value = false;
    await refreshMarines();
    await refreshChapters();

  } catch (err) {
    // Используем правильную переменную err
    if (err.message.includes("403")) {
      alert("You cannot edit this marine because you are not the owner.");
    } else {
      console.error("Failed to save marine:", err);
      operationResult.value = err.message;
    }
  }
}



async function confirmDelete(marine) {
  if (!marine.owner || marine.owner !== props.currentUser) {
    alert("Можно удалять только свои объекты!");
    return;
  }
  if (confirm(`Удалить ${marine.name}?`)) {
    try {
      const res = await fetch(`http://localhost:8080/api/space-marines/${marine.id}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${props.token}`,
          "Content-Type": "application/json"
        }
      });
      if (res.ok) {
        operationResult.value = `${marine.name} удалён`;
        refreshMarines();
      } else {
        operationResult.value = "Ошибка удаления";
      }
    } catch (err) {
      console.error(err);
      operationResult.value = "Ошибка удаления";
    }
  }
}

// Watch для отслеживания изменений токена
watch(() => props.token, (newToken) => {
  if (newToken && props.isLoggedIn) {
    console.log('Token changed, refreshing data...');
    refreshMarines();
    refreshChapters();
  }
});

onMounted(() => {
  console.log('MarinesPage mounted:', {
    isLoggedIn: props.isLoggedIn,
    token: props.token ? props.token.substring(0, 20) + '...' : 'null',
    currentUser: props.currentUser
  });

  if (!props.isLoggedIn || !props.token) {
    console.log('Redirecting to login - missing auth data');
    router.push("/login");
    return;
  }

  refreshMarines();
  refreshChapters();
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top:0; left:0;
  width:100%; height:100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-window {
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  min-width: 300px;
  box-shadow: 0 0 10px rgba(0,0,0,0.5);
}
.space-marines-title {
  color: white;
}
</style>
