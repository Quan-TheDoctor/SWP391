function countTotalEmployees() {
    var availableEmployeesCount = employees.filter(function (employee) {
        return employee.isPresent === true;
    }).length;
    var unavailableEmployeesCount = employees.filter((employee) => {
        return employee.isPresent === false;
    }).length;

    document.getElementById('availableEmployeesCount').innerText = availableEmployeesCount + " / " + unavailableEmployeesCount;
}