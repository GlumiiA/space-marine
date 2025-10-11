<template>
  <div class="container mt-4">
    <h1 class="space-marines-title">Space Marines</h1>

    <div class="d-flex gap-2 mb-3 flex-wrap align-items-center">
      <button class="btn btn-primary" @click="openCreateModal">Create SpaceMarine</button>

      <button class="btn btn-info" @click="toggleOpsPanel">
        {{ showOpsPanel ? "Back to Table" : "Special Operations" }}
      </button>

      <button class="btn btn-warning" @click="openAddToChapter">Add marine to chapter</button>
      <button class="btn btn-danger" @click="openDissolveChapter">Dissolve chapter</button>

      <button v-if="isLoggedIn" class="btn btn-outline-danger ms-auto" @click="handleLogout">
        Logout
      </button>
    </div>

    <!-- спецоперации -->
    <SpecialOperations v-if="showOpsPanel" :token="token" />

    <!-- таблица -->
    <MarineTable
        v-if="!showOpsPanel"
        :marines="marines"
        :sortKey="sortKey"
        :sortDir="sortDir"
        @changeSort="handleChangeSort"
        @view="viewDetails"
        @edit="openEditModal"
        @delete="confirmDelete"
    />

    <!-- пагинация -->
    <nav v-if="!showOpsPanel && totalPages > 1" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button class="page-link" @click="changePage(currentPage - 1)">Prev</button>
        </li>
        <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: currentPage === page - 1 }">
          <button class="page-link" @click="changePage(page - 1)">{{ page }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <button class="page-link" @click="changePage(currentPage + 1)">Next</button>
        </li>
      </ul>
    </nav>

    <MarineForm
        v-else-if="showForm"
        :marine="selectedMarine"
        :chapters="chapters"
        :token="token"
        @save="saveMarine"
        @close="closeForm"
    />
    <!-- Add to Chapter modal -->
    <div v-if="showAddToChapter" class="modal-overlay">
      <div class="modal-window">
        <h5>Add Marine to Chapter</h5>
        <select v-model="selectedMarineId" class="form-select mb-2">
          <option disabled value="">-- select marine --</option>
          <option v-for="m in marines" :key="m.id" :value="m.id">{{ m.name }} (ID: {{ m.id }})</option>
        </select>
        <select v-model="selectedChapterId" class="form-select mb-2">
          <option disabled value="">-- select chapter --</option>
          <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.name }} (ID: {{ c.id }})</option>
        </select>
        <div class="d-flex gap-2">
          <button class="btn btn-primary" @click="assignMarine">Assign</button>
          <button class="btn btn-secondary" @click="showAddToChapter = false">Close</button>
        </div>
      </div>
    </div>

    <!-- Dissolve Chapter modal -->
    <div v-if="showDissolveChapter" class="modal-overlay">
      <div class="modal-window">
        <div class="modal-header">
          <h5>Dissolve Chapter</h5>
          <button class="btn-close" @click="closeDissolveChapter"></button>
        </div>

        <div class="modal-body">
          <label class="form-label">Select chapter to dissolve</label>
          <select v-model="selectedChapterForDissolve" class="form-select mb-2">
            <option disabled value="">-- select chapter --</option>
            <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.name }} (ID: {{ c.id }})</option>
          </select>

          <p class="muted small">Chapter can be dissolved only if it has no marines assigned.</p>

          <div class="d-flex gap-2 mt-2">
            <button class="btn btn-danger" :disabled="!selectedChapterForDissolve" @click="confirmDissolve">
              Dissolve
            </button>
            <button class="btn btn-secondary" @click="closeDissolveChapter">Cancel</button>
          </div>
        </div>
      </div>
    </div>

    <!-- уведомления -->
    <div v-if="operationResult" class="alert alert-info mt-3">
      {{ operationResult }}
      <button class="btn-close float-end" @click="operationResult = ''"></button>
    </div>
  </div>

  <!-- детали -->
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
import { ref, watch, onMounted, toRaw } from 'vue';
import { useRouter } from 'vue-router';
import MarineTable from '../components/MarineTable.vue';
import MarineForm from '../components/MarineForm.vue';
import SpecialOperations from '../components/SpecialOperations.vue';

const router = useRouter();

const props = defineProps({
  isLoggedIn: Boolean,
  token: String,
  currentUser: String
});

const emit = defineEmits(['logout']);
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

const marines = ref([]);
const chapters = ref([]);
const showForm = ref(false);
const selectedMarine = ref(null);
const showAddToChapter = ref(false);
const showDissolveChapter = ref(false);
const selectedMarineId = ref(null);
const selectedChapterId = ref(null);
const selectedChapterForDissolve = ref(null);
const operationResult = ref('');
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);
const selectedMarineDetails = ref(null);
const showDetailsModal = ref(false);
const showOpsPanel = ref(false);

const sortKey = ref("id");
const sortDir = ref("asc");

function handleChangeSort(key) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortKey.value = key;
    sortDir.value = 'asc';
  }
  refreshMarines();
}


function toggleOpsPanel() {
  showOpsPanel.value = !showOpsPanel.value;
}

function viewDetails(marine) {
  selectedMarineDetails.value = { ...toRaw(marine) };
  showDetailsModal.value = true;
}

