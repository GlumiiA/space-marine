<template>
  <table class="table table-striped">
    <thead>
    <tr>
      <th @click="changeSort('id')">
        ID
        <span v-if="sortKey === 'id'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('name')">
        Name
        <span v-if="sortKey === 'name'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('coordinates.x')">
        X
        <span v-if="sortKey === 'coordinates.x'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('coordinates.y')">
        Y
        <span v-if="sortKey === 'coordinates.y'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('creationDate')">
        Creation Date
        <span v-if="sortKey === 'creationDate'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('chapter.name')">
        Chapter
        <span v-if="sortKey === 'chapter.name'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('health')">
        Health
        <span v-if="sortKey === 'health'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('loyal')">
        Loyal
        <span v-if="sortKey === 'loyal'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('achievements')">
        Achievements
        <span v-if="sortKey === 'achievements'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th @click="changeSort('category')">
        Category
        <span v-if="sortKey === 'category'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
      </th>
      <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="marine in marines" :key="marine.id">
      <td>{{ marine.id }}</td>
      <td>{{ marine.name }}</td>
      <td>{{ marine.coordinates.x }}</td>
      <td>{{ marine.coordinates.y }}</td>
      <td>
        <div class="date-time-display">
          <span class="date">{{ formatDate(marine.creationDate) }}</span>
          <span class="time">{{ formatTime(marine.creationDate) }}</span>
        </div>
      </td>
      <td>{{ marine.chapter?.name || 'No chapter' }}</td>
      <td>{{ marine.health }}</td>
      <td>
        <span class="loyal-badge" :class="{ 'loyal': marine.loyal, 'not-loyal': !marine.loyal }">
          {{ marine.loyal ? 'Yes' : 'No' }}
        </span>
      </td>
      <td>{{ marine.achievements }}</td>
      <td>{{ marine.category }}</td>
      <td>
        <button class="btn btn-sm btn-info me-1" @click="$emit('view', marine)">View</button>
        <button class="btn btn-sm btn-warning me-1" @click="$emit('edit', marine)">Edit</button>
        <button class="btn btn-sm btn-danger" @click="$emit('delete', marine)">Delete</button>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  props: {
    marines: { type: Array, required: true },
    sortKey: { type: String, default: '' },
    sortDir: { type: String, default: 'asc' }
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return 'N/A';

      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'Invalid Date';

        return date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        });
      } catch (error) {
        console.error('Date formatting error:', error);
        return 'Error';
      }
    },
    formatTime(dateString) {
      if (!dateString) return 'N/A';

      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'Invalid Time';

        return date.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        });
      } catch (error) {
        console.error('Time formatting error:', error);
        return 'Error';
      }
    },
    changeSort(key) {
      this.$emit('changeSort', key);
    }
  }
};

</script>

<style scoped>
.date-time-display {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date {
  font-weight: 500;
  font-size: 0.9rem;
}

.time {
  color: #666;
  font-size: 0.8rem;
  font-family: 'Courier New', monospace;
}

.loyal-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.loyal {
  background-color: #d4edda;
  color: #155724;
}

.not-loyal {
  background-color: #f8d7da;
  color: #721c24;
}

.table td {
  vertical-align: middle;
}

.btn {
  margin: 1px;
}
</style>