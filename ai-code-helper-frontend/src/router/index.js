import { createRouter, createWebHistory } from 'vue-router'

// 页面组件
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Chat from '../views/Chat.vue'

const routes = [
    {
        path: '/',
        component: Chat
    },
    {
        path: '/login',
        component: Login
    },
    {
        path: '/register',
        component: Register
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userId = localStorage.getItem("userId")

    // 如果没登录，并且访问的不是登录/注册页
    if (!userId && to.path !== '/login' && to.path !== '/register') {
        next('/login') // 强制跳登录
    } else {
        next() // 放行
    }
})

export default router