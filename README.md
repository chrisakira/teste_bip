# BIP App — Benefícios Management System

A fullstack application for managing employee benefits (benefícios) with support for CRUD operations and value transfers between benefits.

## 🏗️ Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Frontend     │────▶│    Backend      │────▶│   PostgreSQL    │
│  (Angular 17)   │     │ (Spring Boot)   │     │      (16)       │
│    Port: 80     │     │   Port: 8080    │     │   Port: 5432    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## 📁 Project Structure

```
teste_bip/
├── frontend/           # Angular 17 SPA
├── backend-module/     # Spring Boot REST API
├── db/                 # Database schema and seed scripts
├── scripts/            # Helper scripts
├── docker-compose.yml  # Docker Swarm deployment
├── .env                # Environment variables (local)
└── .example-env        # Example environment file
```

## 🚀 Quick Start

### Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- (Optional) Docker Swarm initialized for production deployment

---

## 📦 Deployment Options

### Option 1: Docker Compose (Development/Local)

This is the simplest way to run the application locally.

#### 1. Create environment file

```bash
cp .example-env .env
```

Edit `.env` with your configuration:

```env
# Database credentials
POSTGRES_DB=bipapp
POSTGRES_USER=bipuser
POSTGRES_PASSWORD=bippass
DB_HOST=postgres
DB_PORT=5432
DB_NAME=bipapp
DB_USER=bipuser
DB_PASSWORD=bippass

# Application settings
SPRING_PROFILES_ACTIVE=docker
BACKEND_API=http://backend:8080
```

#### 2. Build images

```bash
# Build frontend
cd frontend
docker build -t bipapp-frontend:latest .

# Build backend
cd ../backend-module
docker build -t bipapp-backend:latest .
```

#### 3. Start with Docker Compose

```bash
docker-compose up -d
```

#### 4. Access the application

- **Frontend**: http://localhost:80/teste_bip
- **Backend API**: http://localhost:8080/api/v1/beneficios
- **Database**: localhost:5432

#### 5. Stop the application

```bash
docker-compose down
```

---

### Option 2: Docker Swarm (Production)

Docker Swarm deployment uses **Docker Secrets** for secure credential management instead of environment files.

#### 1. Initialize Docker Swarm (if not already)

```bash
docker swarm init
```

#### 2. Create Docker Secret

Create a secret from your `.env` file:

```bash
docker secret create teste_bip_secrets .env
```

Or create it manually:

```bash
cat << EOF | docker secret create teste_bip_secrets -
POSTGRES_DB=bipapp
POSTGRES_USER=bipuser
POSTGRES_PASSWORD=bippass
DB_HOST=postgres
DB_PORT=5432
DB_NAME=bipapp
DB_USER=bipuser
DB_PASSWORD=bippass
SPRING_PROFILES_ACTIVE=docker
BACKEND_API=http://backend:8080
EOF
```

#### 3. Create external network (for Traefik integration)

```bash
docker network create --driver overlay web
```

#### 4. Build images

```bash
# Build frontend
cd frontend
docker build -t bipapp-frontend:latest .

# Build backend
cd ../backend-module
docker build -t bipapp-backend:latest .
```

#### 5. Deploy the stack

```bash
docker stack deploy -c docker-compose.yml teste_bip
```

#### 6. Check deployment status

```bash
# List services
docker stack services teste_bip

# Check service logs
docker service logs teste_bip_frontend
docker service logs teste_bip_backend
docker service logs teste_bip_postgres
```

#### 7. Access the application

With Traefik reverse proxy:
- **Frontend**: https://your-domain/teste_bip
- **API**: https://your-domain/api/v1/beneficios

Without Traefik (direct port access):
- **Frontend**: http://localhost:8090/teste_bip
- **Backend**: http://localhost:8080/api/v1/beneficios

#### 8. Update the stack

After making changes, rebuild and update:

```bash
# Rebuild images
docker build -t bipapp-frontend:latest ./frontend
docker build -t bipapp-backend:latest ./backend-module

# Update services
docker service update --force teste_bip_frontend
docker service update --force teste_bip_backend
```

