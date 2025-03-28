document.addEventListener('DOMContentLoaded', function() {
    const allAttendancesElement = document.getElementById('allAttendancesData');
    let allAttendances = [];

    if (allAttendancesElement) {
        try {
            allAttendances = JSON.parse(allAttendancesElement.textContent);
        } catch (e) {
            console.error('Error parsing attendance data:', e);
        }
    }

    initializeViewToggles(allAttendances);

    if (allAttendances.length > 0) {
        initializeKanbanView(allAttendances);
    }

    initializeAnalyticsTabs();
    initializeDateRangePicker();
    initializeFilterPanels();
    initializeCheckboxes();
});

function initializeViewToggles(allAttendances) {
    const viewToggles = document.querySelectorAll('.view-toggle');
    const viewContents = document.querySelectorAll('.view-content');
    const tableFilters = document.getElementById('tableFilters');
    const kanbanFilters = document.getElementById('kanbanFilters');

    viewToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            if (this.tagName === 'A' && this.getAttribute('href')) {
                return;
            }

            e.preventDefault();
            const view = this.dataset.view;

            viewToggles.forEach(btn => {
                btn.classList.remove('active', 'bg-primary-50', 'text-primary-600');
                btn.classList.add('bg-white', 'text-gray-700');
            });

            this.classList.add('active', 'bg-primary-50', 'text-primary-600');
            this.classList.remove('bg-white', 'text-gray-700');

            viewContents.forEach(content => {
                content.classList.add('hidden');
            });

            document.getElementById(view + 'View').classList.remove('hidden');

            if (view === 'table') {
                if (tableFilters) tableFilters.classList.remove('hidden');
                if (kanbanFilters) kanbanFilters.classList.add('hidden');
            } else if (view === 'kanban') {
                if (tableFilters) tableFilters.classList.add('hidden');
                if (kanbanFilters) kanbanFilters.classList.remove('hidden');

                if (allAttendances && allAttendances.length > 0) {
                    populateKanbanView(allAttendances);
                }
            }

            const url = new URL(window.location.href);
            url.searchParams.set('view', view);
            history.pushState({}, '', url.toString());
        });
    });
}

function initializeAnalyticsTabs() {
    const analyticsTabs = document.querySelectorAll('.analytics-tab');
    const analyticsPanels = document.querySelectorAll('.analytics-panel');

    analyticsTabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();

            analyticsTabs.forEach(t => {
                t.classList.remove('active', 'border-primary-500', 'text-primary-600');
                t.classList.add('border-transparent');
            });

            this.classList.add('active', 'border-primary-500', 'text-primary-600');
            this.classList.remove('border-transparent');

            analyticsPanels.forEach(panel => {
                panel.classList.add('hidden');
            });

            const targetPanel = document.getElementById(this.dataset.target);
            if (targetPanel) {
                targetPanel.classList.remove('hidden');
                window.dispatchEvent(new Event('resize'));
            }
        });
    });
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

    $('.date-range-picker').on('apply.daterangepicker', function(ev, picker) {
        $(this).val(picker.startDate.format('YYYY-MM-DD') + ' - ' + picker.endDate.format('YYYY-MM-DD'));

        const url = new URL(window.location.href);
        url.searchParams.set('startDate', picker.startDate.format('YYYY-MM-DD'));
        url.searchParams.set('endDate', picker.endDate.format('YYYY-MM-DD'));
        window.location.href = url.toString();
    });

    $('.date-range-picker').on('cancel.daterangepicker', function() {
        $(this).val('');

        const url = new URL(window.location.href);
        url.searchParams.delete('startDate');
        url.searchParams.delete('endDate');
        window.location.href = url.toString();
    });
}

function initializeFilterPanels() {
    const moreFiltersBtn = document.getElementById('moreFiltersBtn');
    const advancedFiltersPanel = document.getElementById('advancedFiltersPanel');

    if (moreFiltersBtn && advancedFiltersPanel) {
        moreFiltersBtn.addEventListener('click', function() {
            advancedFiltersPanel.classList.toggle('hidden');

            const batchActionsPanel = document.getElementById('batchActionsPanel');
            if (batchActionsPanel) {
                batchActionsPanel.classList.add('hidden');
            }
        });
    }

    const batchActionsBtn = document.getElementById('batchActionsBtn');
    const batchActionsPanel = document.getElementById('batchActionsPanel');

    if (batchActionsBtn && batchActionsPanel) {
        batchActionsBtn.addEventListener('click', function() {
            batchActionsPanel.classList.toggle('hidden');

            if (advancedFiltersPanel) {
                advancedFiltersPanel.classList.add('hidden');
            }
        });
    }
}

