(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', async function () {
        if (!api.isLoggedIn()) {
            location.href = '/login';
            return;
        }

        const ledgerSection = document.getElementById('ledger-section');
        const ledgerBody = document.getElementById('ledger-body');
        const ledgerCount = document.getElementById('ledger-count');
        const ledgerEmpty = document.getElementById('ledger-empty');
        const ledgerTable = document.getElementById('ledger-table');

        ledgerSection.style.display = '';

        let storesCache = [];

        try {
            storesCache = await api.listStores();
            const result = await api.listMyReservations();
            renderLedger(result.items);
        } catch (e) {
            modal.alert({title: '조회 실패', message: e.message});
        }

        function getStoreName(storeId) {
            const store = storesCache.find(s => String(s.id) === String(storeId));
            return store ? store.name : ('#' + storeId);
        }

        function renderLedger(items) {
            ledgerCount.textContent = items.length;
            const metaEl = document.getElementById('ledger-count-meta');
            if (metaEl) metaEl.textContent = items.length;
            if (items.length === 0) {
                ledgerTable.style.display = 'none';
                ledgerEmpty.style.display = 'block';
                return;
            }
            ledgerEmpty.style.display = 'none';
            ledgerTable.style.display = '';
            ledgerBody.innerHTML = items.map(r => `
                <tr data-id="${r.id}">
                    <td class="col-id">#${r.id}</td>
                    <td>${escapeHtml(getStoreName(r.storeId))}</td>
                    <td>${escapeHtml(r.theme && r.theme.name || '')}</td>
                    <td>${escapeHtml(r.date || '')}</td>
                    <td>${escapeHtml((r.time && r.time.startAt || '').slice(0, 5))}</td>
                    <td class="col-actions">
                        <button type="button" class="btn btn-sm" data-action="update" data-id="${r.id}"
                                data-date="${escapeHtml(r.date || '')}"
                                data-time-id="${r.time && r.time.id || ''}"
                                data-theme-id="${r.theme && r.theme.id || ''}">변경</button>
                        <button type="button" class="btn btn-danger btn-sm" data-action="delete" data-id="${r.id}">폐기</button>
                    </td>
                </tr>
            `).join('');

            ledgerBody.querySelectorAll('[data-action="delete"]').forEach(btn => {
                btn.addEventListener('click', () => onDelete(btn.dataset.id));
            });
            ledgerBody.querySelectorAll('[data-action="update"]').forEach(btn => {
                btn.addEventListener('click', () => onUpdate(btn.dataset.id, btn.dataset));
            });
        }

        async function onDelete(id) {
            const ok = await modal.confirm({
                title: '예약 폐기',
                message: '이 예약을 폐기하면 복구할 수 없습니다. 계속하시겠습니까?',
                okText: '폐기 확정',
                danger: true
            });
            if (!ok) return;
            try {
                await api.deleteReservation(id);
                const row = ledgerBody.querySelector(`tr[data-id="${id}"]`);
                if (row) row.remove();
                const remaining = ledgerBody.querySelectorAll('tr').length;
                ledgerCount.textContent = remaining;
                if (remaining === 0) {
                    ledgerTable.style.display = 'none';
                    ledgerEmpty.style.display = 'block';
                }
            } catch (e) {
                modal.alert({title: '삭제 실패', message: e.message});
            }
        }

        async function onUpdate(id, dataset) {
            const updatePanel = document.getElementById('update-panel');
            if (updatePanel) updatePanel.remove();

            const today = new Date().toISOString().slice(0, 10);
            
            const panel = document.createElement('div');
            panel.id = 'update-panel';
            panel.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:9999;';
            panel.innerHTML = `
                <div style="background:var(--paper,#fff);padding:2rem;border-radius:8px;min-width:320px;display:flex;flex-direction:column;gap:1rem;">
                    <h3 style="margin:0;">예약 변경</h3>
                    <label>날짜
                        <input id="update-date" type="date" min="${today}" value="${dataset.date}"
                               style="display:block;width:100%;margin-top:0.3rem;">
                    </label>
                    <label>시간
                        <select id="update-time" style="display:block;width:100%;margin-top:0.3rem;" disabled>
                            <option value="">조회 중...</option>
                        </select>
                    </label>
                    <div style="display:flex;gap:0.5rem;justify-content:flex-end;">
                        <button id="update-cancel" class="btn btn-ghost btn-sm">취소</button>
                        <button id="update-ok" class="btn btn-primary btn-sm" disabled>변경 확정</button>
                    </div>
                </div>
            `;
            document.body.appendChild(panel);

            const updateDateInput = document.getElementById('update-date');
            const updateTimeSelect = document.getElementById('update-time');
            const updateOkBtn = document.getElementById('update-ok');

            const refreshTimes = async () => {
                const date = updateDateInput.value;
                if (!date) return;
                updateTimeSelect.disabled = true;
                updateOkBtn.disabled = true;
                updateTimeSelect.innerHTML = '<option value="">조회 중...</option>';
                try {
                    const times = await api.availableTimes(dataset.themeId, date);
                    updateTimeSelect.innerHTML = times.map(t => `<option value="${t.id}">${(t.startAt || '').slice(0, 5)}</option>`).join('');
                    if (times.length === 0) {
                        updateTimeSelect.innerHTML = '<option value="">가능한 시간 없음</option>';
                    } else {
                        updateTimeSelect.disabled = false;
                        updateOkBtn.disabled = false;
                    }
                } catch (e) {
                    updateTimeSelect.innerHTML = '<option value="">오류 발생</option>';
                    console.error(e);
                }
            };

            updateDateInput.addEventListener('change', refreshTimes);
            await refreshTimes();

            document.getElementById('update-cancel').addEventListener('click', () => panel.remove());

            document.getElementById('update-ok').addEventListener('click', async () => {
                const newDate = document.getElementById('update-date').value;
                const newTimeId = Number(document.getElementById('update-time').value);
                if (!newDate || !newTimeId) {
                    modal.alert({message: '날짜와 시간을 모두 입력하세요.'});
                    return;
                }
                try {
                    await api.updateReservation(id, {
                        themeId: Number(dataset.themeId),
                        date: newDate,
                        timeId: newTimeId
                    });
                    panel.remove();
                    const result = await api.listMyReservations();
                    renderLedger(result.items);
                } catch (e) {
                    modal.alert({title: '변경 실패', message: e.message});
                }
            });
        }
    });

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, c => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[c]));
    }
})();
