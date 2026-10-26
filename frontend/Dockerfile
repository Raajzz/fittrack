# Builds the WEB target as static files and serves them with nginx.
# (Native iOS/Android are shipped via EAS/stores, not Docker — see README.)
# This image is fully independent of the backend; it only needs the API URL
# baked in at build time via EXPO_PUBLIC_API_URL.

# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
ARG EXPO_PUBLIC_API_URL=http://localhost:8080
ENV EXPO_PUBLIC_API_URL=$EXPO_PUBLIC_API_URL
RUN npx expo export --platform web

# --- Run stage ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# SPA routing: fall back to index.html.
RUN printf 'server {\n  listen 80;\n  root /usr/share/nginx/html;\n  location / {\n    try_files $uri $uri.html $uri/ /index.html;\n  }\n}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
