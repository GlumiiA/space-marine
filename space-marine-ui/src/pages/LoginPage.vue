<template>
  <div class="materialContainer">
    <!-- Блок логина -->
    <div class="box" v-if="mode === 'login'">
      <div class="title">LOGIN</div>

      <div class="input">
        <label for="username">Username</label>
        <input
            v-model="username"
            type="text"
            id="username"
            required
            @focus="onFocus($event)"
            @blur="onBlur($event)"
        />
        <span class="spin"></span>
      </div>

      <div class="input">
        <label for="password">Password</label>
        <input
            v-model="password"
            type="password"
            id="password"
            required
            @focus="onFocus($event)"
            @blur="onBlur($event)"
        />
        <span class="spin"></span>
      </div>

      <div class="button login">
        <button type="button" @click="login"><span>GO</span></button>
      </div>

      <br />
      <div class="alt-button">
        <button type="button" @click="mode = 'register'">Create account</button>
      </div>
    </div>

    <div class="box" v-else>
      <div class="title">REGISTER</div>

      <div class="input">
        <label for="regUsername">Username</label>
        <input
            v-model="regUsername"
            type="text"
            id="regUsername"
            required
            @focus="onFocus($event)"
            @blur="onBlur($event)"
        />
        <span class="spin"></span>
      </div>

      <div class="input">
        <label for="regPassword">Password</label>
        <input
            v-model="regPassword"
            type="password"
            id="regPassword"
            required
            @focus="onFocus($event)"
            @blur="onBlur($event)"
        />
        <span class="spin"></span>
      </div>

      <div class="input">
        <label for="regRepeat">Repeat Password</label>
        <input
            v-model="regRepeat"
            type="password"
            id="regRepeat"
            required
            @focus="onFocus($event)"
            @blur="onBlur($event)"
        />
        <span class="spin"></span>
      </div>

      <div class="button">
        <button type="button" @click="register"><span>NEXT</span></button>
      </div>

      <div class="alt-button">
        <button type="button" @click="mode = 'login'">Back to login</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const username = ref("");
const password = ref("");
const regUsername = ref("");
const regPassword = ref("");
const regRepeat = ref("");
const mode = ref("login");
const loading = ref(false);
const router = useRouter();
const errorMessage = ref("");
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

// Функция для входа
async function login() {
  if (!username.value || !password.value) {
    errorMessage.value = "Please fill in all fields";
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const res = await fetch(`${apiBaseUrl}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      },
      credentials: "include",
      body: JSON.stringify({
        username: username.value,
        password: password.value
      }),
    });

    console.log("Response status:", res.status);
    console.log("Response headers:", Object.fromEntries(res.headers.entries()));

    if (!res.ok) {
      let errData;
      try {
        errData = await res.json();
      } catch (e) {
        errData = { message: `HTTP error! status: ${res.status}` };
      }
      throw new Error(errData.message || "Login failed");
    }

    const data = await res.json();
    console.log("Login success:", data);

    localStorage.setItem("token", data.token);
    router.push("/");
  } catch (err) {
    console.error("Login error:", err);
    errorMessage.value = err.message;
  } finally {
    loading.value = false;
  }
}

async function register() {
  if (!regUsername.value || !regPassword.value || !regRepeat.value) {
    errorMessage.value = "Please fill in all fields";
    return;
  }

  if (regPassword.value !== regRepeat.value) {
    errorMessage.value = "Passwords do not match";
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const res = await fetch(`${apiBaseUrl}/api/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      },
      credentials: "include",
      body: JSON.stringify({
        username: regUsername.value,
        password: regPassword.value,
      }),
    });

    console.log("Registration response status:", res.status);

    if (!res.ok) {
      let errData;
      try {
        errData = await res.json();
      } catch (e) {
        errData = { message: `HTTP error! status: ${res.status}` };
      }
      throw new Error(errData.message || "Registration failed");
    }

    alert(`User ${regUsername.value} registered!`);
    mode.value = "login";
  } catch (err) {
    console.error("Registration error:", err);
    errorMessage.value = err.message;
  } finally {
    loading.value = false;
  }
}

