# Multi-Stage: Angular-Build, Auslieferung über nginx.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Runtime-Config (config.json) wird beim Start aus ISSUER/CLIENT_ID-Env gerendert.
COPY docker-entrypoint.d/40-render-config.sh /docker-entrypoint.d/40-render-config.sh
RUN chmod +x /docker-entrypoint.d/40-render-config.sh
EXPOSE 80
