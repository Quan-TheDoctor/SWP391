function formatDate(dateValue) {
    if (!dateValue) return 'Invalid Date';

    try {
        const date = new Date(dateValue);
        if (isNaN(date.getTime())) return 'Invalid Date';

        const month = date.toLocaleString('default', { month: 'short' });
        const day = date.getDate();
        return `${month} ${day}`;
    } catch (e) {
        console.error('Date formatting error:', e);
        return 'Invalid Date';
    }
}

function formatTime(timeValue) {
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

function createElement(tag, attributes = {}, content = '') {
    const element = document.createElement(tag);

    Object.entries(attributes).forEach(([key, value]) => {
        if (key === 'className') {
            element.className = value;
        } else {
            element.setAttribute(key, value);
        }
    });

    if (typeof content === 'string') {
        element.textContent = content;
    } else if (content instanceof HTMLElement) {
        element.appendChild(content);
    }

    return element;
}
