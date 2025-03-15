const DependentHandler = {
    dependentCounter: 0,
    initialize() {
        this.addButton = document.getElementById('add-dependent-btn');
        this.container = document.getElementById('dependent-container');
        this.template = document.getElementById('dependent-template');

        if (!this.addButton || !this.container || !this.template) {
            console.error('Required elements not found:', {
                button: !!this.addButton,
                container: !!this.container,
                template: !!this.template
            });
            return;
        }

        this.setupExistingDependents();
        this.addButton.addEventListener('click', () => this.addNewDependent());
    },

    setupExistingDependents() {
        const existingDependents = this.container.querySelectorAll(':scope > div');
        this.dependentCounter = existingDependents.length;
    },

    addNewDependent() {
        const newDependent = this.template.content.firstElementChild.cloneNode(true);
        const index = this.dependentCounter++;
        const inputs = newDependent.querySelectorAll('input');
        inputs.forEach(input => {
            const fieldName = input.getAttribute('name');
            if (fieldName) {
                input.name = fieldName.replace('[]', `[${index}]`);
                input.value = '';
            }
        });

        const deleteButton = this.createDeleteButton();
        newDependent.appendChild(deleteButton);

        this.container.appendChild(newDependent);
    },

    createDeleteButton() {
        const deleteBtn = document.createElement('delete-btn');
        deleteBtn.type = 'button';
        deleteBtn.className = 'delete-btn bg-purple-300 px-5 py-2 text-right rounded';
        deleteBtn.textContent = 'Delete';
        deleteBtn.addEventListener('click', function () {
            this.closest('.flex').remove();
        });
        return deleteBtn;
    }
};