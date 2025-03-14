function initializeFilters() {
    initializeDateRangePicker();
    initializePeriodFilters();
    initializeAdvancedFilters();
    initializeBatchActions();
}

function initializeDateRangePicker() {
    $('.date-range-picker').daterangepicker({
        opens: 'left',
        autoUpdateInput: false,
        locale: {
            format: 'YYYY-MM-DD',
            cancelLabel: 'Clear'
        }
    });

    $('.date-range-picker').on('apply.daterangepicker', function (ev, picker) {
        $(this).val(picker.startDate.format('YYYY-MM-DD') + ' - ' + picker.endDate.format('YYYY-MM-DD'));

        const url = new URL(window.location.href);
        url.searchParams.set('startDate', picker.startDate.format('YYYY-MM-DD'));
        url.searchParams.set('endDate', picker.endDate.format('YYYY-MM-DD'));
        window.location.href = url.toString();
    });

    $('.date-range-picker').on('cancel.daterangepicker', function () {
        $(this).val('');

        const url = new URL(window.location.href);
        url.searchParams.delete('startDate');
        url.searchParams.delete('endDate');
        window.location.href = url.toString();
    });
}

function initializePeriodFilters() {
    const yearSelect = document.getElementById('yearSelect');
    const monthSelect = document.getElementById('monthSelect');
    const weekSelect = document.getElementById('weekSelect');

    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('year')) {
        yearSelect.value = urlParams.get('year');
    }
    if (urlParams.has('month')) {
        monthSelect.value = urlParams.get('month');
    }
    if (urlParams.has('week')) {
        weekSelect.value = urlParams.get('week');
    }

    yearSelect.addEventListener('change', applyDateFilters);
    monthSelect.addEventListener('change', applyDateFilters);
    weekSelect.addEventListener('change', applyDateFilters);
}

function applyDateFilters() {
    const yearSelect = document.getElementById('yearSelect');
    const monthSelect = document.getElementById('monthSelect');
    const weekSelect = document.getElementById('weekSelect');

    const year = yearSelect.value;
    const month = monthSelect.value;
    const week = weekSelect.value;

    const url = new URL(window.location.href);

    if (year) url.searchParams.set('year', year);
    else url.searchParams.delete('year');

    if (month) url.searchParams.set('month', month);
    else url.searchParams.delete('month');

    if (week) url.searchParams.set('week', week);
    else url.searchParams.delete('week');

    window.location.href = url.toString();
}

function initializeAdvancedFilters() {
    const moreFiltersBtn = document.getElementById('moreFiltersBtn');
    const advancedFiltersPanel = document.getElementById('advancedFiltersPanel');

    moreFiltersBtn.addEventListener('click', function () {
        advancedFiltersPanel.classList.toggle('hidden');

        const batchActionsPanel = document.getElementById('batchActionsPanel');
        if (batchActionsPanel) {
            batchActionsPanel.classList.add('hidden');
        }
    });
}

function initializeBatchActions() {
    const batchActionsBtn = document.getElementById('batchActionsBtn');
    const batchActionsPanel = document.getElementById('batchActionsPanel');
    const selectAllCheckbox = document.getElementById('selectAll');
    const rowCheckboxes = document.querySelectorAll('.checkboxes');

    if (batchActionsBtn) {
        batchActionsBtn.addEventListener('click', function () {
            batchActionsPanel.classList.toggle('hidden');

            const advancedFiltersPanel = document.getElementById('advancedFiltersPanel');
            if (advancedFiltersPanel) {
                advancedFiltersPanel.classList.add('hidden');
            }
        });
    }

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }
}
