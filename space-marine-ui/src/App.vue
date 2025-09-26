<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const token = ref(null);
const isLoggedIn = ref(false);

// Функция для проверки авторизации
function checkAuth() {
  token.value = localStorage.getItem("token");
  isLoggedIn.value = !!token.value;
  console.log("Auth checked, isLoggedIn:", isLoggedIn.value);
}

// Инициализация при монтировании
onMounted(() => {
  checkAuth();
});

// Следим за storage
window.addEventListener("storage", (event) => {
  if (event.key === "token") {
    console.log("Storage event, new token:", event.newValue);
    checkAuth(); // Перепроверяем авторизацию
  }
});

// 🔥 Следим за изменениями роута - перепроверяем авторизацию
watch(() => router.currentRoute.value.path, (newPath) => {
  console.log("Route changed to:", newPath);
  // При каждом изменении маршрута проверяем авторизацию
  checkAuth();
});

// Выход
function handleLogout() {
  console.log("Logout called");
  localStorage.removeItem("token");
  checkAuth(); // Немедленно обновляем состояние
  router.push("/login");
}
</script>

<template>
  <div id="app">
    <router-view
        :isLoggedIn="isLoggedIn"
        :handleLogout="handleLogout"
    />
  </div>
</template>