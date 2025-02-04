var datas = [];
var employees = /*[[${allEmployees}]]*/ '[]';
var departments = [
    ...new Map( //No duplicated
        employees.map(e => [`${e.departmentId}-${e.departmentName}`, {id: e.departmentId, name: e.departmentName}])
    ).values()
];
var positions = [
    ...new Map(
        employees.map(e => [`${e.positionId}-${e.positionName}`, {id: e.positionId, name: e.positionName}])
    ).values()
];
for (var employee of employees) {
    datas.push({
        category: 'Employee',
        title: employee.employeeName,
        id: employee.employeeId
    });
}
for (var department of departments) {
    datas.push({
        category: 'Department',
        title: department.name,
        id: department.id
    })
}
for (var position of positions) {
    datas.push({
        category: 'Position',
        title: position.name,
        id: position.id
    })
}

function countTotalEmployees() {
    var availableEmployeesCount = employees.filter(function (employee) {
        return employee.isPresent === true;
    }).length;
    var unavailableEmployeesCount = employees.filter((employee) => {
        return employee.isPresent === false;
    }).length;

    document.getElementById('availableEmployeesCount').innerText = availableEmployeesCount + " / " + unavailableEmployeesCount;
}

function searchEmployees(searchField, searchId) {
    $.ajax({
        url: `/employee/search?field=${encodeURIComponent(searchField)}&id=${encodeURIComponent(searchId)}`,
        method: 'GET',
        success: function (response) {
            window.location.href = `/employee/search?field=${encodeURIComponent(searchField)}&id=${encodeURIComponent(searchId)}`;
        },
        error: function (xhr, status, error) {
            console.error("AJAX Error: " + status + " - " + error);
        }
    });
}


window.onload = function () {
    countTotalEmployees();

    $('.ui.search')
        .search({
            type: 'category',
            source: datas,
            onSelect: function (result, response) {
                var searchField = result.category;
                var searchId = result.id;
                searchEmployees(searchField, searchId);
            }
        });
};