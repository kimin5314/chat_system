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

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'System',
            component: System
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
        },
        {
            path: '/FilePreview/:fileId',
            name: 'FilePreview',
            component: FilePreview,
            props: true
        },        {
            path: '/app',
            component: AppLayout,
            redirect: '/app/home',
            children: [
                { path: 'home', component: Home, meta: { title: '系统首页' } },
                { path: 'fileList', component: FileList, meta: { title: '文件管理' } },
                { path: 'friendList', component: FriendList, meta: { title: '好友列表' } },
                { path: 'chat', component: Chat, meta: { title: '聊天' } },
                { path: 'person', component: Person, meta: { title: '个人信息', parentTitle: '信息管理' } },
                { path: 'passwordChange', component: PasswordChange, meta: { title: '密码修改', parentTitle: '信息管理' } }
            ]
        }    ]
});

export default router;
