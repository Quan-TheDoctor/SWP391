const ToastManager = {
    toastNotify: (message, type = 'success') => {
        const toast = document.createElement('div');
        const bgColor = type === 'success' ? 'bg-green-100 border-green-500 text-green-700' : type === 'error' ? 'bg-red-100 border-red-500 text-red-700' : 'bg-grey-100 border-red-500 text-black';
        toast.className = `fixed top-4 right-4 ${bgColor} border-l-4 p-4 rounded shadow-md z-[9999] transition-opacity duration-500`;
        toast.innerHTML = `<div class="flex items-center"><svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg><p>${message}</p></div>`;

        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => document.body.removeChild(toast), 3000);
        }, 5000);
    }
}