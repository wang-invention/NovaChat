import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

export default defineConfig({
  plugins: [uni()],
  optimizeDeps: {
    include: ['@dcloudio/uni-h5']
  },
  server: {
    host: "0.0.0.0",
    port: 5173,
    proxy: {
      "/api": {
        target: "http://129.211.0.210:8080",
        changeOrigin: true,
      },
      "/ai-api": {
        target: "http://129.211.0.210:8000",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ai-api/, "/api/v1"),
      },
      "/ws": {
        target: "ws://129.211.0.210:8087",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
