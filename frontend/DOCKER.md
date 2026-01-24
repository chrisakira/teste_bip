# Frontend - BIP App Angular

## Development

```bash
npm install
npm start
```

Access at: http://localhost:4200

## Docker Deployment

The frontend is configured to be deployed at a subpath `/teste_bip`.

### Build and Run with Docker Compose

```bash
# From project root
docker-compose up --build frontend
```

Access at: http://localhost:4000/teste_bip

### Customizing the Subpath

To change the subpath, modify:

1. `Dockerfile` - Change `BASE_HREF` arg and the copy destination path
2. `nginx.conf` - Update the location blocks
3. Rebuild the image

### Environment Configuration

The API base URL is automatically determined:
- **Development** (localhost): Uses `http://localhost:8080/api/v1`
- **Production**: Uses same-origin `/api/v1` (requires reverse proxy to backend)

### Production Setup with ngrok

When deploying behind ngrok (e.g., `http://akira.ngrok.dev/teste_bip`), ensure:

1. Your reverse proxy routes `/teste_bip` to the frontend container (port 80)
2. Your reverse proxy routes `/api` to the backend container (port 8080)

Example nginx config for the main reverse proxy:

```nginx
location /teste_bip {
    proxy_pass http://frontend:80;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location /api {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```
