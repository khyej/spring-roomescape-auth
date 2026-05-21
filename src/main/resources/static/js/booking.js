(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', async function () {
        if (!api.isLoggedIn()) {
            location.href = '/login';
            return;
        }

        const params = new URLSearchParams(location.search);
        let currentThemeId = params.get('themeId');

        const today = new Date().toISOString().slice(0, 10);
        const storeSelect = document.getElementById('store');
        const themeSelect = document.getElementById('theme');
        const dateInput = document.getElementById('date');
        const dateSubmit = document.getElementById('date-submit');
        const dateForm = document.getElementById('date-form');
        const slotsArea = document.getElementById('slots-area');
        const slotsList = document.getElementById('slots-list');
        const slotsCount = document.getElementById('slots-count');
        const slotsDate = document.getElementById('slots-date');
        const slotsEmpty = document.getElementById('slots-empty');
        const reserveForm = document.getElementById('reserve-form');
        const todayLabel = document.getElementById('today-label');

        if (todayLabel) todayLabel.textContent = today;
        if (dateInput) {
            dateInput.min = today;
            if (!dateInput.value) dateInput.value = today;
        }

        let stores = [];
        let allThemes = [];

        try {
            [stores, allThemes] = await Promise.all([
                api.listStores(),
                api.listAllThemes()
            ]);

            stores.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.name;
                storeSelect.appendChild(opt);
            });

            if (currentThemeId) {
                const theme = allThemes.find(t => String(t.id) === String(currentThemeId));
                if (theme) {
                    storeSelect.value = theme.storeId;
                    await updateThemes(theme.storeId);
                    themeSelect.value = currentThemeId;
                    renderBanner(theme);
                    enableDateSelection(true);
                }
            }
        } catch (e) {
            modal.alert({title: '로드 실패', message: e.message});
        }

        storeSelect.addEventListener('change', async (e) => {
            const storeId = e.target.value;
            currentThemeId = null;
            enableDateSelection(false);
            if (storeId) {
                await updateThemes(storeId);
            } else {
                themeSelect.innerHTML = '<option value="">테마 선택...</option>';
                themeSelect.disabled = true;
            }
            renderBanner(null);
        });

        themeSelect.addEventListener('change', (e) => {
            currentThemeId = e.target.value;
            if (currentThemeId) {
                const theme = allThemes.find(t => String(t.id) === String(currentThemeId));
                renderBanner(theme);
                enableDateSelection(true);
            } else {
                renderBanner(null);
                enableDateSelection(false);
            }
        });

        async function updateThemes(storeId) {
            const filteredThemes = allThemes.filter(t => String(t.storeId) === String(storeId));
            themeSelect.innerHTML = '<option value="">테마 선택...</option>';
            filteredThemes.forEach(t => {
                const opt = document.createElement('option');
                opt.value = t.id;
                opt.textContent = t.name;
                themeSelect.appendChild(opt);
            });
            themeSelect.disabled = false;
        }

        function enableDateSelection(enabled) {
            dateInput.disabled = !enabled;
            dateSubmit.disabled = !enabled;
            if (!enabled) {
                slotsArea.style.display = 'none';
            }
        }

        dateForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = dateInput.value;
            if (!date || !currentThemeId) return;
            if (date < today) {
                await modal.alert({title: '잘못된 일자', message: '오늘 이후의 날짜만 선택할 수 있습니다.'});
                return;
            }
            await loadSlots(currentThemeId, date);
        });

        reserveForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = dateInput.value;
            if (date < today) {
                await modal.alert({title: '잘못된 일자', message: '오늘 이후의 날짜만 선택할 수 있습니다.'});
                return;
            }
            const fd = new FormData(reserveForm);
            const payload = {
                themeId: Number(currentThemeId),
                date: date,
                timeId: Number(fd.get('timeId'))
            };
            if (!payload.timeId) {
                await modal.alert({message: '시간을 선택하세요.'});
                return;
            }
            try {
                await api.createReservation(payload);
                location.href = '/reservations';
            } catch (err) {
                await modal.alert({title: '예약 실패', message: err.message || '서버 오류가 발생했습니다.'});
            }
        });

        async function loadSlots(themeId, date) {
            try {
                const slots = await api.availableTimes(themeId, date);
                slotsArea.style.display = 'block';
                slotsDate.textContent = date;
                slotsCount.textContent = slots.length;
                if (slots.length === 0) {
                    slotsEmpty.style.display = 'block';
                    reserveForm.style.display = 'none';
                    return;
                }
                slotsEmpty.style.display = 'none';
                reserveForm.style.display = '';
                slotsList.innerHTML = slots.map((s, i) => `
                    <div class="slot">
                        <input type="radio" id="slot-${s.id}" name="timeId" value="${s.id}" ${i === 0 ? 'checked' : ''} required>
                        <label for="slot-${s.id}">${(s.startAt || '').slice(0, 5)}</label>
                    </div>
                `).join('');
                
                // Do NOT scrollIntoView here to prevent jumping
            } catch (e) {
                modal.alert({title: '시간 조회 실패', message: e.message});
            }
        }
    });

    function renderBanner(theme) {
        const banner = document.getElementById('case-banner');
        if (!theme) {
            banner.innerHTML = '';
            return;
        }
        const initial = (theme.name || '?').charAt(0);
        banner.innerHTML = `
            <div class="thumb" data-initial="${escapeAttr(initial)}">
                <img src="${escapeAttr(theme.thumbnail || '')}" alt="${escapeAttr(theme.name || '')}"
                     loading="lazy" referrerpolicy="no-referrer"
                     onerror="this.remove();">
            </div>
            <div>
                <span class="filenum">CASE FILE · No. ${theme.id} / 2026</span>
                <h1>${escapeHtml(theme.name || '')}</h1>
                <p>${escapeHtml(theme.description || '')}</p>
            </div>
        `;
    }

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, c => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[c]));
    }

    function escapeAttr(s) {
        return escapeHtml(s);
    }
})();
