import axios from 'axios'
import {Message} from 'element-ui';
import router from '../router'
import {mymessage} from '@/utils/mymessage';

axios.interceptors.response.use(success => {
    if (success.status && success.status == 200 && success.data.status == 500) {
        Message.error({message: success.data.msg})
        return;
    }
    if (success.data.msg) {
        Message.success({message: success.data.msg})
    }
    return success.data;
}, error => {
    if (error.response.status == 504 || error.response.status == 404) {
        Message.error({message: '服务器被吃了( ╯□╰ )'})
    } else if (error.response.status == 403) {
        Message.error({message: '权限不足，请联系管理员'})
    } else if (error.response.status == 401) {
        mymessage.error({message: error.response.data.msg ? error.response.data.msg : '尚未登录，请登录'})
        router.replace('/');
    } else {
        if (error.response.data.msg) {
            Message.error({message: error.response.data.msg})
        } else {
            Message.error({message: '未知错误!'})
        }
    }
    return;
})

let base = '';

export const postKeyValueRequest = (url, params) => {
    return axios({
        method: 'post',
        url: `${base}${url}`,
        data: params,
        transformRequest: [function (data) {
            let ret = '';
            for (let i in data) {
                ret += encodeURIComponent(i) + '=' + encodeURIComponent(data[i]) + '&'
            }
            return ret;
        }],
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });
}
/*export const postRequest = (url, params) => {
    return axios({
        method: 'post',
        url: `${base}${url}`,
        data: params
    })
}*/
export const postRequest = (url, params,responseType = "json") => {
    return axios({
        method: 'post',
        url: `${base}${url}`,
        data: params,
        responseType: responseType // 允许传入 responseType 参数
    })
}
export const putRequest = (url, params) => {
    return axios({
        method: 'put',
        url: `${base}${url}`,
        data: params
    })
}

//vue前端中的封装的GET请求方法，并返回 axios 处理的 Promise。
export const getRequest = (url, params,responseType = "json") => {
    return axios({
        method: 'get',
        url: `${base}${url}`,//拼接完整的请求地址，base是API的基础路径
        params: params,//get请求的参数，axio会自动转换为查询字符
        responseType: responseType // 允许传入 responseType 参数
    })
}
/*
//vue前端中的封装的GET请求方法，并返回 axios 处理的 Promise。
export const getRequest = (url, params) => {
    return axios({
        method: 'get',
        url: `${base}${url}`,//拼接完整的请求地址，base是API的基础路径
        params: params//get请求的参数，axio会自动转换为查询字符
    })
}*/
export const deleteRequest = (url, params) => {
    return axios({
        method: 'delete',
        url: `${base}${url}`,
        params: params
    })
}
