<template>
  <div class="container mt-4">
    <h1 class="space-marines-title">Space Marines</h1>

    <!-- Панель кнопок -->
    <div class="d-flex gap-2 mb-3 flex-wrap align-items-center">
      <!-- Исправленная кнопка -->
      <button class="btn btn-primary" @click="openCreateModal">
        Create SpaceMarine
      </button>

      <button class="btn btn-secondary">Sum health</button>
      <button class="btn btn-secondary">Avg health</button>
      <button class="btn btn-secondary">Min coordinates</button>
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

    <MarineTable
        :marines="filteredMarines"
        @edit="openEditModal"
        @delete="confirmDelete"
        @view="viewDetails"
    />

    <!-- Create / Edit Marine -->
    <MarineForm
        v-if="showForm"
        :marine="selectedMarine"
        @close="closeForm"
        @save="refreshMarines"
    />

    <!-- Add Marine to Chapter -->
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

    <!-- Dissolve Chapter -->
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

    <!-- Результаты операций -->
    <div v-if="operationResult" class="alert alert-info mt-3">
      {{ operationResult }}
      <button class="btn-close float-end" @click="operationResult = ''"></button>
    </div>
  </div>
</template>

<script>
import MarineTable from '../components/MarineTable.vue';
import MarineForm from '../components/MarineForm.vue';

export default {
  props: {
    isLoggedIn: Boolean,
    handleLogout: Function
  },
  components: { MarineTable, MarineForm },
  data() {
    return {
      marines: [],
      chapters: [], // заглушка, позже из API
      search: '',
      showForm: false,
      selectedMarine: null,
      showAddToChapter: false,
      showDissolveChapter: false,
      selectedMarineId: null,
      selectedChapterId: null,
      operationResult: ''
    };
  },
  computed: {
    filteredMarines() {
      if (!this.search) return this.marines;
      return this.marines.filter(m =>
          m.name.includes(this.search) ||
          (m.chapter?.name?.includes(this.search)) ||
          (m.chapter?.parentLegion?.includes(this.search)) ||
          (m.chapter?.world?.includes(this.search)) ||
          (m.achievements?.includes(this.search))
      );
    }
  },
  methods: {
    openCreateModal() {
      this.selectedMarine = null;
      this.showForm = true;
    },
    openEditModal(marine) {
      this.selectedMarine = marine;
      this.showForm = true;
    },
    closeForm() {
      this.showForm = false;
    },
    refreshMarines() {
      // Заглушка: подгрузка с сервера
      this.marines = [
        { id:1, name:'Marine 1', coordinates:{x:1,y:2}, creationDate:'2025-01-01', chapter:{name:'Alpha'}, health:100, loyal:true, achievements:'None', category:'SCOUT' }
      ];
    },
    confirmDelete(marine) {
      if (confirm(`Удалить ${marine.name}?`)) { /* DELETE API */ }
    },
    viewDetails(marine) { /* Можно показать детальную панель */ },

    // Спецоперации
    sumHealth() { this.operationResult = 'Sum health: 1000 (API заглушка)'; },
    avgHealth() { this.operationResult = 'Avg health: 200 (API заглушка)'; },
    minCoordinates() { this.operationResult = 'Min coordinates: Marine 1 (API заглушка)'; },

    // Диалог добавления
    openAddToChapter() {
      this.showAddToChapter = true;
      this.selectedMarineId = this.marines[0]?.id || null;
      this.selectedChapterId = this.chapters[0]?.id || null;
    },
    assignMarine() {
      // POST /api/space-marines/{id}/assign-chapter?chapterId=...
      this.operationResult = `Marine ID ${this.selectedMarineId} assigned to Chapter ID ${this.selectedChapterId}`;
      this.showAddToChapter = false;
    },

    // Диалог Dissolve
    openDissolveChapter() {
      this.showDissolveChapter = true;
      this.selectedChapterId = this.chapters[0]?.id || null;
    },
    dissolveChapter() {
      // POST /api/chapters/{id}/dissolve
      this.operationResult = `Chapter ID ${this.selectedChapterId} dissolved`;
      this.showDissolveChapter = false;
    }
  },
  mounted() {
    console.log("MarinesPage mounted, isLoggedIn:", this.isLoggedIn);

    // ✅ Защита на случай если компонент все же загрузился
    if (!this.isLoggedIn) {
      console.warn("Not authorized! Redirecting...");
      this.$router.push("/login");
    }
  },

  watch: {
    isLoggedIn(newVal) {
      console.log("MarinesPage isLoggedIn changed to:", newVal);

      // ✅ Если авторизация пропала - перенаправляем
      if (!newVal) {
        this.$router.push("/login");
      }
    }
  },
};
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
