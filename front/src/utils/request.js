import axios from 'axios';
import { ElMessage } from 'element-plus';
import Cookies from 'js-cookie';

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE || "http://localhost:9090",
    timeout: 30000
});


// 请求拦截器
request.interceptors.request.use(config => {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
        return config;
    },
    error => {
        console.log(error);
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data;
        
        // 根据实际业务需求判断响应状态
        if (res.code !== 200 && res.code !== '200') {
            console.log('请求失败：', res.message);
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        console.log('Request error:', error);
        ElMessage.error(error.message || '请求失败');
        return Promise.reject(error);
    }
);

// 封装 get 请求
export function get(url, params = {}) {
    return request({
        url,
        method: 'get',
        params
    });
}

// 封装 post 请求
export function post(url, data = {}) {
    return request({
        url,
        method: 'post',
        data
    });
}

// 封装 put 请求
export function put(url, data = {}) {
    return request({
        url,
        method: 'put',
        data
    });
}

// 封装 delete 请求
export function del(url, params = {}) {
    return request({
        url,
        method: 'delete',
        params
    });
}

export default request