async function refreshMarines() {
  if (!props.token) return;
  try {
    const url = `${apiBaseUrl}/api/space-marines?page=${currentPage.value}&size=${pageSize.value}&sortBy=${sortKey.value}&sortDir=${sortDir.value}`;
    const res = await fetch(url, {
      headers: { "Authorization": `Bearer ${props.token}` }
    });

    if (!res.ok) {
      if (res.status === 403) { handleLogout(); return; }
      throw new Error(`HTTP ${res.status}`);
    }

    const data = await res.json();
    marines.value = data.content || [];
    totalPages.value = data.totalPages || 0;
  } catch (e) {
    console.error(e);
    operationResult.value = "Ошибка загрузки Space Marines";
  }
}

async function refreshChapters() {
  if (!props.token) return;
  try {
    const res = await fetch('http://localhost:8080/api/chapters', {
      headers: { "Authorization": `Bearer ${props.token}` }
    });
    if (!res.ok) {
      if (res.status === 403) { handleLogout(); return; }
      throw new Error(`HTTP ${res.status}`);
    }
    chapters.value = await res.json();
  } catch (e) {
    console.error(e);
    operationResult.value = "Ошибка загрузки глав";
  }
}

// user actions
function handleLogout() { emit('logout'); }
function changePage(page) { if (page >= 0 && page < totalPages.value) { currentPage.value = page; refreshMarines(); } }
function openCreateModal() { selectedMarine.value = null; showForm.value = true; }
function openEditModal(marine) { selectedMarine.value = { ...marine, coordinates: { ...marine.coordinates } }; showForm.value = true; }
function closeForm() { showForm.value = false; }
function openAddToChapter() {
  showAddToChapter.value = true;
  selectedMarineId.value = null;
  selectedChapterId.value = null;
}

async function saveMarine(payload) {
  try {
    const url = payload.id
        ? `${apiBaseUrl}/api/space-marines/${payload.id}`
        : `${apiBaseUrl}/api/space-marines`;
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
    if (err.message.includes("403")) {
      alert("You cannot edit this marine because you are not the owner.");
    } else {
      console.error("Failed to save marine:", err);
      operationResult.value = err.message;
    }
  }
}

async function assignMarine() {
  if (!selectedMarineId.value || !selectedChapterId.value) {
    alert("Please select both marine and chapter!");
    return;
  }

  try {
    const res = await fetch(
        `${apiBaseUrl}/api/space-marines/${selectedMarineId.value}/assign-chapter?chapterId=${selectedChapterId.value}`,
        {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${props.token}`,
            "Content-Type": "application/json"
          }
        }
    );

    if (!res.ok) {
      const msg = await handleErrorResponse(res);
      operationResult.value = msg;
      showAddToChapter.value = false;
      return;
    }

    operationResult.value = "Marine successfully assigned to chapter!";
    showAddToChapter.value = false;
    await refreshMarines();
    await refreshChapters();
  } catch (err) {
    console.error(err);
    operationResult.value = "Network error while assigning marine";
  }
}

function openDissolveChapter() {
  // очистим выбор и откроем модалку
  selectedChapterForDissolve.value = null;
  showDissolveChapter.value = true;
}
function closeDissolveChapter() {
  showDissolveChapter.value = false;
  selectedChapterForDissolve.value = null;
}

async function confirmDissolve() {
  if (!selectedChapterForDissolve.value) return;
  const ok = confirm("Вы действительно хотите распустить эту главу? Это действие необратимо.");
  if (!ok) return;
  try {
    const res = await fetch(`${apiBaseUrl}/api/chapters/${selectedChapterForDissolve.value}/dissolve`, {
      method: 'POST',
      headers: { "Authorization": `Bearer ${props.token}` }
    });
    if (!res.ok) {
      const msg = await handleErrorResponse(res);
      operationResult.value = msg;
      closeDissolveChapter();
      return;
    }
    operationResult.value = "Chapter dissolved successfully";
    showDissolveChapter.value = false;
    await refreshChapters();
    await refreshMarines(); // на случай, если список изменилась
  } catch (e) {
    console.error(e);
    operationResult.value = "Network error while dissolving chapter";
  }
}

async function confirmDelete(marine) {
  if (confirm(`Удалить ${marine.name}?`)) {
    try {
      const res = await fetch(`${apiBaseUrl}/api/space-marines/${marine.id}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${props.token}` }
      });
      if (!res.ok) {
        const msg = await handleErrorResponse(res);
        operationResult.value = msg;
        closeDissolveChapter();
        return;
      }

      const data = await res.json();
      operationResult.value = data.message || "Chapter dissolved successfully";

      showDissolveChapter.value = false;
      await refreshChapters();
      await refreshMarines();
    } catch (err) {
      console.error(err);
      operationResult.value = "Ошибка сети при удалении";
    }
  }
}

async function handleErrorResponse(res) {
  let msg = `Ошибка: ${res.status}`;
  try {
    const data = await res.json();

    if (data.message) {
      msg = data.message;
    } else if (data.error) {
      msg = data.error;
      if (data.timestamp && data.status) {
        msg += data.message ? `: ${data.message}` : '';
      }
    }
  } catch {
    const text = await res.text();
    msg = text || msg;
  }

  return msg;
}


watch(() => props.token, (newToken) => {
  if (newToken && props.isLoggedIn) { refreshMarines(); refreshChapters(); }
});

onMounted(() => {
  if (!props.isLoggedIn || !props.token) { router.push("/login"); return; }
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
