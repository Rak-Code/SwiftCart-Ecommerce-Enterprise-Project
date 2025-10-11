# 🐳 Docker Deployment Guide

This guide explains how to deploy the Athena backend application using Docker and Docker Compose.

## 📋 Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)
- At least 2GB of free RAM
- At least 5GB of free disk space

## 🏗️ Architecture

The application consists of three services:

1. **MySQL Database** - Persistent data storage
2. **Redis Cache** - Caching layer for improved performance
3. **Spring Boot Backend** - Main application server

## 🚀 Quick Start

### 1. Clone and Navigate

```bash
cd backend
```

### 2. Configure Environment Variables

Copy the example environment file and update with your credentials:

```bash
cp .env.example .env
```

Edit `.env` file with your actual values:

```env
# Database
DB_NAME=athena
DB_USERNAME=athena_user
DB_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here

# Email (Gmail example)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password

# Razorpay
RAZORPAY_KEY_ID=your_razorpay_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
```

### 3. Build and Run

**Option A: Using pre-built JAR (faster)**

```bash
# Build the application first
mvn clean package -DskipTests

# Start all services
docker-compose up -d
```

**Option B: Build inside Docker (no local Maven needed)**

The Dockerfile uses multi-stage build, so you can build directly:

```bash
docker-compose up -d --build
```

### 4. Verify Deployment

Check if all services are running:

```bash
docker-compose ps
```

Expected output:
```
NAME              STATUS         PORTS
athena-backend    Up (healthy)   0.0.0.0:8080->8080/tcp
athena-mysql      Up (healthy)   0.0.0.0:3306->3306/tcp
athena-redis      Up (healthy)   0.0.0.0:6379->6379/tcp
```

### 5. Access the Application

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 📝 Docker Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f redis
```

### Stop Services

```bash
# Stop all services (keeps data)
docker-compose stop

# Stop and remove containers (keeps data)
docker-compose down

# Stop and remove everything including volumes (⚠️ deletes all data)
docker-compose down -v
```

### Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Rebuild After Code Changes

```bash
# Rebuild and restart backend only
docker-compose up -d --build backend

# Rebuild everything
docker-compose up -d --build
```

### Access Container Shell

```bash
# Backend container
docker exec -it athena-backend sh

# MySQL container
docker exec -it athena-mysql mysql -u root -p

# Redis container
docker exec -it athena-redis redis-cli
```

## 🔧 Configuration

### Environment Variables

All configuration is done through environment variables. You can:

1. **Use `.env` file** (recommended for local development)
2. **Set in `docker-compose.yml`** (for specific deployments)
3. **Pass via command line**:
   ```bash
   DB_PASSWORD=newpass docker-compose up -d
   ```

### Port Mapping

Default ports can be changed in `.env`:

```env
APP_PORT=8080      # Backend application
DB_PORT=3306       # MySQL database
REDIS_PORT=6379    # Redis cache
```

### Memory Limits

Adjust JVM memory in `docker-compose.yml`:

```yaml
environment:
  JAVA_OPTS: "-Xms512m -Xmx1024m"  # Min 512MB, Max 1GB
```

## 🗄️ Database Management

### Backup Database

```bash
docker exec athena-mysql mysqldump -u root -p${DB_PASSWORD} athena > backup.sql
```

### Restore Database

```bash
docker exec -i athena-mysql mysql -u root -p${DB_PASSWORD} athena < backup.sql
```

### Access MySQL CLI

```bash
docker exec -it athena-mysql mysql -u root -p
```

## 🐛 Troubleshooting

### Backend Won't Start

1. **Check logs**:
   ```bash
   docker-compose logs backend
   ```

2. **Verify database connection**:
   ```bash
   docker-compose logs mysql
   ```

3. **Check health status**:
   ```bash
   docker inspect athena-backend | grep -A 10 Health
   ```

### Database Connection Issues

1. **Wait for MySQL to be ready** (it takes ~30 seconds on first start)
2. **Check MySQL health**:
   ```bash
   docker-compose ps mysql
   ```

3. **Verify credentials** in `.env` file

### Port Already in Use

If ports are already in use, change them in `.env`:

```env
APP_PORT=8081
DB_PORT=3307
REDIS_PORT=6380
```

### Out of Memory

Increase Docker memory limit:
- Docker Desktop → Settings → Resources → Memory (increase to 4GB+)

### Clear Everything and Start Fresh

```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild from scratch
docker-compose up -d --build
```

## 🚀 Production Deployment

### Security Checklist

- [ ] Change all default passwords
- [ ] Use strong JWT secret (256+ bits)
- [ ] Enable HTTPS/TLS
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Use Docker secrets for sensitive data
- [ ] Limit exposed ports
- [ ] Enable firewall rules
- [ ] Regular security updates

### Performance Optimization

1. **Increase JVM memory**:
   ```yaml
   JAVA_OPTS: "-Xms1g -Xmx2g"
   ```

2. **Use production profile**:
   ```env
   SPRING_PROFILES_ACTIVE=prod
   ```

3. **Enable connection pooling** (already configured in application.properties)

4. **Monitor resources**:
   ```bash
   docker stats
   ```

### Scaling

To run multiple backend instances:

```bash
docker-compose up -d --scale backend=3
```

Use a load balancer (nginx/traefik) to distribute traffic.

## 📊 Monitoring

### Health Checks

All services have health checks configured:

```bash
# Check health status
docker-compose ps

# Detailed health info
docker inspect athena-backend --format='{{json .State.Health}}'
```

### Resource Usage

```bash
# Real-time stats
docker stats

# Disk usage
docker system df
```

## 🔄 Updates

### Update Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose up -d --build backend
```

### Update Dependencies

```bash
# Update base images
docker-compose pull

# Rebuild with new images
docker-compose up -d --build
```

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

## 🆘 Support

If you encounter issues:

1. Check the logs: `docker-compose logs -f`
2. Verify environment variables: `docker-compose config`
3. Check service health: `docker-compose ps`
4. Review this guide's troubleshooting section

---

**Happy Deploying! 🚀**
