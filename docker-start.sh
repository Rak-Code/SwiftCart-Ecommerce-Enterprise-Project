#!/bin/bash

# Docker Quick Start Script for Linux/Mac
# Athena Backend Application

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================"
echo -e "  Athena Backend - Docker Deployment"
echo -e "========================================${NC}"
echo ""

# Check if Docker is running
echo -e "${YELLOW}Checking Docker status...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker is not running. Please start Docker.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker is running${NC}"

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠ .env file not found. Creating from .env.example...${NC}"
    cp .env.example .env
    echo -e "${GREEN}✓ Created .env file. Please update it with your credentials.${NC}"
    echo ""
    read -p "Press Enter to continue after updating .env file..."
fi

# Ask user for deployment option
echo ""
echo -e "${CYAN}Select deployment option:${NC}"
echo -e "${NC}1. Quick start (use pre-built JAR)"
echo -e "2. Full build (build inside Docker)"
echo -e "3. Development mode (rebuild backend only)${NC}"
echo ""
read -p "Enter choice (1-3): " choice

case $choice in
    1)
        echo ""
        echo -e "${YELLOW}Building application with Maven...${NC}"
        mvn clean package -DskipTests
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗ Maven build failed!${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}✓ Build successful${NC}"
        echo ""
        echo -e "${YELLOW}Starting Docker containers...${NC}"
        docker-compose up -d
        ;;
    2)
        echo ""
        echo -e "${YELLOW}Building and starting Docker containers...${NC}"
        docker-compose up -d --build
        ;;
    3)
        echo ""
        echo -e "${YELLOW}Rebuilding backend service...${NC}"
        docker-compose up -d --build backend
        ;;
    *)
        echo -e "${RED}Invalid choice. Exiting.${NC}"
        exit 1
        ;;
esac

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Docker deployment failed!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================"
echo -e "  Deployment Successful!"
echo -e "========================================${NC}"
echo ""

# Wait for services to be healthy
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 5

# Check service status
echo ""
echo -e "${CYAN}Service Status:${NC}"
docker-compose ps

echo ""
echo -e "${CYAN}========================================"
echo -e "  Access Your Application"
echo -e "========================================${NC}"
echo ""
echo -e "${NC}API Base URL:    http://localhost:8080"
echo -e "Swagger UI:      http://localhost:8080/swagger-ui.html"
echo -e "Health Check:    http://localhost:8080/actuator/health${NC}"
echo ""
echo -e "${CYAN}========================================"
echo -e "  Useful Commands"
echo -e "========================================${NC}"
echo ""
echo -e "${NC}View logs:       docker-compose logs -f"
echo -e "Stop services:   docker-compose down"
echo -e "Restart:         docker-compose restart${NC}"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
