function employeeChart() {
    const employeeChart = document.getElementById('employeeChart');
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

    const labels = Object.keys(sortedCountByMonth);
    const data = Object.values(sortedCountByMonth);


    new Chart(employeeChart, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '# of Votes',
                data: data,
                borderWidth: 1,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}