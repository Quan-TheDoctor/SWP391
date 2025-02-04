
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