function initializeCheckboxes() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const rowCheckboxes = document.querySelectorAll('.checkboxes');

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }
}

function initializeKanbanView(allAttendances) {
    initializeKanbanFilters(allAttendances);
    checkInitialKanbanDisplay(allAttendances);
}

function initializeKanbanFilters(allAttendances) {
    const kanbanDateFilter = document.getElementById('kanbanDateFilter');
    const kanbanFilterBtn = document.getElementById('kanbanFilterBtn');
    const kanbanResetBtn = document.getElementById('kanbanResetBtn');
    const kanbanStatusFilter = document.getElementById('kanbanStatusFilter');
    const kanbanDepartmentFilter = document.getElementById('kanbanDepartmentFilter');

    if (kanbanFilterBtn) {
        kanbanFilterBtn.addEventListener('click', function() {
            if (allAttendances && allAttendances.length > 0) {
                const filters = {
                    date: kanbanDateFilter ? kanbanDateFilter.value : null,
                    status: kanbanStatusFilter ? kanbanStatusFilter.value : null,
                    department: kanbanDepartmentFilter ? kanbanDepartmentFilter.value : null
                };

                populateKanbanView(allAttendances, filters);
            }
        });
    }

    if (kanbanResetBtn) {
        kanbanResetBtn.addEventListener('click', function() {
            if (kanbanDateFilter) kanbanDateFilter.value = '';
            if (kanbanStatusFilter) kanbanStatusFilter.value = '';
            if (kanbanDepartmentFilter) kanbanDepartmentFilter.value = '';

            if (allAttendances && allAttendances.length > 0) {
                populateKanbanView(allAttendances);
            }
        });
    }
}

function checkInitialKanbanDisplay(allAttendances) {
    const urlParams = new URLSearchParams(window.location.search);
    const viewParam = urlParams.get('view');

    if (viewParam === 'kanban' && allAttendances && allAttendances.length > 0) {
        const viewToggles = document.querySelectorAll('.view-toggle');
        const tableFilters = document.getElementById('tableFilters');
        const kanbanFilters = document.getElementById('kanbanFilters');

        viewToggles.forEach(btn => {
            if (btn.dataset.view === 'kanban') {
                btn.classList.add('active', 'bg-primary-50', 'text-primary-600');
                btn.classList.remove('bg-white', 'text-gray-700');
            } else {
                btn.classList.remove('active', 'bg-primary-50', 'text-primary-600');
                btn.classList.add('bg-white', 'text-gray-700');
            }
        });

        document.getElementById('kanbanView').classList.remove('hidden');
        document.getElementById('tableView').classList.add('hidden');

        if (tableFilters) tableFilters.classList.add('hidden');
        if (kanbanFilters) kanbanFilters.classList.remove('hidden');

        const filters = {
            date: urlParams.get('startDate') || null,
            status: urlParams.get('status') || null,
            department: urlParams.get('department') || null,
            query: urlParams.get('query') || null
        };

        populateKanbanView(allAttendances, filters);
    }
}

function populateKanbanView(attendances, filters = {}) {
    if (!attendances || attendances.length === 0) {
        document.getElementById('kanbanEmpty').style.display = 'block';
        return;
    }

    const kanbanContainer = document.getElementById('employeeKanbanContainer');
    kanbanContainer.innerHTML = '';
    kanbanContainer.className = 'flex gap-4 min-w-max pb-4 pt-2';

    let filteredAttendances = [...attendances];
    if (filters.date) {
        filteredAttendances = filteredAttendances.filter(record => {
            const recordDate = record.attendanceDate.substring(0, 10);
            return recordDate === filters.date;
        });
    } else {
        const dateFilter = document.getElementById('kanbanDateFilter');
        if (dateFilter && dateFilter.value) {
            filteredAttendances = filteredAttendances.filter(record => {
                const recordDate = record.attendanceDate.substring(0, 10);
                return recordDate === dateFilter.value;
            });
        }
    }

    if (filters.status) {
        filteredAttendances = filteredAttendances.filter(record => {
            if (!record.attendanceStatus) return false;

            const status = record.attendanceStatus.toLowerCase();
            const filterStatus = filters.status.toLowerCase();

            if (filterStatus === 'đúng giờ' || filterStatus === 'on time') {
                return status === 'đúng giờ' || status === 'on time';
            } else if (filterStatus === 'đi muộn' || filterStatus === 'late') {
                return status === 'đi muộn' || status === 'late';
            } else if (filterStatus === 'vắng mặt' || filterStatus === 'absent') {
                return status === 'vắng mặt' || status === 'absent';
            } else if (filterStatus === 'chưa chấm công' || filterStatus === 'not checked') {
                return status === 'chưa chấm công' || status === 'not checked';
            }

            return true;
        });
    }

    if (filters.department) {
        filteredAttendances = filteredAttendances.filter(record => {
            return record.departmentId == filters.department;
        });
    }

    if (filters.query) {
        const query = filters.query.toLowerCase();
        filteredAttendances = filteredAttendances.filter(record => {
            const firstName = record.employeeFirstName ? record.employeeFirstName.toLowerCase() : '';
            const lastName = record.employeeLastName ? record.employeeLastName.toLowerCase() : '';
            const fullName = `${firstName} ${lastName}`.trim();
            const employeeCode = record.employeeCode ? record.employeeCode.toLowerCase() : '';

            return fullName.includes(query) || employeeCode.includes(query);
        });
    }

    const employeeMap = new Map();
    filteredAttendances.forEach(record => {
        const employeeId = record.employeeId;

        if (!employeeMap.has(employeeId)) {
            employeeMap.set(employeeId, {
                employee: record,
                records: []
            });
        }

        employeeMap.get(employeeId).records.push(record);
    });

    if (employeeMap.size === 0) {
        kanbanContainer.innerHTML = '<div class="text-center text-gray-500 py-4 w-full" id="kanbanEmpty">No records found for the selected filters</div>';
        return;
    }

    employeeMap.forEach((data, employeeId) => {
        const column = createEmployeeColumn(data);
        kanbanContainer.appendChild(column);
    });
}

