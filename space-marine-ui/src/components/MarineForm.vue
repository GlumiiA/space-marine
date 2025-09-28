<template>
  <div class="modal-overlay">
    <div class="modal-window" ref="modal">
      <div class="modal-header">
        <h5>{{ marine ? "Edit" : "Create" }} SpaceMarine</h5>
        <button class="btn-close" @click="$emit('close')"></button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <!-- Name -->
          <input v-model="form.name" class="form-control mb-2" placeholder="Name"
                 :class="{'is-invalid': errors.name}" required />
          <div class="invalid-feedback">{{ errors.name }}</div>

          <!-- Coordinates -->
          <input v-model.number="form.coordinates.x" type="number" step="0.01" class="form-control mb-2"
                 placeholder="X" :class="{'is-invalid': errors['coordinates.x']}" required />
          <div class="invalid-feedback">{{ errors['coordinates.x'] }}</div>

          <input v-model.number="form.coordinates.y" type="number" step="0.01" class="form-control mb-2"
                 placeholder="Y" :class="{'is-invalid': errors['coordinates.y']}" required />
          <div class="invalid-feedback">{{ errors['coordinates.y'] }}</div>

          <!-- Health -->
          <input v-model.number="form.health" type="number" step="0.1" min="0.1" class="form-control mb-2"
                 placeholder="Health" :class="{'is-invalid': errors.health}" required />
          <div class="invalid-feedback">{{ errors.health }}</div>

          <!-- Loyal -->
          <div class="form-check mb-2">
            <input type="checkbox" v-model="form.loyal" class="form-check-input" id="loyal"/>
            <label class="form-check-label" for="loyal">Loyal</label>
          </div>

          <!-- Achievements -->
          <textarea v-model="form.achievements" class="form-control mb-2"
                    placeholder="Achievements" :class="{'is-invalid': errors.achievements}" required></textarea>
          <div class="invalid-feedback">{{ errors.achievements }}</div>

          <!-- Category -->
          <select v-model="form.category" class="form-select mb-2"
                  :class="{'is-invalid': errors.category}" required>
            <option disabled value="">-- select category --</option>
            <option>SCOUT</option>
            <option>DREADNOUGHT</option>
            <option>ASSAULT</option>
            <option>SUPPRESSOR</option>
            <option>LIBRARIAN</option>
          </select>
          <div class="invalid-feedback">{{ errors.category }}</div>

          <!-- Chapter -->
          <div class="mb-2">
            <label class="form-label">Chapter *</label>
            <select v-model="selectedChapterId" class="form-select mb-2" :disabled="createChapter">
              <option disabled value="">-- select existing --</option>
              <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <div class="form-text">
              или <a href="#" @click.prevent="toggleCreateChapter">
              {{ createChapter ? "cancel new chapter" : "create new" }}
            </a>
            </div>
          </div>

          <!-- New Chapter Form -->
          <div v-if="createChapter" class="border rounded p-2 mb-2">
            <input v-model="newChapter.name" class="form-control mb-1" placeholder="Chapter name"
                   :class="{'is-invalid': errors['chapter.name']}" required />
            <div class="invalid-feedback">{{ errors['chapter.name'] }}</div>
            <input v-model="newChapter.parentLegion" class="form-control mb-1" placeholder="Parent Legion (optional)" />
            <input v-model="newChapter.world" class="form-control mb-1" placeholder="World (optional)" />
          </div>

          <!-- Submit -->
          <button type="submit" class="btn btn-primary w-100">{{ marine ? "Save" : "Create" }}</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  marine: Object,
  chapters: { type: Array, default: () => [] },
  token: String
});
const emit = defineEmits(['close', 'save']);

const form = ref(
    props.marine
        ? JSON.parse(JSON.stringify(props.marine))
        : { name: "", coordinates: { x: 0, y: 0 }, health: 1, loyal: false, achievements: "", category: "", chapter: null }
);

const selectedChapterId = ref(
    props.marine?.chapter?.id || (props.marine && props.marine.chapter?.id) || ""
);
const createChapter = ref(false);
const newChapter = ref({ name: "", parentLegion: "", world: "" });
const errors = ref({});

function toggleCreateChapter() {
  createChapter.value = !createChapter.value;
  if (createChapter.value) selectedChapterId.value = "";
}

function validate() {
  errors.value = {};
  if (!form.value.name.trim()) errors.value.name = "Name required";
  if (form.value.coordinates.x === "" || isNaN(form.value.coordinates.x)) errors.value['coordinates.x'] = "X required";
  if (form.value.coordinates.y === "" || isNaN(form.value.coordinates.y)) errors.value['coordinates.y'] = "Y required";
  if (!form.value.health || form.value.health <= 0) errors.value.health = "Health must be > 0";
  if (!form.value.achievements.trim()) errors.value.achievements = "Achievements required";
  if (!form.value.category) errors.value.category = "Category required";
  if (createChapter.value && !newChapter.value.name.trim()) errors.value['chapter.name'] = "Chapter name required";
  if (!createChapter.value && !selectedChapterId.value) errors.value.chapterId = "Chapter required";

  return Object.keys(errors.value).length === 0;
}


async function submitForm() {
  if (!validate()) return;

  let chapterId = selectedChapterId.value;

// Если создаём новую главу
  if (createChapter.value) {
    const res = await fetch('http://localhost:8080/api/chapters', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${props.token}` },
      body: JSON.stringify(newChapter.value)
    });
    if (!res.ok) {
      throw new Error("Failed to create chapter");
    }
    const createdChapter = await res.json();
    chapterId = createdChapter.id;
  }

  if (!chapterId) {
    errors.value.chapterId = "Chapter is required";
    return;
  }

  const payload = {
    ...form.value,
    coordinates: { x: parseFloat(form.value.coordinates.x), y: parseFloat(form.value.coordinates.y) },
    health: parseFloat(form.value.health),
    chapterId: Number(chapterId)
  };

  await emit('save', payload);
  emit('close');
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-window {
  position: relative;
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  min-width: 350px;
  box-shadow: 0 0 10px rgba(0,0,0,0.5);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 0.5rem;
}
.is-invalid { border-color: red; }
.invalid-feedback { color: red; font-size: 0.85em; display: block; }
</style>
