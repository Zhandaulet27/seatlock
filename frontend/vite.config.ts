import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev-only proxy: lets `npm run dev` (Vite on :5173) call the same relative
// "/api/..." paths the production build uses, forwarding them to the
// Spring Boot app on :8080 - either your locally-running `mvn spring-boot:run`
// or the Dockerized backend (which publishes 8080 on 127.0.0.1). No CORS
// config needed on the backend at all, in dev or in prod: in prod, Nginx
// serves this app's static files AND proxies /api to the backend on the same
// origin (see ../nginx/nginx.conf), so the browser never sees a cross-origin
// request either way.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/swagger-ui': 'http://localhost:8080',
      '/v3/api-docs': 'http://localhost:8080',
    },
  },
})
