# Makefile for Athena Backend Docker Management

.PHONY: help build up down restart logs clean rebuild test backup restore

# Default target
help:
	@echo "Athena Backend - Docker Management Commands"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  build          - Build Docker images"
	@echo "  up             - Start all services"
	@echo "  down           - Stop all services"
	@echo "  restart        - Restart all services"
	@echo "  logs           - View logs (all services)"
	@echo "  logs-backend   - View backend logs only"
	@echo "  logs-mysql     - View MySQL logs only"
	@echo "  logs-redis     - View Redis logs only"
	@echo "  ps             - Show running containers"
	@echo "  clean          - Stop and remove containers"
	@echo "  clean-all      - Remove containers, volumes, and images"
	@echo "  rebuild        - Clean and rebuild everything"
	@echo "  shell-backend  - Access backend container shell"
	@echo "  shell-mysql    - Access MySQL CLI"
	@echo "  shell-redis    - Access Redis CLI"
	@echo "  backup         - Backup MySQL database"
	@echo "  restore        - Restore MySQL database from backup"
	@echo "  health         - Check service health status"
	@echo "  stats          - Show resource usage"
	@echo "  test           - Run application tests"

# Build Docker images
build:
	@echo "Building Docker images..."
	docker-compose build

# Start all services
up:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Services started! Access the app at http://localhost:8080"

# Stop all services
down:
	@echo "Stopping all services..."
	docker-compose down

# Restart all services
restart:
	@echo "Restarting all services..."
	docker-compose restart

# View logs for all services
logs:
	docker-compose logs -f

# View backend logs only
logs-backend:
	docker-compose logs -f backend

# View MySQL logs only
logs-mysql:
	docker-compose logs -f mysql

# View Redis logs only
logs-redis:
	docker-compose logs -f redis

# Show running containers
ps:
	docker-compose ps

# Stop and remove containers
clean:
	@echo "Stopping and removing containers..."
	docker-compose down

# Remove containers, volumes, and images
clean-all:
	@echo "Removing containers, volumes, and images..."
	docker-compose down -v --rmi all

# Clean and rebuild everything
rebuild:
	@echo "Rebuilding everything..."
	docker-compose down
	docker-compose build --no-cache
	docker-compose up -d

# Access backend container shell
shell-backend:
	docker exec -it athena-backend sh

# Access MySQL CLI
shell-mysql:
	docker exec -it athena-mysql mysql -u root -p

# Access Redis CLI
shell-redis:
	docker exec -it athena-redis redis-cli

# Backup MySQL database
backup:
	@echo "Creating database backup..."
	@mkdir -p backups
	docker exec athena-mysql mysqldump -u root -p$${DB_PASSWORD:-Rak@1411} athena > backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "Backup created in backups/ directory"

# Restore MySQL database from backup
restore:
	@echo "Restoring database from backup..."
	@read -p "Enter backup file path: " backup_file; \
	docker exec -i athena-mysql mysql -u root -p$${DB_PASSWORD:-Rak@1411} athena < $$backup_file
	@echo "Database restored successfully"

# Check service health status
health:
	@echo "Checking service health..."
	@docker inspect athena-backend --format='Backend: {{.State.Health.Status}}' 2>/dev/null || echo "Backend: Not running"
	@docker inspect athena-mysql --format='MySQL: {{.State.Health.Status}}' 2>/dev/null || echo "MySQL: Not running"
	@docker inspect athena-redis --format='Redis: {{.State.Health.Status}}' 2>/dev/null || echo "Redis: Not running"

# Show resource usage
stats:
	docker stats --no-stream

# Run application tests
test:
	mvn clean test

# Development mode - rebuild backend only
dev:
	@echo "Rebuilding backend for development..."
	docker-compose up -d --build backend
	docker-compose logs -f backend
