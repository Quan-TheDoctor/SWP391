function initializeViewToggle(allAttendances) {
    const viewToggles = document.querySelectorAll('.view-toggle');
    const viewContents = document.querySelectorAll('.view-content');

    viewToggles.forEach(toggle => {
        toggle.addEventListener('click', function (e) {
            if (this.tagName === 'A' && this.getAttribute('href')) {
                return;
            }

            e.preventDefault();
            const view = this.dataset.view;

            updateToggleButtonsState(viewToggles, this);

            updateViewContent(viewContents, view);

            if (view === 'kanban' && allAttendances && allAttendances.length > 0) {
                populateKanbanView(allAttendances);
            }
        });
    });
}


function updateToggleButtonsState(toggleButtons, activeButton) {
    toggleButtons.forEach(btn => {
        btn.classList.remove('active', 'bg-primary-50', 'text-primary-600');
        btn.classList.add('bg-white', 'text-gray-700');
    });

    activeButton.classList.add('active', 'bg-primary-50', 'text-primary-600');
    activeButton.classList.remove('bg-white', 'text-gray-700');
}

function updateViewContent(viewContents, activeView) {
    viewContents.forEach(content => {
        content.classList.add('hidden');
    });

    document.getElementById(activeView + 'View').classList.remove('hidden');
}
