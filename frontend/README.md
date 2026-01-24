# Frontend (Angular 17)

Single-page dashboard that consumes the Spring Boot backend (`backend-module`) to manage benefícios and perform saldo transfers with optimistic locking guarantees.

## Stack & Highlights

- Angular 17 standalone app with SSR enabled
- Dark-blue visual system inspired by night terminals
- Reactive Forms for CRUD + transfer flows
- `BeneficioService` centralizes REST access to `/api/v1/beneficios`
- Http interceptors replaced by `provideHttpClient(withFetch())`
- Docker support with nginx reverse proxy
- Docker Swarm secrets integration

## Running locally

1. Install dependencies once:
	```bash
	cd frontend
	npm install
	```
2. Make sure the backend is running (default: `http://localhost:8080`).
3. Start the dev server:
	```bash
	npm start
	```
4. Open [http://localhost:4200](http://localhost:4200) to use the dashboard.

## API base URL

The API URL is configured in `src/app/config/api.config.ts`:

- **Development** (port 4200): Uses `http://localhost:8080/api/v1`
- **Production/Docker**: Uses relative path `/api/v1` (proxied by nginx)

## Docker Deployment

### Building the Docker Image

```bash
docker build -t bipapp-frontend:latest .
```

### Docker Compose

The frontend integrates with the main `docker-compose.yml` in the project root.

### Docker Swarm with Secrets

When deployed with Docker Swarm, the frontend uses Docker secrets for configuration:

1. **Secret mounting**: The secret `teste_bip_secrets` is mounted at `/run/secrets/teste_bip_secrets`
2. **Entrypoint script**: `docker-entrypoint.sh` loads environment variables from the secrets file
3. **Nginx configuration**: Uses `envsubst` to substitute `${BACKEND_API}` in nginx config

#### Environment Variables (from secrets)

| Variable | Description | Default |
|----------|-------------|---------|
| `BACKEND_API` | Backend service URL for nginx proxy | `http://backend:8080` |

### Traefik Integration

The frontend includes Traefik labels for reverse proxy:

```yaml
deploy:
  labels:
    - 'traefik.enable=true'
    - 'traefik.docker.network=web'
    - "traefik.http.routers.teste_bip.rule=PathPrefix(`/teste_bip`)"
    - "traefik.http.routers.teste_bip.entryPoints=https"
    - "traefik.http.routers.teste_bip.tls.certresolver=myresolver"
    - "traefik.http.routers.teste_bip_api.rule=PathPrefix(`/api`)"
```

### Nginx Configuration

The nginx server:
- Serves the Angular app at `/teste_bip`
- Proxies `/api/*` requests to the backend service
- Uses `absolute_redirect off` to preserve ports in redirects
- Caches static assets with 1-year expiry

## Tests

Run the unit suite with:

```bash
npm run test -- --watch=false --browsers=ChromeHeadless
```

The provided specs mock HTTP calls to validate CRUD behavior and form validation rules.

### Test Coverage

Current coverage: ~97% statements, 100% functions

## Build

```bash
npm run build
```

Artifacts will be generated in `dist/bip-app/` (including the SSR server bundle).

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── config/          # API configuration
│   │   ├── models/          # TypeScript interfaces
│   │   ├── services/        # Angular services
│   │   ├── app.component.*  # Main component
│   │   └── app.routes.ts    # Routing
│   ├── assets/              # Static assets
│   └── styles.scss          # Global styles
├── Dockerfile               # Multi-stage Docker build
├── docker-entrypoint.sh     # Secrets loader script
├── nginx.conf               # Nginx template config
└── package.json
```

## Files for Docker Swarm

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build (Node → Nginx) |
| `docker-entrypoint.sh` | Loads secrets and configures nginx |
| `nginx.conf` | Nginx template with `${BACKEND_API}` substitution |