// Анимация label
function onFocus(e) {
  const label = e.target.previousElementSibling;
  label.style.lineHeight = "18px";
  label.style.fontSize = "18px";
  label.style.top = "0px";
  const spin = e.target.nextElementSibling;
  spin.style.width = "100%";
}

function onBlur(e) {
  const label = e.target.previousElementSibling;
  const spin = e.target.nextElementSibling;
  spin.style.width = "0";
  if (!e.target.value) {
    label.style.lineHeight = "60px";
    label.style.fontSize = "24px";
    label.style.top = "10px";
  }
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  background: url('@/assets/background.jpg') no-repeat center center fixed;
  background-size: cover;
}
.box {
  position: relative;
  top: 0;
  opacity: 1;
  float: left;
  padding: 60px 50px 40px 50px;
  width: 100%;
  background: #fff;
  border-radius: 10px;
  transform: scale(1);
  -webkit-transform: scale(1);
  -ms-transform: scale(1);
  z-index: 5;

  &.back {
    transform: scale(0.95);
    -webkit-transform: scale(0.95);
    -ms-transform: scale(0.95);
    top: -20px;
    opacity: .8;
    z-index: -1;
  }

  &:before {
    content: "";
    width: 100%;
    height: 30px;
    border-radius: 10px;
    position: absolute;
    top: -10px;
    background: rgba(255, 255, 255, 0.6);
    left: 0;
    transform: scale(0.95);
    -webkit-transform: scale(0.95);
    -ms-transform: scale(0.95);
    z-index: -1;
  }
}

.overbox .title {
  color: #fff;

  &:before {
    background: #fff;
  }
}

.title {
  width: 100%;
  float: left;
  line-height: 46px;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #ED2553;
  position: relative;

  &:before {
    content: "";
    width: 5px;
    height: 100%;
    position: absolute;
    top: 0;
    left: -50px;
    background: #ED2553;
  }
}

