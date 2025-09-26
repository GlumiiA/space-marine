<template>
  <table class="table table-striped">
    <thead>
    <tr>
      <th>ID</th>
      <th>Name</th>
      <th>X</th>
      <th>Y</th>
      <th>Creation Date</th>
      <th>Chapter</th>
      <th>Parent Legion</th>
      <th>World</th>
      <th>Health</th>
      <th>Loyal</th>
      <th>Achievements</th>
      <th>Category</th>
      <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="marine in marines" :key="marine.id">
      <td>{{ marine.id }}</td>
      <td>{{ marine.name }}</td>
      <td>{{ marine.coordinates.x }}</td>
      <td>{{ marine.coordinates.y }}</td>
      <td>{{ marine.creationDate }}</td>
      <td>{{ marine.chapter?.name }}</td>
      <td>{{ marine.chapter?.parentLegion }}</td>
      <td>{{ marine.chapter?.world }}</td>
      <td>{{ marine.health }}</td>
      <td>{{ marine.loyal }}</td>
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
    marines: { type: Array, required: true }
  }
};
</script>
