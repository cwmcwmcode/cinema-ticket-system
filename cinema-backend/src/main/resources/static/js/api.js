/* 与后端接口交互的公共封装 */
const API = (function () {
    const USER_KEY = 'cinema_user';

    async function request(url, options) {
        const opts = Object.assign({ method: 'GET' }, options || {});
        if (opts.body !== undefined) {
            opts.headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
            opts.body = JSON.stringify(opts.body);
        }
        let res;
        try {
            res = await fetch(url, opts);
        } catch (e) {
            throw new Error('无法连接后端服务, 请确认 http://localhost:8082 已启动');
        }
        let json;
        try {
            json = await res.json();
        } catch (e) {
            throw new Error('服务响应异常 (HTTP ' + res.status + ')');
        }
        if (json.code !== 200) {
            throw new Error(json.message || '请求失败');
        }
        return json.data;
    }

    return {
        get: function (url) {
            return request(url);
        },
        post: function (url, body) {
            return request(url, { method: 'POST', body: body });
        },
        put: function (url, body) {
            return request(url, { method: 'PUT', body: body === undefined ? {} : body });
        },
        del: function (url) {
            return request(url, { method: 'DELETE' });
        },
        getUser: function () {
            try {
                return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
            } catch (e) {
                return null;
            }
        },
        setUser: function (user) {
            localStorage.setItem(USER_KEY, JSON.stringify(user));
        },
        logout: function () {
            localStorage.removeItem(USER_KEY);
        }
    };
})();

/* 金额格式化 */
function money(value) {
    return '¥' + Number(value || 0).toFixed(2);
}

/* 电影评分(展示用, 后端暂无评分字段, 按 id 生成稳定值) */
function movieRating(id) {
    return (7.6 + ((Number(id) * 7) % 22) / 10).toFixed(1);
}

/* 日期时间格式化: 2026-11-10 10:00:00 -> 11-10 10:00 */
function shortTime(value) {
    if (!value) {
        return '';
    }
    return String(value).substring(5, 16);
}

function dayOf(value) {
    return value ? String(value).substring(0, 10) : '';
}

function clockOf(value) {
    return value ? String(value).substring(11, 16) : '';
}

function statusText(status) {
    return { 0: '未支付', 1: '已支付', 2: '已取消' }[status] || '未知';
}

function statusType(status) {
    return { 0: 'warning', 1: 'success', 2: 'info' }[status] || 'info';
}

/* 取票码: 订单号后 8 位, 每 4 位分组 */
function ticketCode(orderNo) {
    if (!orderNo) {
        return '--------';
    }
    const tail = String(orderNo).slice(-8);
    return tail.slice(0, 4) + ' ' + tail.slice(4);
}

/* 未登录时跳转首页登录 */
function requireLogin() {
    const user = API.getUser();
    if (!user) {
        window.location.href = 'index.html?login=1&redirect=' + encodeURIComponent(location.pathname.split('/').pop() + location.search);
        return null;
    }
    return user;
}
