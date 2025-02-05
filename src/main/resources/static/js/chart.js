function employeeChart() {
    const employeeChart = document.getElementById('employeeChart');
    const departmentChart = document.getElementById('departmentChart');
    const positionChart = document.getElementById('positionChart');
    const isPresentChart = document.getElementById('isPresentChart');

    const employeeLabels = Object.keys(getCountByJoiningDate());
    const employeeDatas = Object.values(getCountByJoiningDate());

    const departmentLabels = Object.keys(getCountByDepartment());
    const departmentDatas = Object.values(getCountByDepartment());

    const positionLabels = Object.keys(getCountByPosition());
    const positionDatas = Object.values(getCountByPosition());

    const isPresentLabels = Object.keys(getCountByIsPresent());
    const isPresentDatas = Object.keys(getCountByIsPresent());

    initChart(employeeChart, 'line', employeeLabels, employeeDatas);
    initChart(departmentChart, 'doughnut', departmentLabels, departmentDatas);
    initChart(positionChart, 'doughnut', positionLabels, positionDatas)
    initChart(isPresentChart, 'doughnut', isPresentLabels, isPresentDatas)
}

function initChart(name, type, label, data) {
    new Chart(name, {
        type: type, data: {
            labels: label, datasets: [{
                label: 'employees',
                data: data,
                borderWidth: 0.5,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(150, 130, 152, 0.35)',
                hoverBackgroundColor: 'rgba(75, 100, 192, 0.85)'
            }]
        }, options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                tooltip: {
                    enabled: true
                },
                legend: {
                    display: false,
                }
            },
            scales: {
                y: {
                    ticks: {
                        display: false
                    }
                }
            }
        }
    });
}

function getCountByIsPresent() {
    const countByIsPresent = employees.reduce((acc, { isPresent }) => {
        acc[isPresent] = (acc[isPresent] || 0) + 1;
        return acc;
    }, {});

    return Object.keys(countByIsPresent)
        .sort()
        .reduce((acc, isPresent) => {
            acc[isPresent] = countByIsPresent[isPresent];
            return acc;
        }, {})
}

function getCountByDepartment() {
    const countByDepartment = employees.reduce((acc, { departmentName }) => {
        acc[departmentName] = (acc[departmentName] || 0) + 1;
        return acc;
    }, {});

    return Object.keys(countByDepartment)
        .sort()
        .reduce((acc, department) => {
            acc[department] = countByDepartment[department];
            return acc;
        }, {});
}

function getCountByPosition() {
    console.log(employees)
    const countByPosition = employees.reduce((acc, { positionName }) => {
        acc[positionName] = (acc[positionName] || 0) + 1;
        return acc;
    }, {});

    return Object.keys(countByPosition)
        .sort()
        .reduce((acc, position) => {
            acc[position] = countByPosition[position];
            return acc;
        }, {});
}

function getCountByJoiningDate() {
    const countByMonth = employees.reduce((acc, {joiningDate}) => {
        const [year, month] = joiningDate.split('-').slice(0, 2);
        const monthYear = `${year}-${month}`;
        acc[monthYear] = (acc[monthYear] || 0) + 1;
        return acc;
    }, {});

    const sortedCountByMonth = Object.keys(countByMonth)
        .sort()
        .reduce((acc, monthYear) => {
            acc[monthYear] = countByMonth[monthYear];
            return acc;
        }, {});

    return sortedCountByMonth;
}