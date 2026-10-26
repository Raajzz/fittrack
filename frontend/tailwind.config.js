/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{js,jsx,ts,tsx}", "./src/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        ink: "#0f172a",
        surface: "#1e293b",
        brand: {
          DEFAULT: "#22c55e",
          dark: "#16a34a",
        },
        bulk: "#f59e0b",
        cut: "#ef4444",
        recomp: "#3b82f6",
      },
    },
  },
  plugins: [],
};