.input {
  transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);

  label, input, .spin {
    transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.button {
  transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);

  button .button.login button i.fa {
    transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.material-button .shape {
  &:before, &:after {
    transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
    -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.button.login button {
  transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -webkit-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
  -ms-transition: 300ms cubic-bezier(0.4, 0, 0.2, 1);
}

.material-button, .alt-2, .material-button .shape, .alt-2 .shape, .box {
  transition: 400ms cubic-bezier(0.4, 0, 0.2, 1);
  -webkit-transition: 400ms cubic-bezier(0.4, 0, 0.2, 1);
  -ms-transition: 400ms cubic-bezier(0.4, 0, 0.2, 1);
}

.input {
  width: 100%;
  float: left;

  label, input, .spin {
    width: 100%;
    float: left;
  }
}

.button {
  width: 100%;
  float: left;

  button {
    width: 100%;
    float: left;
  }
}

.input, .button {
  margin-top: 30px;
  height: 70px;
}

.input {
  position: relative;

  input {
    position: relative;
  }
}

.button {
  position: relative;

  button {
    position: relative;
  }
}

.input {
  input {
    height: 60px;
    top: 10px;
    border: none;
    background: transparent;
    font-family: 'Roboto', sans-serif;
    font-size: 24px;
    color: rgba(0, 0, 0, 0.8);
    font-weight: 300;
  }

  label {
    font-family: 'Roboto', sans-serif;
    font-size: 24px;
    color: rgba(0, 0, 0, 0.8);
    font-weight: 300;
  }
}

.button button {
  font-family: 'Roboto', sans-serif;
  font-size: 24px;
  color: rgba(0, 0, 0, 0.8);
  font-weight: 300;
}

.input {
  &:before, .spin {
    width: 100%;
    height: 1px;
    position: absolute;
    bottom: 0;
    left: 0;
  }

  &:before {
    content: "";
    background: rgba(0, 0, 0, 0.1);
    z-index: 3;
  }

  .spin {
    background: #ED2553;
    z-index: 4;
    width: 0;
  }
}

.overbox .input {
  .spin {
    background: rgba(255, 255, 255, 1);
  }

  &:before {
    background: rgba(255, 255, 255, 0.5);
  }
}

.input label {
  position: absolute;
  top: 10px;
  left: 0;
  z-index: 2;
  cursor: pointer;
  line-height: 60px;
}

.button {
  &.login {
    width: 60%;
    left: 20%;

    button {
      width: 100%;
      line-height: 64px;
      left: 0%;
      background-color: transparent;
      border: 3px solid rgba(0, 0, 0, 0.1);
      font-weight: 900;
      font-size: 18px;
      color: rgba(0, 0, 0, 0.2);
    }
  }

  button {
    width: 100%;
    line-height: 64px;
    left: 0%;
    background-color: transparent;
    border: 3px solid rgba(0, 0, 0, 0.1);
    font-weight: 900;
    font-size: 18px;
    color: rgba(0, 0, 0, 0.2);
  }

  &.login {
    margin-top: 30px;
  }

  margin-top: 20px;

  button {
    background-color: #fff;
    color: #ED2553;
    border: none;
  }

  &.login button {
    &.active {
      border: 3px solid transparent;
      color: #fff !important;

      span {
        opacity: 0;
        transform: scale(0);
        -webkit-transform: scale(0);
        -ms-transform: scale(0);
      }

      i.fa {
        opacity: 1;
        transform: scale(1) rotate(-0deg);
        -webkit-transform: scale(1) rotate(-0deg);
        -ms-transform: scale(1) rotate(-0deg);
      }
    }

    i.fa {
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
      line-height: 60px;
      transform: scale(0) rotate(-45deg);
      -webkit-transform: scale(0) rotate(-45deg);
      -ms-transform: scale(0) rotate(-45deg);
    }

    &:hover {
      color: #ED2553;
      border-color: #ED2553;
    }
  }

  margin: 40px 0;
  overflow: hidden;
  z-index: 2;

  button {
    cursor: pointer;
    position: relative;
    z-index: 2;
  }
}

.overbox {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  overflow: inherit;
  border-radius: 10px;
  padding: 60px 50px 40px 50px;

  .title, .button, .input {
    z-index: 111;
    position: relative;
    color: #fff !important;
    display: none;
  }

  .title {
    width: 80%;
  }

  .input {
    margin-top: 20px;

    input, label {
      color: #fff;
    }
  }
}

.material-button .shape, .alt-2 .shape {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
}

.material-button .shape:before, .alt-2 .shape:before, .material-button .shape:after, .alt-2 .shape:after {
  content: "";
  background: #fff;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(360deg);
  -webkit-transform: translate(-50%, -50%) rotate(360deg);
  -ms-transform: translate(-50%, -50%) rotate(360deg);
}

.material-button .shape:before, .alt-2 .shape:before {
  width: 25px;
  height: 4px;
}

.material-button .shape:after, .alt-2 .shape:after {
  height: 25px;
  width: 4px;
}

html {
  overflow: hidden;
}

.materialContainer {
  width: 100%;
  max-width: 460px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  -webkit-transform: translate(-50%, -50%);
  -ms-transform: translate(-50%, -50%);
}

* {
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  text-decoration: none;
  list-style-type: none;
  outline: none;

  &:after, &::before {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    text-decoration: none;
    list-style-type: none;
    outline: none;
  }
}
.alt-button {
  width: 100%;
  margin-top: 20px;
  text-align: center;
}

.alt-button button {
  background: transparent;
  border: 2px solid #ED2553;
  color: #ED2553;
  font-size: 16px;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.alt-button button:hover {
  background: #ED2553;
  color: #fff;
}
</style>