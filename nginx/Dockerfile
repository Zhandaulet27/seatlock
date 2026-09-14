# ---- Build stage ----
# Builds the React app into static files. Nothing from this stage ends up in
# the final image except the compiled dist/ output - no Node.js, no
# node_modules, no source, in the image that actually runs on EC2.
FROM node:20-alpine AS frontend-build
WORKDIR /app

# Copy only the package manifests first so Docker can cache the
# `npm ci` layer - it's only invalidated when dependencies actually change,
# not on every source edit (same trick the backend's own Dockerfile uses
# with pom.xml).
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ---- Run stage ----
FROM nginx:1.27-alpine
COPY --from=frontend-build /app/dist /usr/share/nginx/html
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
