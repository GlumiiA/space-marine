<template>
  <div class="modal-overlay">
    <div class="modal-window" ref="modal">
      <div class="modal-header">
        <h5>{{ marine ? 'Edit' : 'Create' }} SpaceMarine</h5>
        <button class="btn-close" @click="$emit('close')"></button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="save">
          <input v-model="form.name" class="form-control mb-2" placeholder="Name" required />
          <input v-model.number="form.coordinates.x" type="number" step="0.01" class="form-control mb-2" placeholder="X" required />
          <input v-model.number="form.coordinates.y" type="number" step="0.01" class="form-control mb-2" placeholder="Y" required />
          <input v-model.number="form.health" type="number" step="0.1" min="0.1" class="form-control mb-2" placeholder="Health" required />
          <div class="form-check mb-2">
            <input type="checkbox" v-model="form.loyal" class="form-check-input" id="loyal">
            <label class="form-check-label" for="loyal">Loyal</label>
          </div>
          <textarea v-model="form.achievements" class="form-control mb-2" placeholder="Achievements" required></textarea>
          <select v-model="form.category" class="form-select mb-2" required>
            <option>SCOUT</option>
            <option>DREADNOUGHT</option>
            <option>ASSAULT</option>
            <option>SUPPRESSOR</option>
            <option>LIBRARIAN</option>
          </select>
          <input v-model="form.chapter.name" class="form-control mb-2" placeholder="Chapter name" required />
          <button type="submit" class="btn btn-primary w-100">{{ marine ? 'Save' : 'Create' }}</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const props = defineProps({ marine: Object });
const emit = defineEmits(['close', 'save']);
const form = ref(props.marine ? JSON.parse(JSON.stringify(props.marine)) : {
  name: '', coordinates: { x: 0, y: 0 }, health: 1,
  loyal: false, achievements: '', category: 'SCOUT', chapter: { name: '' }
});

const modal = ref(null);

// Перетаскивание модального окна
onMounted(() => {
  const el = modal.value;
  let isDragging = false, startX, startY, origX, origY;

  el.querySelector('.modal-header').addEventListener('mousedown', e => {
    isDragging = true;
    startX = e.clientX; startY = e.clientY;
    const rect = el.getBoundingClientRect();
    origX = rect.left; origY = rect.top;
    document.body.style.userSelect = 'none';
  });

  document.addEventListener('mousemove', e => {
    if (isDragging) {
      el.style.left = origX + (e.clientX - startX) + 'px';
      el.style.top = origY + (e.clientY - startY) + 'px';
    }
  });

  document.addEventListener('mouseup', () => {
    isDragging = false;
    document.body.style.userSelect = '';
  });
});

function save() {
  emit('save', form.value);
  emit('close');
}
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
  position: absolute;
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  min-width: 300px;
  box-shadow: 0 0 10px rgba(0,0,0,0.5);
  cursor: move;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  cursor: grab;
}
</style>
