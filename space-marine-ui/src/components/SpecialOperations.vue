<template>
  <div class="ops-panel">
    <h2 class="title">⚙️ Special Operations</h2>

    <!-- Sum health -->
    <div class="op-card">
      <button class="btn btn-primary w-100" @click="sumHealth">
        Calculate Sum Health
      </button>
      <p v-if="sumHealthResult !== null" class="result">
        Total health sum: <strong>{{ sumHealthResult }}</strong>
      </p>
    </div>

    <!-- Avg health -->
    <div class="op-card">
      <button class="btn btn-success w-100" @click="avgHealth">
        Calculate Avg Health
      </button>
      <p v-if="avgHealthResult !== null" class="result">
        Average health: <strong>{{ avgHealthResult }}</strong>
      </p>
    </div>

    <!-- Min coordinates -->
    <div class="op-card">
      <button class="btn btn-info w-100" @click="minCoordinates">
        Find Min Coordinates
      </button>
      <p v-if="minCoordinatesResult" class="result">
        Min coordinates:<br />
        <strong>ID:</strong> {{ minCoordinatesResult.id }} <br />
        <strong>Name:</strong> {{ minCoordinatesResult.name }} <br />
        <strong>Coordinates:</strong>
        ({{ minCoordinatesResult.coordinates.x }},
        {{ minCoordinatesResult.coordinates.y }})
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const props = defineProps({ token: String });

const sumHealthResult = ref(null);
const avgHealthResult = ref(null);
const minCoordinatesResult = ref(null);

async function sumHealth() {
  const res = await fetch("http://localhost:8080/api/space-marines/ops/sum-health", {
    headers: { "Authorization": `Bearer ${props.token}` }
  });
  sumHealthResult.value = await res.text();
}

async function avgHealth() {
  const res = await fetch("http://localhost:8080/api/space-marines/ops/avg-health", {
    headers: { "Authorization": `Bearer ${props.token}` }
  });
  avgHealthResult.value = await res.text();
}

async function minCoordinates() {
  const res = await fetch("http://localhost:8080/api/space-marines/ops/min-coordinates", {
    headers: { "Authorization": `Bearer ${props.token}` }
  });
  minCoordinatesResult.value = await res.json();
}
</script>

<style scoped>
.ops-panel {
  margin-top: 1.5rem;
  display: grid;
  gap: 1rem;
}

.title {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #fff;
}

.op-card {
  background: #fff;
  padding: 1rem;
  border-radius: 0.75rem;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  transition: transform 0.15s ease;
}

.op-card:hover {
  transform: translateY(-2px);
}

.result {
  margin-top: 0.75rem;
  font-size: 0.95rem;
  color: #444;
}
</style>
