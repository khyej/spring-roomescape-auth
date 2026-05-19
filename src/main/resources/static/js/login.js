(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('login-form');

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            try {
                const res = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({username, password})
                });

                if (!res.ok) {
                    const data = await res.json();
                    modal.alert({title: '로그인 실패', message: data.message || '아이디 또는 비밀번호를 확인하세요.'});
                    return;
                }

                const data = await res.json();
                localStorage.setItem('token', data.token);
                location.href = '/';
            } catch (e) {
                modal.alert({title: '오류', message: e.message});
            }
        });
    });
})();
