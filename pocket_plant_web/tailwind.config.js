/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}", // src 내부의 컴포넌트들을 감지합니다.
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}