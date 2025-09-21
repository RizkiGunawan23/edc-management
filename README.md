# EDC Management System

A comprehensive RESTful API for Electronic Data Capture (EDC) terminal management with CRUD operations, authentication, and echo functionality.

## 📋 Table of Contents

-   [Overview](#overview)
-   [Features](#features)
-   [Technology Stack](#technology-stack)
-   [System Requirements](#system-requirements)
-   [Installation & Setup](#installation--setup)
-   [Configuration](#configuration)
-   [Running the Application](#running-the-application)
-   [API Documentation](#api-documentation)
-   [Authentication](#authentication)
-   [Monitoring & Logging](#monitoring--logging)
-   [Troubleshooting](#troubleshooting)

## 🎯 Overview

The EDC Management System is a Spring Boot-based application designed to manage Electronic Data Capture terminals in a banking or payment processing environment. It provides secure APIs for terminal registration, management, and echo testing functionality.

## ✨ Features

-   **Terminal Management**: Full CRUD operations for EDC terminals
-   **Authentication & Authorization**: JWT-based security with role management
-   **Echo Testing**: Test terminal connectivity and response
-   **Audit Logging**: Comprehensive logging for all operations
-   **Data Validation**: Request validation with detailed error messages
-   **Database Integration**: PostgreSQL with JPA/Hibernate
-   **RESTful API**: Clean API design with proper HTTP status codes
-   **Security**: HMAC signature validation for sensitive operations

## 🛠 Technology Stack

-   **Backend Framework**: Spring Boot 2.7.18
-   **Java Version**: OpenJDK 1.8+
-   **Database**: PostgreSQL 12+
-   **Security**: Spring Security with JWT
-   **Build Tool**: Maven 3.6+
-   **ORM**: JPA/Hibernate
-   **Validation**: Bean Validation (JSR-303)
-   **Logging**: SLF4J with Logback

## 📋 System Requirements

### Development Environment

-   **Java**: OpenJDK 1.8 or higher
-   **Maven**: 3.6.0 or higher
-   **PostgreSQL**: 12.0 or higher
-   **IDE**: IntelliJ IDEA, Eclipse, or VS Code (recommended)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/RizkiGunawan23/edc-management.git
cd edc-management
```

### 2. Database Setup

```sql
-- Create database
CREATE DATABASE edc_db;

-- Create user (optional)
CREATE USER edc_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE edc_db TO edc_user;
```

### 3. Install Dependencies

```bash
mvn clean install
```

## ⚙️ Configuration

### Database Configuration

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/edc_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Security Configuration

```properties
# JWT Configuration
application.security.jwt.secret-key=your-256-bit-secret-key
application.security.jwt.expiration=1800000
application.security.jwt.refresh-expiration=604800000

# Signature Configuration
application.security.signature.secret-key=your-signature-secret
```

## 🎮 Running the Application

### Development Mode

```bash
# Using Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication Endpoints

#### 1. User Sign Up

```http
POST /api/auth/sign-up
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**Response:**

```json
{
    "message": "User sign up successfully",
    "data": {
        "tokens": {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        },
        "user": {
            "id": 1,
            "username": "admin"
        }
    }
}
```

#### 2. User Sign In

```http
POST /api/auth/sign-in
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**Response:**

```json
{
    "message": "User sign in successfully",
    "data": {
        "tokens": {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        },
        "user": {
            "id": 1,
            "username": "admin"
        }
    }
}
```

#### 3. Token Refresh

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**

```json
{
    "message": "Token refreshed successfully",
    "data": {
        "tokens": {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        },
        "user": {
            "id": 1,
            "username": "admin"
        }
    }
}
```

#### 4. User Sign Out

```http
POST /api/auth/sign-out
Authorization: Bearer {access_token}
```

**Response:**

```json
{
    "message": "User signed out successfully"
}
```

### Terminal Management Endpoints

#### 1. Create Terminal

```http
POST /api/edc
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "terminalId": "EDC-JKT-001",
  "location": "Jakarta Main Office",
  "status": "ACTIVE",
  "serialNumber": "SN123456789",
  "model": "V400m",
  "manufacturer": "Verifone",
  "ipAddress": "192.168.1.100"
}
```

**Response:**

```json
{
    "message": "Terminal EDC created successfully",
    "data": {
        "terminalId": "EDC-JKT-001",
        "location": "Jakarta Main Office",
        "status": "ACTIVE",
        "serialNumber": "SN123456789",
        "model": "V400m",
        "manufacturer": "Verifone",
        "lastMaintenance": null,
        "ipAddress": "192.168.1.100",
        "createdAt": "2023-05-31T10:30:00",
        "updatedAt": "2023-05-31T10:30:00"
    }
}
```

#### 2. Get All Terminals

```http
GET /api/edc?page=0&size=10&sortBy=createdAt&sortDirection=desc&terminalType=KIOSK&status=ACTIVE&location=Jakarta
Authorization: Bearer {access_token}
```

**Query Parameters:**

-   `page` (optional): Page number (default: 0)
-   `size` (optional): Page size (default: 10, max: 100)
-   `sortBy` (optional): Sort field (default: "createdAt")
-   `sortDirection` (optional): Sort direction "asc" or "desc" (default: "desc")
-   `status` (optional): Filter by status (ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE)
-   `location` (optional): Filter by location (contains search)
-   `manufacturer` (optional): Filter by manufacturer (contains search)
-   `model` (optional): Filter by model (contains search)
-   `terminalType` (optional): Filter by terminal type (EDC, ATM, POS, KIOSK)
-   `ipAddress` (optional): Filter by IP address (exact match)
-   `serialNumber` (optional): Filter by serial number (contains search)
-   `createdFrom` (optional): Filter by created date from (YYYY-MM-DD)
-   `createdTo` (optional): Filter by created date to (YYYY-MM-DD)
-   `lastMaintenanceFrom` (optional): Filter by last maintenance from (YYYY-MM-DD)
-   `lastMaintenanceTo` (optional): Filter by last maintenance to (YYYY-MM-DD)

**Response:**

```json
{
    "message": "Terminals retrieved successfully",
    "data": {
        "terminalEDC": [
            {
                "terminalId": "KIOSK-MKS-017",
                "location": "Makassar Edited",
                "status": "ACTIVE",
                "serialNumber": "MKS999238",
                "model": "KIOSK-Remote-2025",
                "manufacturer": "BCA Systems",
                "lastMaintenance": "2025-09-21T12:34:57.331",
                "ipAddress": "192.168.25.204",
                "createdAt": "2025-09-21T12:33:52.08",
                "updatedAt": "2025-09-21T12:34:57.332"
            },
            {
                "terminalId": "KIOSK-SMG-012",
                "location": "Semarang Trade Center",
                "status": "MAINTENANCE",
                "serialNumber": "SMG555666777",
                "model": "Kiosk-Pro-2024",
                "manufacturer": "Mandiri Tech",
                "lastMaintenance": null,
                "ipAddress": "192.168.1.112",
                "createdAt": "2025-09-20T20:23:12.356",
                "updatedAt": "2025-09-20T20:23:12.356"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 2,
        "totalPages": 1,
        "first": true,
        "last": true,
        "hasNext": false,
        "hasPrevious": false,
        "numberOfElements": 2,
        "sortBy": "createdAt",
        "sortDirection": "desc",
        "appliedFilters": "terminalType=KIOSK,status=ACTIVE"
    }
}
```

#### 3. Get Terminal by ID

```http
GET /api/edc/{terminalId}
Authorization: Bearer {access_token}
```

**Response:**

```json
{
    "message": "Terminal EDC retrieved successfully",
    "data": {
        "terminalId": "EDC-JKT-001",
        "location": "Jakarta Main Office",
        "status": "ACTIVE",
        "serialNumber": "SN123456789",
        "model": "V400m",
        "manufacturer": "Verifone",
        "lastMaintenance": null,
        "ipAddress": "192.168.1.100",
        "createdAt": "2023-05-31T10:30:00",
        "updatedAt": "2023-05-31T10:30:00"
    }
}
```

#### 4. Update Terminal

```http
PUT /api/edc/{terminalId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "location": "Jakarta Branch Office",
  "status": "INACTIVE",
  "model": "V240m",
  "ipAddress": "192.168.1.101"
}
```

**Response:**

```json
{
    "message": "Terminal EDC updated successfully",
    "data": {
        "terminalId": "EDC-JKT-001",
        "location": "Jakarta Branch Office",
        "status": "INACTIVE",
        "serialNumber": "SN123456789",
        "model": "V240m",
        "manufacturer": "Verifone",
        "lastMaintenance": null,
        "ipAddress": "192.168.1.101",
        "createdAt": "2023-05-31T10:30:00",
        "updatedAt": "2023-05-31T11:45:00"
    }
}
```

#### 5. Delete Terminal

```http
DELETE /api/edc/{terminalId}
Authorization: Bearer {access_token}
```

### Echo Testing Endpoints

#### 1. Echo Test

```http
POST /api/edc/echo
Signature: {HMAC_SHA256_signature}
Content-Type: application/json

{
  "terminalId": "EDC-JKT-001"
}
```

**Headers:**

-   `Signature`: HMAC-SHA256 signature of the request body using the secret key

**Signature Generation Example (JavaScript):**

```javascript
const crypto = require("crypto-js");
const requestBody = JSON.stringify({ terminalId: "EDC-JKT-001" });
const secretKey = "EDCmgmt2025!.?"; // From application.properties
const signature = crypto.HmacSHA256(requestBody, secretKey).toString();
```

**Response:**

```json
{
    "message": "Echo test successful",
    "data": {
        "id": 1,
        "terminalId": "EDC-JKT-001",
        "timestamp": "2023-05-31T10:30:00"
    }
}
```

#### 2. Get Echo Logs

```http
GET /api/edc/echo-logs?page=0&size=10&sortBy=timestamp&sortDirection=desc&terminalId=edc
Authorization: Bearer {access_token}
```

**Query Parameters:**

-   `page` (optional): Page number (default: 0)
-   `size` (optional): Page size (default: 10, max: 100)
-   `sortBy` (optional): Sort field (default: "timestamp")
-   `sortDirection` (optional): Sort direction "asc" or "desc" (default: "desc")
-   `terminalId` (optional): Filter by terminal ID (contains search)
-   `timestampFrom` (optional): Filter by timestamp from (YYYY-MM-DD)
-   `timestampTo` (optional): Filter by timestamp to (YYYY-MM-DD)

**Response:**

```json
{
    "message": "Echo logs retrieved successfully",
    "data": {
        "echoLogs": [
            {
                "id": 21,
                "terminalId": "EDC-JKT-001",
                "timestamp": "2025-09-21T05:32:06"
            },
            {
                "id": 20,
                "terminalId": "EDC-JKT-001",
                "timestamp": "2025-09-21T05:23:55"
            },
            {
                "id": 19,
                "terminalId": "EDC-JKT-001",
                "timestamp": "2025-09-21T04:41:43"
            }
        ],
        "page": 0,
        "size": 5,
        "totalElements": 11,
        "totalPages": 3,
        "first": true,
        "last": false,
        "hasNext": true,
        "hasPrevious": false,
        "numberOfElements": 5,
        "sortBy": "timestamp",
        "sortDirection": "desc",
        "appliedFilters": "terminalId=edc"
    }
}
```

### Health Check

```http
GET /api/ping
```

**Response:**

```json
{
    "message": "Application is running"
}
```

## 📋 Terminal ID Format

Terminal IDs must follow this pattern: `{TYPE}-{LOCATION}-{SEQUENCE}`

-   **TYPE**: EDC, ATM, POS, KIOSK
-   **LOCATION**: 3 uppercase letters (e.g., JKT, BDG, SBY, DPS)
-   **SEQUENCE**: 3 digits (001-999)

**Examples:**

-   `EDC-JKT-001` - EDC terminal #1 in Jakarta
-   `ATM-BDG-045` - ATM terminal #45 in Bandung
-   `POS-SBY-123` - POS terminal #123 in Surabaya
-   `KIOSK-DPS-001` - Kiosk terminal #1 in Denpasar

## 📊 Terminal Status Values

-   **ACTIVE**: Terminal is operational and accepting transactions
-   **INACTIVE**: Terminal is not operational
-   **MAINTENANCE**: Terminal is under maintenance
-   **OUT_OF_SERVICE**: Terminal is permanently disabled

## 🟠 Postman Collection

For easy API testing, a Postman collection is included: `Technical Test Sarana Pactindo.postman_collection.json`

**Import Instructions:**

1. Open Postman
2. Click "Import" button
3. Select the collection file
4. Set `baseURL` variable to `http://localhost:8080/api`

## 📊 Monitoring & Logging

### Application Logs

Logs are stored in the `logs/` directory:

-   `edc-management.log` - Application logs
-   `audit.log` - Audit trail logs
-   `performance.log` - Performance metrics

### Log Levels

```properties
# Root level
logging.level.root=INFO

# Application level
logging.level.com.rizki.edcmanagement=DEBUG

# SQL logging (development only)
logging.level.org.hibernate.SQL=DEBUG
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Error**: `Connection to localhost:5432 refused`

**Solution**:

-   Ensure PostgreSQL is running
-   Check database credentials
-   Verify network connectivity

#### 2. JWT Token Expired

**Error**: `JWT token is expired`

**Solution**:

-   Use refresh token to get new access token
-   Re-authenticate if refresh token is also expired

### Debugging

#### Enable Debug Logging

```properties
logging.level.com.rizki.edcmanagement=DEBUG
logging.level.org.springframework.security=DEBUG
```

#### Database Query Logging

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
