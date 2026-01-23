# Stage 1: Build the Angular application
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

# Copy source code
COPY . .

# Build with base href for subpath deployment (skip prerendering for Docker build)
ARG BASE_HREF=/teste_bip/
RUN npm run build -- --base-href ${BASE_HREF} --configuration production --no-prerender

# Stage 2: Serve with nginx
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built Angular app to nginx html folder
COPY --from=builder /app/dist/bip-app/browser /usr/share/nginx/html/teste_bip

# Expose port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
