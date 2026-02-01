# Docker Setup for Recrutement Intelligente

This directory contains the Docker configuration for running the **Plateforme de Recrutement Intelligente**.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Configure Environment Variables

```bash
cd docker
cp .env.example .env
```

Edit `.env` and set at minimum:
- `OPENAI_API_KEY` - Your OpenAI API key (required for AI features)
- `JWT_SECRET` - Generate with: `openssl rand -base64 32`

### 2. Start the Services

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** with PgVector extension on port 5432
- **Spring Boot Backend** on port 8080

### 3. Check Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

### 4. Access the Application

- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `OPENAI_API_KEY` | Yes | - | OpenAI API key for AI features |
| `JWT_SECRET` | Yes | - | Secret key for JWT tokens |
| `POSTGRES_DB` | No | recrutement | Database name |
| `POSTGRES_USER` | No | recrutement | Database user |
| `POSTGRES_PASSWORD` | No | recrutement123 | Database password |
| `BACKEND_PORT` | No | 8080 | Backend API port |
| `CORS_ALLOWED_ORIGINS` | No | localhost:3000,4200 | Allowed CORS origins |

## Commands

### Start Services

```bash
docker-compose up -d
```

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Volumes

```bash
docker-compose down -v
```

### Rebuild Backend

```bash
docker-compose build backend
docker-compose up -d backend
```

### View Logs

```bash
docker-compose logs -f backend
```

### Execute Commands in Container

```bash
# Backend shell
docker-compose exec backend sh

# PostgreSQL shell
docker-compose exec postgres psql -U recrutement -d recrutement
```

## Project Structure

```
docker/
├── docker-compose.yml    # Main Docker Compose configuration
├── .env.example          # Example environment variables
├── init-db.sql           # Database initialization script
└── README.md            # This file
```

## Volumes

The following named volumes are created:

- `recrutement_postgres_data` - PostgreSQL data
- `recrutement_cv_uploads` - Uploaded CV files
- `recrutement_backend_logs` - Application logs

## Database Initialization

The `init-db.sql` script runs automatically on first start and:
- Creates the database
- Enables the PgVector extension
- Sets up the vector table for embeddings

## Troubleshooting

### Backend Won't Start

1. Check logs: `docker-compose logs backend`
2. Verify PostgreSQL is healthy: `docker-compose ps`
3. Check environment variables in `.env`

### Database Connection Errors

1. Ensure PostgreSQL is running: `docker-compose ps postgres`
2. Check database credentials in `.env`
3. Verify the `init-db.sql` was executed

### OpenAI API Errors

1. Verify your API key is valid: https://platform.openai.com/api-keys
2. Check the key is correctly set in `.env`
3. Restart the backend: `docker-compose restart backend`

## Production Deployment

For production:

1. Use strong passwords for `POSTGRES_PASSWORD` and `JWT_SECRET`
2. Set `SPRING_PROFILES_ACTIVE=prod`
3. Configure proper `CORS_ALLOWED_ORIGINS`
4. Use a reverse proxy (nginx/traefik) for SSL
5. Set `LOG_LEVEL=INFO` or `LOG_LEVEL=WARN`

## Cleanup

To remove everything including volumes:

```bash
docker-compose down -v
docker system prune -a
```

## Support

For issues or questions, please refer to the main project documentation.
