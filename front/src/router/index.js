import { createRouter, createWebHashHistory } from 'vue-router';
import Login from "@/components/Login.vue";
import Register from "@/components/Register.vue";
import System from "@/components/System.vue";
import Home from '@/components/Home.vue'
import FileList from '@/components/FileList.vue'
import FilePreview from '@/components/FilePreview.vue'
import Person from '@/components/Person.vue'
import Admin from '@/components/PasswordChange.vue'
import AppLayout from "@/components/AppLayout.vue"
import FriendList from "@/components/FriendList.vue";
import PasswordChange from "@/components/PasswordChange.vue";
import Chat from "@/components/Chat.vue";
import Cookies from 'js-cookie';
import { ElMessage } from 'element-plus';

// Authentication guard function
const isAuthenticated = () => {
    const token = Cookies.get('token');
    const userId = Cookies.get('userId');
    
    // Check if both token and userId exist
    if (!token || !userId) {
        return false;
    }
    
    // TODO: In a production app, you might want to validate the token against the server
    // For now, we just check if the token exists and isn't expired
    try {
        // Simple JWT payload decoding to check expiration
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Date.now() / 1000;
        
        if (payload.exp && payload.exp < currentTime) {
            // Token is expired
            return false;
        }
        
        return true;
    } catch (error) {
        // Invalid token format
        console.error('Invalid token format:', error);
        return false;
    }
};

const router = createRouter({
    history: createWebHashHistory(),
    routes: [        {
            path: '/',
            name: 'System',
            component: System,
            beforeEnter: (to, from, next) => {
                // If user is authenticated, redirect to app
                if (isAuthenticated()) {
                    next('/app');
                } else {
                    next();
                }
            }
        },
        {
            path: '/login',
            name: 'Login',
            component: Login
        },
        {
            path: '/register',
            name: 'Register',
            component: Register
        },        {
            path: '/FilePreview/:fileId',
            name: 'FilePreview',
            component: FilePreview,
            props: true,
            meta: { requiresAuth: true }
        },{
            path: '/app',
            component: AppLayout,
            redirect: '/app/home',
            meta: { requiresAuth: true },
            children: [
                { path: 'home', component: Home, meta: { title: '系统首页', requiresAuth: true } },
                { path: 'fileList', component: FileList, meta: { title: '文件管理', requiresAuth: true } },
                { path: 'friendList', component: FriendList, meta: { title: '好友列表', requiresAuth: true } },
                { path: 'chat', component: Chat, meta: { title: '聊天', requiresAuth: true } },
                { path: 'person', component: Person, meta: { title: '个人信息', parentTitle: '信息管理', requiresAuth: true } },
                { path: 'passwordChange', component: PasswordChange, meta: { title: '密码修改', parentTitle: '信息管理', requiresAuth: true } }
            ]
        }]
});

// Global navigation guard
router.beforeEach((to, from, next) => {
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    
    if (requiresAuth) {
        if (isAuthenticated()) {
            next();
        } else {
            // Clear invalid/expired tokens
            Cookies.remove('token');
            Cookies.remove('userId');
            sessionStorage.clear();
            localStorage.removeItem('chatToken');
            
            ElMessage.warning('登录已过期，请重新登录');
            next('/login');
        }
    } else {
        // For public routes (login, register, etc.)
        if (to.path === '/login' || to.path === '/register') {
            // If user is already authenticated and tries to access login/register, redirect to app
            if (isAuthenticated()) {
                next('/app');
                return;
            }
        }
        next();
    }
});

export default router;
