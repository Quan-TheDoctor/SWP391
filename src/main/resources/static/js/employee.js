window.onload = function() {
    var employeeFirstName = document.getElementById("employeeFirstName");
    var employeeLastName = document.getElementById("employeeLastName");
    var employeeCode = document.getElementById("employeeCode");
    var employeeCompanyEmail = document.getElementById("employeeCompanyEmail");
    var employmentHistoryEmployeeCode = document.getElementById('employmentHistoryEmployeeCode');
    var employmentHistoryStartDate = document.getElementById("employmentHistoryStartDate");
    var employeeBirthDate = document.getElementById("employeeBirthDate");
    var employeeGender = document.getElementById("male");
    var employeeIdNumber = document.getElementById("employeeIdNumber");
    var employeePhoneNumber = document.getElementById("employeePhoneNumber");
    var employeePersonalEmail = document.getElementById("employeePersonalEmail");
    var employeePermanentAddress = document.getElementById("employeePermanentAddress");
    var employeeTemporaryAddress = document.getElementById("employeeTemporaryAddress");
    var employeeMaritalStatus = document.getElementById("single");
    var bankAccountName = document.getElementById("bankAccountName");
    var bankAccountCode = document.getElementById("bankAccountCode");
    var bankAccountEmployeeCode = document.getElementById("bankAccountEmployeeCode");
    var employeeTaxNumber= document.getElementById("employeeTaxNumber");
    var employeeCreatedAt = document.getElementById("employeeCreatedAt");
    var employeeUpdatedAt = document.getElementById("employeeUpdatedAt")

    var employmentHistoryDepartmentCode = document.getElementById("employmentHistoryDepartmentCode");
    var employmentHistoryPositionName = document.getElementById("employmentHistoryPositionName");
    var employmentHistoryIsPresent = document.getElementById("employmentHistoryIsPresent");
    var employmentHistoryCreatedAt = document.getElementById("employmentHistoryCreatedAt");
    var employmentHistoryUpdatedAt = document.getElementById("employmentHistoryUpdatedAt");
    var contractEmployeeCode = document.getElementById("contractEmployeeCode");
    var contractCode = document.getElementById("contractCode");
    var contractType = document.getElementById("contractType");
    var contractStartDate = document.getElementById("contractStartDate");
    var contractEndDate = document.getElementById("contractEndDate");
    var contractSignDate = document.getElementById("contractSignDate");
    var contractBaseSalary = document.getElementById("contractBaseSalary");
    var contractIsPresent = document.getElementById("contractIsPresent");
    var contractCreatedAt = document.getElementById("contractCreatedAt");
    const qualificationAddBtn = document.getElementById('qualificationAddBtn');
    const qualificationContainer = document.getElementById('qualificationContainer');
    var countContract = document.getElementById("countContract");

};

document.addEventListener('DOMContentLoaded', function () {
    let counter = document.querySelectorAll('.qualification-item').length;
    console.log("Document loaded");
    const empCode = document.getElementById('employeeCode');
    if (empCode) {
        console.log("Employee code field found on load");
    }

    if (employeeBirthDate) {
        employeeBirthDate.addEventListener("blur", function() {
            initValues();
        });
    }
    contractType.addEventListener("change", () => {
        console.log(contractType.value)
        contractEndDate.value = getCurrentDate(0, parseInt(contractType.value), 0);
    })

    qualificationAddBtn.addEventListener('click', function() {
        const template = document.getElementById('qualificationTemplate');
        const newQualification = template.content.cloneNode(true);

        const inputs = newQualification.querySelectorAll('input');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            input.name = name.replace('[]', `[${counter}]`);
        });

        const deleteBtn = newQualification.querySelector('.delete-qualification');
        deleteBtn.addEventListener('click', function() {
            this.closest('.qualification-item').remove();
            updateIndexes();
        });

        qualificationContainer.appendChild(newQualification);
        counter++;
    });
    document.querySelectorAll('.qualification-item').forEach(item => {
        const deleteBtn = item.querySelector('.delete-qualification');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', function() {
                this.closest('.qualification-item').remove();
                updateIndexes();
            });
        }
    });
    initValues()

});
function updateIndexes() {
    const items = document.querySelectorAll('.qualification-item');
    items.forEach((item, index) => {
        const inputs = item.querySelectorAll('input');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            const fieldName = name.split('[')[0];
            const field = name.split('.')[1];
            input.name = `${fieldName}[${index}].${field}`;
        });
    });
    counter = items.length;
}
function removeAccents(str) {
    if (typeof str !== 'string') {
        return str;
    }

    var accents = [
        {base: 'a', letters: /[\áàảãạăắằẳẵặâấầẩẫậ]/g},
        {base: 'e', letters: /[\éèẻẽẹêếềểễệ]/g},
        {base: 'i', letters: /[\íìỉĩị]/g},
        {base: 'o', letters: /[\óòỏõọôốồổỗộơớờở̃ợ]/g},
        {base: 'u', letters: /[\úùủũụưứừửữự]/g},
        {base: 'y', letters: /[\ýỳỷỹỵ]/g},
        {base: 'd', letters: /[\đ]/g}
    ];

    accents.forEach(function (acc) {
        str = str.replace(acc.letters, acc.base);
    });

    return str;
}

function initValues() {
    if (employeeLastName) {
        var name = (employeeFirstName.value + " " + employeeLastName.value).split(" ");
        console.log(name)
        var lastName = name[name.length - 1];
        var firstName = name[0];
        lastName = removeAccents(lastName);
        firstName = removeAccents(firstName);
        employeeCompanyEmail.value = lastName.toLowerCase() + firstName.toLowerCase() + "." +  `${employeeBirthDate.value.split("-")[0]}` + "@company.com";
        employeeCode.value = `${lastName.toUpperCase()} ${firstName.toUpperCase()}`
        employeeUpdatedAt.value = getCurrentDateTime();
        employeeCreatedAt.value = getCurrentDateTime();
        employmentHistoryStartDate.value = getCurrentDate();
        employmentHistoryIsPresent.value = true;
        employmentHistoryCreatedAt.value = getCurrentDateTime();
        contractCode.value = `HD${(parseInt(countContract.value) + 1).toString().padStart(3, '0')}-${getCurrentDate().split("/")[0]}`;
        contractStartDate.value = getCurrentDate();
        contractSignDate.value = getCurrentDate();
        contractIsPresent.value = true;
        contractCreatedAt.value = getCurrentDateTime();
    }
}

function getCurrentDate(addDays = 0, addMonths = 0, addYears = 0) {
    const date = new Date();

    const months = parseInt(addMonths);

    date.setDate(date.getDate() + addDays);
    date.setMonth(date.getMonth() + months);
    date.setFullYear(date.getFullYear() + addYears);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${year}-${month}-${day}`;
}

function getCurrentDateTime(addDays = 0, addMonths = 0, addYears = 0) {
    const date = new Date();

    // Apply any date adjustments
    date.setDate(date.getDate() + addDays);
    date.setMonth(date.getMonth() + addMonths);
    date.setFullYear(date.getFullYear() + addYears);

    // Format year, month, day, hours, minutes, seconds, and milliseconds
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    const milliseconds = String(date.getMilliseconds()).padStart(3, '0');

    // Construct LocalDateTime-compatible string (without time zone)
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}`;
}
