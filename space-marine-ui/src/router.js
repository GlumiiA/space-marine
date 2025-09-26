import { createRouter, createWebHistory } from 'vue-router';
import MarinesPage from './pages/MarinesPage.vue';
import SpecialOpsPage from './pages/SpecialOpsPage.vue';
import LoginPage from './pages/LoginPage.vue';

const routes = [
    {
        path: '/',
        name: 'Marines',
        component: MarinesPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/special-ops',
        name: 'SpecialOps',
        component: SpecialOpsPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginPage,
        meta: { requiresGuest: true }
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const isLoggedIn = !!localStorage.getItem('token');

    if (to.meta.requiresAuth && !isLoggedIn) {
        next('/login');  // Перенаправляем на логин
    } else if (to.meta.requiresGuest && isLoggedIn) {
        next('/');       // Перенаправляем на главную
    } else {
        next();          // Разрешаем переход
    }
});

export default router;