#### 9. Remove the stack

```bash
docker stack rm teste_bip
```

---

## 🔐 Docker Secrets

Docker Swarm uses secrets to securely pass sensitive data to containers.

### How Secrets Work

1. Secrets are created and stored encrypted in the Swarm
2. Secrets are mounted as files at `/run/secrets/<secret_name>` inside containers
3. The application reads environment variables from the secrets file at startup

### Secret Structure

The `teste_bip_secrets` secret should contain:

```env
# Database Configuration
POSTGRES_DB=bipapp
POSTGRES_USER=bipuser
POSTGRES_PASSWORD=your_secure_password
DB_HOST=postgres
DB_PORT=5432
DB_NAME=bipapp
DB_USER=bipuser
DB_PASSWORD=your_secure_password

# Application Settings
SPRING_PROFILES_ACTIVE=docker
BACKEND_API=http://backend:8080

# Optional: CORS (for external access)
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### Managing Secrets

```bash
# List secrets
docker secret ls

# Inspect a secret (metadata only)
docker secret inspect teste_bip_secrets

# Remove a secret (must remove stack first)
docker secret rm teste_bip_secrets

# Update a secret (requires recreating)
docker secret rm teste_bip_secrets
docker secret create teste_bip_secrets .env
docker stack deploy -c docker-compose.yml teste_bip
```

---

## 🌐 Traefik Integration

The frontend service includes Traefik labels for reverse proxy integration:

| Label | Description |
|-------|-------------|
| `traefik.enable=true` | Enable Traefik for this service |
| `traefik.docker.network=web` | Network for Traefik communication |
| `traefik.http.routers.teste_bip.rule=PathPrefix('/teste_bip')` | Route `/teste_bip` to frontend |
| `traefik.http.routers.teste_bip_api.rule=PathPrefix('/api')` | Route `/api` to frontend nginx proxy |
| `traefik.http.routers.*.entryPoints=https` | Use HTTPS entrypoint |
| `traefik.http.routers.*.tls.certresolver=myresolver` | Use Let's Encrypt certificates |

---

## 🧪 Running Tests

### Frontend Tests

```bash
cd frontend
npm install
npm test -- --watch=false --browsers=ChromeHeadless
```

### Backend Tests

```bash
cd backend-module
./scripts/run-tests.sh -q
```

---

## 📚 Documentation

- [Frontend Documentation](frontend/README.md) - Angular app details
- [Backend Documentation](backend-module/README.md) - Spring Boot API details
- [Docker Documentation](frontend/DOCKER.md) - Container details

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Backend can't connect to database

```bash
# Check if postgres is running
docker service ps teste_bip_postgres

# Check postgres logs
docker service logs teste_bip_postgres

# Verify secrets are loaded
docker exec $(docker ps -q -f name=teste_bip_backend) cat /run/secrets/teste_bip_secrets
```

#### 2. Frontend returns 502 Bad Gateway

```bash
# Check backend is running
docker service ps teste_bip_backend

# Check backend logs
docker service logs teste_bip_backend

# Verify network connectivity
docker exec $(docker ps -q -f name=teste_bip_frontend) ping backend
```

#### 3. Secrets not loading

Ensure the secret exists and services are configured correctly:

```bash
docker secret ls | grep teste_bip
docker service inspect teste_bip_backend --format '{{json .Spec.TaskTemplate.ContainerSpec.Secrets}}'
```

#### 4. Database data not persisting

The PostgreSQL data is stored in a Docker volume. Check:

```bash
docker volume ls | grep pgdata
```

---

## 📋 Environment Variables Reference

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | PostgreSQL database name | `bipapp` |
| `POSTGRES_USER` | PostgreSQL username | `bipuser` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `bippass` |
| `DB_HOST` | Database host for backend | `postgres` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name for backend | `bipapp` |
| `DB_USER` | Database user for backend | `bipuser` |
| `DB_PASSWORD` | Database password for backend | `bippass` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `docker` |
| `BACKEND_API` | Backend URL for nginx proxy | `http://backend:8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | (all origins) |

---

## 📄 License

This project is for assessment/demonstration purposes.