function createEmployeeColumn(data) {
    const { employee, records } = data;

    const column = document.createElement('div');
    column.className = 'flex-shrink-0 w-64 bg-gray-50 rounded-lg p-3 flex flex-col';

    const header = document.createElement('div');
    header.className = 'flex items-center gap-2 mb-3 pb-2 border-b flex-shrink-0';

    const avatar = document.createElement('div');
    const firstLetter = employee.employeeFirstName ? employee.employeeFirstName.charAt(0).toUpperCase() : 'U';
    avatar.className = 'w-8 h-8 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-bold';
    avatar.innerHTML = `<span>${firstLetter}</span>`;

    const nameContainer = document.createElement('div');
    const name = document.createElement('h4');
    name.className = 'font-medium text-sm truncate';
    name.textContent = `${employee.employeeFirstName || ''} ${employee.employeeLastName || ''}`;

    const position = document.createElement('div');
    position.className = 'text-xs text-gray-500';
    position.textContent = employee.employeePosition || 'Employee';

    nameContainer.appendChild(name);
    nameContainer.appendChild(position);

    header.appendChild(avatar);
    header.appendChild(nameContainer);

    const timelineContainer = document.createElement('div');
    timelineContainer.className = 'overflow-y-auto flex-grow max-h-80 pr-1 custom-scrollbar';

    addCustomScrollbarStyle();

    const timeline = document.createElement('div');
    timeline.className = 'relative pl-5 pt-2';

    const timelineLine = document.createElement('div');
    timelineLine.className = 'absolute left-1 top-0 bottom-0 w-0.5 bg-gray-200';
    timeline.appendChild(timelineLine);

    records.sort((a, b) => {
        return new Date(b.attendanceDate) - new Date(a.attendanceDate);
    });

    records.forEach(record => {
        const recordItem = createRecordItem(record);
        timeline.appendChild(recordItem);
    });

    if (records.length > 5) {
        const seeAllContainer = document.createElement('div');
        seeAllContainer.className = 'text-center mt-2';

        const seeAllLink = document.createElement('a');
        seeAllLink.href = `/attendance/view/${employee.employeeId}`;
        seeAllLink.className = 'text-xs text-primary-600 hover:text-primary-800 transition-colors';
        seeAllLink.textContent = 'See all records';

        seeAllContainer.appendChild(seeAllLink);
        timeline.appendChild(seeAllContainer);
    }

    timelineContainer.appendChild(timeline);
    column.appendChild(header);
    column.appendChild(timelineContainer);

    return column;
}

function createRecordItem(record) {
    const recordItem = document.createElement('div');
    recordItem.className = 'mb-3 relative';

    let formattedDate = formatAttendanceDate(record.attendanceDate);
    const { dotColor, statusText } = getStatusInfo(record.attendanceStatus);

    const dot = document.createElement('div');
    dot.className = `absolute -left-4 mt-1.5 w-3 h-3 rounded-full bg-${dotColor}-500 border-2 border-white`;
    dot.title = statusText;

    const content = document.createElement('div');
    content.className = 'bg-white p-2 rounded-md shadow-sm';

    const dateText = document.createElement('div');
    dateText.className = 'font-medium text-xs';
    dateText.textContent = formattedDate;

    const timeInfo = createTimeInfo(record);

    const statusBadge = document.createElement('span');
    statusBadge.className = `inline-block px-2 py-0.5 text-xs rounded-full mt-1 bg-${dotColor}-100 text-${dotColor}-800`;
    statusBadge.textContent = statusText;

    content.appendChild(dateText);
    content.appendChild(timeInfo);
    content.appendChild(statusBadge);

    recordItem.appendChild(dot);
    recordItem.appendChild(content);

    return recordItem;
}

