<template>
  <div id="app">
    <router-view
        :isLoggedIn="isLoggedIn"
        :token="token"
        :currentUser="currentUser"
        @login="handleLogin"
        @logout="handleLogout"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { decodeJwt } from './utils/decodeJwt';

const router = useRouter();
const token = ref(null);
const isLoggedIn = ref(false);
const currentUser = ref(null);

function checkAuth() {
  token.value = localStorage.getItem("token");
  isLoggedIn.value = !!token.value;

  if (token.value) {
    const decoded = decodeJwt(token.value);
    currentUser.value = decoded?.sub || null;
  } else {
    currentUser.value = null;
  }

  console.log("Auth checked:", {
    isLoggedIn: isLoggedIn.value,
    currentUser: currentUser.value,
    token: token.value ? token.value.substring(0, 20) + '...' : null
  });
}

function handleLogin(authData) {
  console.log('Handle login called with:', authData);
  localStorage.setItem("token", authData.token);
  checkAuth();
}

function handleLogout() {
  localStorage.removeItem("token");
  checkAuth();
  router.push("/login");
}

onMounted(() => {
  checkAuth();
});

window.addEventListener("storage", (event) => {
  if (event.key === "token") {
    checkAuth();
  }
});

watch(() => router.currentRoute.value.path, () => {
  checkAuth();
});
</script>