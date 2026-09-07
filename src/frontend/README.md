# Banana.math frontend

Vue 3 single-page application built with Vite.

```bash
npm install
npm run dev
npm run lint
npm run build
```

The Vite development server runs on port 3000 and proxies `/api` to Spring Boot on port 8080. Maven runs `npm ci` and copies the production bundle into the Spring Boot executable JAR.