function createTimeInfo(record) {
    const timeInfo = document.createElement('div');
    timeInfo.className = 'flex flex-col mt-1';

    const timeLabels = document.createElement('div');
    timeLabels.className = 'flex justify-between text-xs text-gray-500';

    const inLabel = document.createElement('span');
    inLabel.textContent = 'In';

    const outLabel = document.createElement('span');
    outLabel.textContent = 'Out';

    timeLabels.appendChild(inLabel);
    timeLabels.appendChild(outLabel);

    const timeValues = document.createElement('div');
    timeValues.className = 'flex justify-between mt-0.5';

    const checkInSpan = document.createElement('span');
    checkInSpan.className = 'px-1.5 py-0.5 bg-gray-100 rounded text-xs text-gray-700';
    checkInSpan.textContent = formatTimeValue(record.attendanceCheckIn);

    const checkOutSpan = document.createElement('span');
    checkOutSpan.className = 'px-1.5 py-0.5 bg-gray-100 rounded text-xs text-gray-700';
    checkOutSpan.textContent = formatTimeValue(record.attendanceCheckOut);

    timeValues.appendChild(checkInSpan);
    timeValues.appendChild(checkOutSpan);

    timeInfo.appendChild(timeLabels);
    timeInfo.appendChild(timeValues);

    return timeInfo;
}

function formatTimeValue(timeValue) {
    if (!timeValue) return '--';

    try {
        if (typeof timeValue === 'string') {
            if (timeValue.includes('NaN') || timeValue === '--') {
                return '--';
            } else if (timeValue.includes(':')) {
                const timeParts = timeValue.split(':');
                if (timeParts.length >= 2) {
                    const hours = timeParts[0].padStart(2, '0');
                    const minutes = timeParts[1].padStart(2, '0');
                    return `${hours}:${minutes}`;
                }
            } else {
                const time = new Date(timeValue);
                if (!isNaN(time.getTime())) {
                    const hours = time.getHours().toString().padStart(2, '0');
                    const minutes = time.getMinutes().toString().padStart(2, '0');
                    return `${hours}:${minutes}`;
                }
            }
        } else if (timeValue instanceof Date) {
            const hours = timeValue.getHours().toString().padStart(2, '0');
            const minutes = timeValue.getMinutes().toString().padStart(2, '0');
            return `${hours}:${minutes}`;
        }
    } catch (e) {
        console.error('Time formatting error:', e);
    }

    return '--';
}

function formatAttendanceDate(dateValue) {
    let formattedDate = 'Invalid Date';
    try {
        const recordDate = new Date(dateValue);
        if (!isNaN(recordDate.getTime())) {
            const month = recordDate.toLocaleString('default', { month: 'short' });
            const day = recordDate.getDate();
            formattedDate = `${month} ${day}`;
        }
    } catch (e) {
        console.error('Date formatting error:', e);
    }
    return formattedDate;
}

function getStatusInfo(status) {
    let dotColor = 'gray';
    let statusText = 'Unknown';

    if (status) {
        const statusLower = status.toLowerCase();
        if (statusLower === 'đúng giờ' || statusLower === 'on time') {
            dotColor = 'green';
            statusText = 'On Time';
        } else if (statusLower === 'đi muộn' || statusLower === 'late') {
            dotColor = 'yellow';
            statusText = 'Late';
        } else if (statusLower === 'vắng mặt' || statusLower === 'absent') {
            dotColor = 'red';
            statusText = 'Absent';
        } else if (statusLower === 'chưa chấm công' || statusLower === 'not checked') {
            dotColor = 'gray';
            statusText = 'Not Checked';
        }
    }

    return { dotColor, statusText };
}

function addCustomScrollbarStyle() {
    if (!document.getElementById('custom-scrollbar-style')) {
        const style = document.createElement('style');
        style.id = 'custom-scrollbar-style';
        style.textContent = `
            .custom-scrollbar::-webkit-scrollbar {
                width: 4px;
            }
            .custom-scrollbar::-webkit-scrollbar-track {
                background: #f1f1f1;
                border-radius: 10px;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb {
                background: #d1d5db;
                border-radius: 10px;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb:hover {
                background: #9ca3af;
            }
        `;
        document.head.appendChild(style);
    }
}
