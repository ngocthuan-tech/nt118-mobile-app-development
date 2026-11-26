import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

// Web được phục vụ ở https://<tên miền server>/admin/ (Caddy), cùng tên miền với API → không cần CORS.
// Chạy thử trên máy: npm run dev → http://localhost:5173/admin/ , các request /api chuyển thẳng tới server Azure.
const API_SERVER = process.env.VITE_API_SERVER ?? 'https://aicoach-r15.koreacentral.cloudapp.azure.com';

export default defineConfig({
  base: '/admin/',
  plugins: [react()],
  server: {
    proxy: {
      '/api': { target: API_SERVER, changeOrigin: true, secure: true },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        // Tách thư viện lớn thành file riêng: trình duyệt cache lâu, lần deploy sau chỉ tải lại phần code của mình
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          antd: ['antd', '@ant-design/icons'],
          charts: ['recharts'],
        },
      },
    },
  },
  test: {
    environment: 'node',
    include: ['src/**/*.test.ts'],
  },
});
