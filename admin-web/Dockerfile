# Web admin AI Coach — build ra file tĩnh rồi chép vào volume dùng chung với Caddy.
# docker compose (thư mục backend/) chạy service này 1 lần mỗi lần deploy; Caddy phục vụ ở https://<API_DOMAIN>/admin/

# ---- Giai đoạn 1: build bằng Node ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# Máy ảo nhỏ: giới hạn bộ nhớ Node và chỉ chạy vite build (kiểm tra kiểu TypeScript đã chạy ở GitHub Actions)
ENV NODE_OPTIONS=--max-old-space-size=512
RUN npx vite build

# ---- Giai đoạn 2: image rất nhỏ chỉ chứa file tĩnh ----
FROM alpine:3.20
COPY --from=build /app/dist /dist
# Xoá bản cũ trong volume rồi chép bản mới (tránh sót file của lần build trước)
CMD ["sh", "-c", "mkdir -p /srv/admin && find /srv/admin -mindepth 1 -delete && cp -r /dist/. /srv/admin/ && echo 'Da chep web admin vao /srv/admin'"]
