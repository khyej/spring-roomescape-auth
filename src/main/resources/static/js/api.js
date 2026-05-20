window.api = (function () {
    'use strict';

    function getToken() {
        return localStorage.getItem('token');
    }

    function authHeaders() {
        const token = getToken();
        return token ? {'Authorization': 'Bearer ' + token} : {};
    }

    async function getJson(url, auth = false) {
        const headers = {'Accept': 'application/json', ...(auth ? authHeaders() : {})};
        const res = await fetch(url, {headers});
        if (res.status === 401) {
            location.href = '/login';
            throw new Error('인증이 필요합니다.');
        }
        if (!res.ok) throw await toError(res);
        return res.json();
    }

    async function postJson(url, body, auth = false) {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(auth ? authHeaders() : {})
        };
        const res = await fetch(url, {method: 'POST', headers, body: JSON.stringify(body)});
        if (res.status === 401) {
            location.href = '/login';
            throw new Error('인증이 필요합니다.');
        }
        if (!res.ok) throw await toError(res);
        return res.status === 204 ? null : res.json();
    }

    async function putJson(url, body, auth = false) {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(auth ? authHeaders() : {})
        };
        const res = await fetch(url, {method: 'PUT', headers, body: JSON.stringify(body)});
        if (res.status === 401) {
            location.href = '/login';
            throw new Error('인증이 필요합니다.');
        }
        if (!res.ok) throw await toError(res);
        return res.status === 204 ? null : res.json();
    }

    async function del(url, auth = false) {
        const headers = {...(auth ? authHeaders() : {})};
        const res = await fetch(url, {method: 'DELETE', headers});
        if (res.status === 401) {
            location.href = '/login';
            throw new Error('인증이 필요합니다.');
        }
        if (!res.ok) throw await toError(res);
        return null;
    }

    async function toError(res) {
        let message = '';
        try {
            const contentType = res.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                const json = await res.json();
                message = json.message || JSON.stringify(json);
            } else {
                message = await res.text();
            }
        } catch (_) {
        }
        const err = new Error(message || ('HTTP ' + res.status));
        err.status = res.status;
        return err;
    }

    async function fetchAllPages(basePath, size = 100) {
        const all = [];
        let page = 0;
        while (true) {
            const data = await getJson(`${basePath}?page=${page}&size=${size}`);
            const items = Array.isArray(data) ? data : Object.values(data).find(Array.isArray) || [];
            all.push(...items);
            if (!data.hasNext) break;
            page++;
        }
        return all;
    }

    async function logout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: authHeaders()
            });
        } finally {
            localStorage.removeItem('token');
            location.href = '/login';
        }
    }

    return {
        isLoggedIn: () => !!getToken(),
        logout,

        listThemes: async (page = 0, size = 10) => {
            const data = await getJson(`/api/themes?page=${page}&size=${size}`);
            return {items: data.themes || [], hasNext: data.hasNext ?? false};
        },
        listAllThemes: () => fetchAllPages('/api/themes'),
        popularThemes: async () => {
            const data = await getJson('/api/themes/popular');
            return data.themes || [];
        },

        listMyReservations: async () => {
            const data = await getJson('/api/reservations/my', true);
            return {items: data.reservations || []};
        },

        createReservation: (payload) => postJson('/api/reservations', payload, true),
        deleteReservation: (id) => del('/api/reservations/' + id, true),
        updateReservation: (id, payload) => putJson('/api/reservations/' + id, payload, true),
        deleteReservationByAdmin: (id) => del('/api/admin/reservations/' + id),

        listTimes: async () => {
            const data = await getJson('/api/times');
            return data.reservationTimes || [];
        },
        availableTimes: async (themeId, date) => {
            const data = await getJson('/api/times/available?theme_id=' + encodeURIComponent(themeId) + '&date=' + encodeURIComponent(date));
            return data.reservationTimes || [];
        },
        createTheme: (payload) => postJson('/api/admin/themes', payload),
        deleteTheme: (id) => del('/api/admin/themes/' + id),
        createTime: (payload) => postJson('/api/admin/times', payload),
        deleteTime: (id) => del('/api/admin/times/' + id)
    };
})();
