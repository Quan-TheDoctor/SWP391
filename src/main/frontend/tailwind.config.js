/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ["../resources/templates/fragments/*.{html,js}",
                "../resources/templates/*.{html,js}",
                "../resources/static/css/style.css",
                "../resources/static/*/*.css"], // it will be explained later
    theme: {
        extend: {},
    },
    plugins: [],
}