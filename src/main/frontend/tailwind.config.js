/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "../resources/templates/**/*.{html,js}"
  ]
  ,
  theme: {
    extend: {
      colors: {
        'break-time': 'rgba(253, 230, 138, 0.5)',
        'overtime': 'rgba(252, 165, 165, 0.5)'
      }
    }
  },
  plugins: [],
}