import axios from 'axios';
import { ElMessage } from 'element-plus';
import Cookies from 'js-cookie';

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE,
    timeout: 30000
});


// 请求拦截器
request.interceptors.request.use(config => {
        // Only set Content-Type for non-blob requests
        if (config.responseType !== 'blob') {
            config.headers['Content-Type'] = 'application/json;charset=utf-8';
        }
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
        // Skip response code check for blob responses (file downloads/previews)
        if (response.config.responseType === 'blob') {
            return response;
        }
        
        const res = response.data;
        
        // Handle plain string responses (like from /file/share endpoint)
        if (typeof res === 'string') {
            return response;
        }
        
        // Handle JSON responses with code property
        if (res && typeof res === 'object' && res.code !== undefined) {
            if (res.code !== 200 && res.code !== '200') {
                console.log('请求失败：', res.msg || res.message);
                return Promise.reject(new Error(res.msg || res.message || 'Error'));
            }
        }
        
        return response;
    },
    error => {
        console.log('Request error:', error);
        if (error.response && error.response.data) {
            ElMessage.error(error.response.data.msg || error.response.data.message || '请求失败');
        } else {
            ElMessage.error(error.message || '网络连接失败');
        }
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