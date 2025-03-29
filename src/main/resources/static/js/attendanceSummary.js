const AttendanceManager = {
    data: {
        employeeId: '',
        employeeName: '',
        attendanceRecords: [],
        lastEditedIndex: null,
        monthlySummary: {
            workDays: 0,
            lateDays: 0,
            absentDays: 0,
            overtimeHours: 0
        }
    },

    ui: {
        showModal: (id) => document.getElementById(id).classList.remove('hidden'),
        hideModal: (id) => document.getElementById(id).classList.add('hidden'),

        showToast: (message, type = 'success') => {
            const toast = document.createElement('div');
            const bgColor = type === 'success' ? 'bg-green-100 border-green-500 text-green-700' : 'bg-red-100 border-red-500 text-red-700';
            toast.className = `fixed bottom-4 right-4 ${bgColor} border-l-4 p-4 rounded shadow-md z-[9999] transition-opacity duration-500`;
            toast.innerHTML = `<div class="flex items-center"><svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg><p>${message}</p></div>`;

            document.body.appendChild(toast);
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => document.body.removeChild(toast), 500);
            }, 3000);
        },

        updateCards: (data) => {
            const container = document.getElementById('attendanceCards');
            container.innerHTML = '';

            if (data.length === 0) {
                container.innerHTML = '<div class="p-8 text-center text-gray-500">Không có dữ liệu chấm công phù hợp với bộ lọc.</div>';
                return;
            }

            data.forEach((record, index) => {
                const card = AttendanceManager.ui.createCard(record, index);
                container.appendChild(card);
            });

            if (AttendanceManager.data.lastEditedIndex !== null) {
                setTimeout(() => {
                    const editedCard = container.querySelector(`[data-index="${AttendanceManager.data.lastEditedIndex}"]`);
                    if (editedCard) editedCard.scrollIntoView({behavior: 'smooth', block: 'center'});
                }, 100);
            }
        },

        createCard: (record, index) => {
            const isRecentlyEdited = (index === AttendanceManager.data.lastEditedIndex);
            const isAbsent = record.status.includes('Absent') || (record.start === '00:00' && record.end === '00:00');

            const card = document.createElement('div');
            card.className = `relative ${isRecentlyEdited ? 'bg-gradient-to-r from-yellow-50 to-amber-50 ring-2 ring-yellow-300 highlight-flash' : 'bg-gradient-to-r from-blue-50 to-purple-50'} rounded-2xl p-4 hover:shadow-lg transition-all`;
            card.dataset.index = index;

            card.innerHTML = `
${isRecentlyEdited ? '<div class="absolute -top-2 -right-2 bg-yellow-400 text-yellow-900 text-xs px-2 py-1 rounded-full shadow-md animate-pulse">Vừa cập nhật</div>' : ''}
<div class="flex justify-between items-start mb-2">
    <span class="text-sm font-medium text-gray-700">${record.date}</span>
    <div class="flex items-center gap-2">
        <span class="px-2 py-1 rounded-full text-xs ${record.statusClass}">${record.status}</span>
        <button onclick="AttendanceManager.editRecord(${index})" class="p-1 bg-blue-100 rounded-full hover:bg-blue-200 text-blue-700">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
            </svg>
        </button>
    </div>
</div>
${isAbsent ?
                `<div class="h-16 flex items-center justify-center">
                        <div class="text-center text-gray-500">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 mx-auto text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM7 9a1 1 0 000 2h6a1 1 0 100-2H7z" clip-rule="evenodd" />
                            </svg>
                            <p class="mt-1 text-sm">Không có dữ liệu chấm công</p>
                        </div>
                    </div>` :
                `<div class="h-16 relative">
                        <div class="absolute top-6 left-0 right-0 h-3 bg-gray-100 rounded-full shadow-inner">
                            <div class="absolute -top-2 w-full flex justify-between">${AttendanceManager.utils.createTimeMarkers()}</div>
                            <div class="absolute h-3 bg-gradient-to-r from-green-400 to-blue-400 rounded-full shadow-lg"
                                 style="left: ${AttendanceManager.utils.calcPosition(record.start)}%; width: ${AttendanceManager.utils.calcWidth(record.start, record.end)}%">
                                <div class="absolute -top-8 left-0 bg-white/95 backdrop-blur-sm px-2 py-1 rounded-md shadow-md border border-gray-100">
                                    <div class="text-[9px] text-gray-500">Start</div>
                                    <span class="text-xs font-semibold text-green-600">${record.start}</span>
                                </div>
                                <div class="absolute -top-8 right-0 bg-white/95 backdrop-blur-sm px-2 py-1 rounded-md shadow-md border border-gray-100">
                                    <div class="text-[9px] text-gray-500">End</div>
                                    <span class="text-xs font-semibold text-blue-600">${record.end}</span>
                                </div>
                            </div>
                        </div>
                        <div class="absolute bottom-0 inset-x-0 flex justify-between text-xs">
                            <div class="flex items-center gap-1 text-gray-500">
                                <div class="w-3 h-3 bg-green-400 rounded-sm"></div>
                                Giờ làm
                            </div>
                            <div class="flex items-center gap-1 text-gray-500">
                                <div class="w-3 h-3 bg-blue-400 rounded-sm"></div>
                                OT
                            </div>
                        </div>
                    </div>`
            }
`;

            return card;
        },

        updateMainSummary: function () {
            const employeeCards = document.querySelectorAll('[data-employee-id]');

            let totalWorkDays = 0;
            let totalLateDays = 0;
            let totalAbsentDays = 0;
            let totalOTHours = 0;
            let employeeCount = 0;

            employeeCards.forEach(card => {
                const workDaysElement = card.querySelector('[name$="workedDays"]');
                const lateDaysElement = card.querySelector('[name$="lateDays"]');
                const absentDaysElement = card.querySelector('[name$="absentDays"]');
                const otHoursElement = card.querySelector('[name$="overtimeHours"]');

                const workDays = workDaysElement ? parseInt(workDaysElement.value) || 0 : 0;
                const lateDays = lateDaysElement ? parseInt(lateDaysElement.value) || 0 : 0;
                const absentDays = absentDaysElement ? parseInt(absentDaysElement.value) || 0 : 0;
                const otHours = otHoursElement ? parseFloat(otHoursElement.value) || 0 : 0;

                totalWorkDays += workDays;
                totalLateDays += lateDays;
                totalAbsentDays += absentDays;
                totalOTHours += otHours;
                employeeCount++;
            });

            if (employeeCount > 0) {
                const avgWorkDays = Math.round(totalWorkDays / employeeCount);
                const avgLateDays = Math.round(totalLateDays / employeeCount);
                const avgAbsentDays = Math.round(totalAbsentDays / employeeCount);
                const totalOTHoursFixed = totalOTHours.toFixed(1);

                const workDaysElement = document.querySelector('.text-green-600');
                const lateDaysElement = document.querySelector('.text-yellow-600');
                const absentDaysElement = document.querySelector('.text-red-600');
                const otHoursElement = document.querySelector('.text-blue-600');

                if (workDaysElement) workDaysElement.textContent = avgWorkDays;
                if (lateDaysElement) lateDaysElement.textContent = avgLateDays;
                if (absentDaysElement) absentDaysElement.textContent = avgAbsentDays;
                if (otHoursElement) otHoursElement.textContent = totalOTHoursFixed;
            }
        },

        updateMainSummaryStats: function () {
            const allRecords = AttendanceManager.data.attendanceRecords;

            const now = new Date();
            const year = now.getFullYear();
            const month = now.getMonth();
            const daysInMonth = new Date(year, month + 1, 0).getDate();

            let workingDaysInMonth = 0;
            for (let day = 1; day <= daysInMonth; day++) {
                const date = new Date(year, month, day);
                const dayOfWeek = date.getDay();
                if (dayOfWeek !== 0 && dayOfWeek !== 6) {
                    workingDaysInMonth++;
                }
            }

            const mainSummaryCards = document.querySelectorAll('.animate__fadeIn .bg-white.p-4.rounded-lg.shadow-lg.flex.flex-col.items-center');
            if (mainSummaryCards.length >= 4) {
                const workingDays = allRecords.filter(r => r.status.includes('On time')).length;
                mainSummaryCards[0].querySelector('h2').textContent = workingDays;

                const lateDays = allRecords.filter(r => r.status.includes('Late')).length;
                mainSummaryCards[1].querySelector('h2').textContent = lateDays;

                const absentDays = allRecords.filter(r => r.status.includes('Absent')).length;
                mainSummaryCards[2].querySelector('h2').textContent = absentDays;

                const totalOTHours = allRecords.reduce((sum, record) => sum + (record.overtimeHours || 0), 0);
                mainSummaryCards[3].querySelector('h2').textContent = totalOTHours.toFixed(1);
            }
        },

        updateStats: function (data) {
            const workDays = data.filter(r => r.status === 'On time').length;
            const lateDays = data.filter(r => r.status === 'Late').length;
            const absentDays = data.filter(r => r.status === 'Absent').length;

            const totalOTHours = data.reduce((total, record) => {
                if (typeof record.overtimeHours === 'number') {
                    return total + record.overtimeHours;
                }

                if (record.status === 'OT') {
                    const match = record.status.match(/OT\s+(\d+(\.\d+)?)/);
                    if (match) {
                        return total + parseFloat(match[1]);
                    }
                }

                if (record.end && record.end !== '00:00') {
                    const [endHour, endMin] = record.end.split(':').map(Number);
                    if (endHour >= 18) {
                        const overtimeMinutes = (endHour - 18) * 60 + endMin;
                        return total + (overtimeMinutes / 60);
                    }
                }

                return total;
            }, 0);

            document.getElementById('totalWorkDays').textContent = workDays;
            document.getElementById('totalLateDays').textContent = lateDays;
            document.getElementById('totalAbsentDays').textContent = absentDays;
            document.getElementById('totalOTHours').textContent = totalOTHours.toFixed(1);

            AttendanceManager.data.monthlySummary = {
                workDays,
                lateDays,
                absentDays,
                overtimeHours: totalOTHours
            };

            console.log("Monthly summary updated:", AttendanceManager.data.monthlySummary);
        },

        updateMonthlySummary: function () {
            const employeeId = AttendanceManager.data.employeeId;
            const employeeInputs = document.querySelectorAll(`input[name*="payrollCalculations"][value="${employeeId}"]`);

            if (employeeInputs.length > 0) {
                const employeeInput = employeeInputs[0];
                const formIndex = employeeInput.name.match(/\[(\d+)\]/)[1];
                const summary = AttendanceManager.data.monthlySummary;

                document.querySelector(`input[name="payrollCalculations[${formIndex}].workedDays"]`).value = summary.workDays;
                document.querySelector(`input[name="payrollCalculations[${formIndex}].lateDays"]`).value = summary.lateDays;
                document.querySelector(`input[name="payrollCalculations[${formIndex}].absentDays"]`).value = summary.absentDays;
                document.querySelector(`input[name="payrollCalculations[${formIndex}].overtimeHours"]`).value = summary.overtimeHours.toFixed(1);

                console.log("Form fields updated with monthly summary data");
            } else {
                console.log("Could not find form fields for employee ID:", employeeId);
            }
        }
    },

    utils: {
        calcPosition: (time) => {
            if (time === '00:00') return 0;
            const [h, m] = time.split(':').map(Number);
            return ((h + m / 60) / 24 * 100).toFixed(2);
        },

        calcWidth: (start, end) => {
            if (start === '00:00' && end === '00:00') return 0;
            return AttendanceManager.utils.calcPosition(end) - AttendanceManager.utils.calcPosition(start);
        },

        createTimeMarkers: () => {
            let markers = '';
            for (let hour = 0; hour < 24; hour++) {
                const isWorkHour = hour >= 8 && hour < 18;
                markers += `
<div class="relative" style="width: 4.1667%">
    <div class="h-4 w-px mx-auto ${isWorkHour ? 'bg-blue-300' : 'bg-gray-300'}"></div>
<span class="absolute top-1.5 left-1/2 -translate-x-1/2 text-[8px] font-mono
                        ${isWorkHour ? 'text-blue-500' : 'text-gray-400'}">
                        ${hour === 0 ? '00h' : hour + 'h'}
                    </span>
</div>`;
            }
            return markers;
        },

        calculateOvertimeHours: function (start, end) {
            if (start === '00:00' || end === '00:00') return 0;

            const [startHour, startMin] = start.split(':').map(Number);
            const [endHour, endMin] = end.split(':').map(Number);

            const startMinutes = startHour * 60 + startMin;
            const endMinutes = endHour * 60 + endMin;
            const standardEndMinutes = 18 * 60;

            if (endMinutes <= standardEndMinutes) return 0;

            const overtimeMinutes = endMinutes - standardEndMinutes;
            return Math.round(overtimeMinutes / 6) / 10;
        },

        getRecordStatusType: function (record) {
            if (record.status === 'On time') return 'ontime';
            if (record.status === 'Late') return 'late';
            if (record.status === 'OT') return 'overtime';
            if (record.status === 'Absent') return 'absent';
            return 'unknown';
        },

        debugOTHours: function () {
            console.log("Current attendance records:",
                AttendanceManager.data.attendanceRecords.map(r => ({
                    date: r.date,
                    status: r.status,
                    overtimeHours: r.overtimeHours,
                    start: r.start,
                    end: r.end,
                    calculatedOT: this.calculateOvertimeHours(r.start, r.end)
                }))
            );

            const totalOT = AttendanceManager.data.attendanceRecords.reduce((sum, r) =>
                sum + (r.overtimeHours || 0), 0);
            console.log("Total OT hours:", totalOT.toFixed(1));
        }
    },

    init: function () {
        document.getElementById('statusFilter').addEventListener('change', this.filterRecords.bind(this));
        document.getElementById('editStartTime').addEventListener('change', this.updateOvertimeHours.bind(this));
        document.getElementById('editEndTime').addEventListener('change', this.updateOvertimeHours.bind(this));

        if (document.querySelectorAll('[data-employee-id]').length > 0) {
            this.ui.updateMainSummary();
        }
    },

    fetchData: async function (employeeId, date) {
        const data = await fetch(`/api/attendance/getAllAttendanceByDateAndEmployeeId?employeeId=${employeeId}&date=${date}`)
            .then(res => res.json())
            .then((data) => {
                return data;
            });

        return data;
    },

    updateOvertimeHours: function() {
        const start = document.getElementById('editStartTime').value;
        const end = document.getElementById('editEndTime').value;
        const calculatedOT = this.utils.calculateOvertimeHours(start, end);

        if (calculatedOT > 0) {
            const currentStatus = document.getElementById('editStatus').value;
            if (currentStatus !== 'Late' && currentStatus !== 'Absent') {
                document.getElementById('editStatus').value = 'OT';
                document.getElementById('otHoursContainer').classList.remove('hidden');
                document.getElementById('editOTHours').value = calculatedOT.toFixed(1);
            }
        }

        if (document.getElementById('editStatus').value === 'OT') {
            document.getElementById('editOTHours').value = calculatedOT.toFixed(1);
        }
    },

    showDetails: async function (employeeId, employeeName) {
        this.data.employeeId = employeeId;
        this.data.employeeName = employeeName;

        document.getElementById('modalEmployeeName').textContent = `Chi tiết chấm công - ${employeeName}`;

        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const datas = await this.fetchData(employeeId, `${year}-${month}-${day}`);

        this.data.attendanceRecords = [];

        datas.forEach((data) => {
            let overtimeHours = 0;
            if (data.attendanceOvertimeHours !== null && data.attendanceOvertimeHours !== undefined) {
                overtimeHours = parseFloat(data.attendanceOvertimeHours);
                if (isNaN(overtimeHours)) overtimeHours = 0;
            }

            this.data.attendanceRecords.push({
                id: data.attendanceId,
                date: data.attendanceDate,
                start: data.attendanceCheckIn.substring(0, 5),
                end: data.attendanceCheckOut.substring(0, 5),
                status: data.attendanceStatus,
                overtimeHours: overtimeHours,
                statusClass: data.attendanceStatus == 'On time' ? 'bg-green-100 text-green-800' :
                    data.attendanceStatus.includes('Late') ? 'bg-yellow-100 text-yellow-800' :
                        data.attendanceStatus.includes('OT') ? 'bg-blue-100 text-blue-800' :
                            'bg-red-100 text-red-800'
            });
        });

        this.filterRecords();
        this.ui.showModal('attendanceModal');
    },

    filterRecords: function () {
        const status = document.getElementById('statusFilter').value;

        let filteredData = this.data.attendanceRecords;
        if (status !== 'all') {
            filteredData = this.data.attendanceRecords.filter(record => {
                if (status === 'On time') return record.status === 'On time';
                if (status === 'late') return record.status === 'Late';
                if (status === 'overtime') return record.status === 'OT';
                if (status === 'absent') return record.status === 'Absent';
                return true;
            });
        }

        this.ui.updateCards(filteredData);
        this.ui.updateStats(this.data.attendanceRecords);
    },

    editRecord: function (index) {
        const record = this.data.attendanceRecords[index];

        document.getElementById('editDate').textContent = `Ngày: ${record.date}`;
        document.getElementById('editRecordIndex').value = index;
        document.getElementById('editStartTime').value = record.start;
        document.getElementById('editEndTime').value = record.end;

        const calculatedOT = this.utils.calculateOvertimeHours(record.start, record.end);
        let status = 'On time';
        if (record.status.includes('Late')) {
            status = 'Late';
            const minutes = record.status.match(/\d+/) || ['0'];
            document.getElementById('editLateMinutes').value = minutes[0];
            document.getElementById('lateMinutesContainer').classList.remove('hidden');
            document.getElementById('otHoursContainer').classList.add('hidden');
        } else if (record.status.includes('OT')) {
            status = 'OT';
            const hours = record.overtimeHours ||
                (record.status.match(/\d+(\.\d+)?/) || [calculatedOT.toFixed(1)])[0];
            document.getElementById('editOTHours').value = parseFloat(hours).toFixed(1);
            document.getElementById('otHoursContainer').classList.remove('hidden');
            document.getElementById('lateMinutesContainer').classList.add('hidden');
        } else if (record.status.includes('Absent')) {
            status = 'Absent';
            document.getElementById('lateMinutesContainer').classList.add('hidden');
            document.getElementById('otHoursContainer').classList.add('hidden');
        } else {
            document.getElementById('lateMinutesContainer').classList.add('hidden');
            document.getElementById('otHoursContainer').classList.add('hidden');
        }

        document.getElementById('editStatus').value = status;

        document.getElementById('editStatus').addEventListener('change', this.handleStatusChange);
        this.ui.showModal('editAttendanceModal');
    },

    handleStatusChange: function () {
        const status = document.getElementById('editStatus').value;
        const start = document.getElementById('editStartTime').value;
        const end = document.getElementById('editEndTime').value;
        const calculatedOT = AttendanceManager.utils.calculateOvertimeHours(start, end);

        document.getElementById('lateMinutesContainer').classList.add('hidden');
        document.getElementById('otHoursContainer').classList.add('hidden');

        if (status === 'Late') {
            document.getElementById('lateMinutesContainer').classList.remove('hidden');
        } else if (status === 'OT') {
            document.getElementById('otHoursContainer').classList.remove('hidden');
            document.getElementById('editOTHours').value = calculatedOT.toFixed(1);
        }

        if (status === 'Absent') {
            document.getElementById('editStartTime').value = '00:00';
            document.getElementById('editEndTime').value = '00:00';
        }
    },

    saveChanges: function () {
        const index = parseInt(document.getElementById('editRecordIndex').value);
        const record = this.data.attendanceRecords[index];

        const previousStatus = this.utils.getRecordStatusType(record);
        const previousOvertimeHours = record.overtimeHours || 0;

        const newStartTime = document.getElementById('editStartTime').value;
        const newEndTime = document.getElementById('editEndTime').value;

        const calculatedOT = this.utils.calculateOvertimeHours(newStartTime, newEndTime);

        let statusType = document.getElementById('editStatus').value;
        let statusText = statusType;
        let statusClass = 'bg-green-100 text-green-800';
        let overtimeHours = 0;

        if (calculatedOT > 0 && statusType !== 'Absent' && statusType !== 'Late') {
            statusType = 'OT';
        }

        if (statusType === 'Absent') {
            record.start = '00:00';
            record.end = '00:00';
            statusClass = 'bg-red-100 text-red-800';
        } else {
            record.start = newStartTime;
            record.end = newEndTime;

            if (statusType === 'Late') {
                const minutes = document.getElementById('editLateMinutes').value;
                statusText = `Late ${minutes}p`;
                statusClass = 'bg-yellow-100 text-yellow-800';
            } else if (statusType === 'OT') {
                overtimeHours = document.getElementById('otHoursContainer').classList.contains('hidden')
                    ? calculatedOT
                    : parseFloat(document.getElementById('editOTHours').value);

                if (isNaN(overtimeHours)) overtimeHours = calculatedOT;
                statusText = `OT ${overtimeHours.toFixed(1)}h`;
                statusClass = 'bg-blue-100 text-blue-800';
            }
        }

        record.status = statusText;
        record.statusClass = statusClass;
        record.overtimeHours = overtimeHours;

        this.data.lastEditedIndex = index;

        const putData = {
            attendanceId: record.id,
            attendanceCheckIn: record.start,
            attendanceCheckOut: record.end,
            attendanceStatus: record.status,
            attendanceDate: record.date,
            attendanceOvertimeHours: overtimeHours
        };

        console.log("Sending to server:", putData);

        this.closeEditModal();
        this.filterRecords();

        fetch('/api/attendance/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(putData)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    this.ui.showToast('Đã lưu thành công!');

                    this.ui.updateStats(this.data.attendanceRecords);
                    this.ui.updateMonthlySummary();
                    this.ui.updateMainSummary();

                    const currentStatus = this.utils.getRecordStatusType(record);
                    console.log(`Status changed: ${previousStatus} -> ${currentStatus}, OT Hours: ${previousOvertimeHours} -> ${overtimeHours}`);

                    this.filterRecords();
                } else {
                    this.ui.showToast('Lỗi khi lưu dữ liệu!', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                this.ui.showToast('Lỗi kết nối!', 'error');
            });

        setTimeout(() => {
            this.data.lastEditedIndex = null;
            this.filterRecords();
        }, 10000);
    },

    closeEditModal: function () {
        document.getElementById('editStatus').removeEventListener('change', this.handleStatusChange);
        this.ui.hideModal('editAttendanceModal');
    }
};

document.addEventListener("DOMContentLoaded", function () {
    AttendanceManager.init();
});

function showAttendanceDetails(employeeId, employeeName) {
    AttendanceManager.showDetails(employeeId, employeeName);
}

function editAttendance(index) {
    AttendanceManager.editRecord(index);
}

function saveAttendanceChanges() {
    AttendanceManager.saveChanges();
}

function closeAttendanceDetails() {
    AttendanceManager.ui.hideModal('attendanceModal');
}

function closeEditModal() {
    AttendanceManager.closeEditModal();
}

function closePopup() {
    AttendanceManager.ui.hideModal("errorPopup");
    AttendanceManager.ui.hideModal("overlay");
}
