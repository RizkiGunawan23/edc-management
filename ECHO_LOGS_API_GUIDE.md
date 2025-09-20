# Echo Logs API Testing Guide

## Authentication Requirements

### POST /api/edc/echo (Terminal Echo)
- **Authentication**: None (uses signature validation)
- **Header Required**: `Signature` (HmacSHA256)
- **Purpose**: Terminal EDC sends echo requests

### GET /api/edc/echo (View Echo Logs)
- **Authentication**: JWT Token required
- **Header Required**: `Authorization: Bearer <JWT_TOKEN>`
- **Purpose**: Admin/User views echo log history

## Step 1: Get JWT Token (for GET requests)

First, sign in to get JWT token:

```bash
curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

Response will contain `access_token`. Use this token for GET requests.

## Step 2: Test GET /api/edc/echo Endpoint

### Basic Request (Default Pagination)
```bash
curl -X GET "http://localhost:8080/api/edc/echo" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### With Pagination Parameters
```bash
curl -X GET "http://localhost:8080/api/edc/echo?page=0&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### With Sorting
```bash
curl -X GET "http://localhost:8080/api/edc/echo?sortBy=timestamp&sortDirection=desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### With Terminal ID Filter (Contains Search)
```bash
curl -X GET "http://localhost:8080/api/edc/echo?terminalId=EDC" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Filter by Partial Terminal ID
```bash
curl -X GET "http://localhost:8080/api/edc/echo?terminalId=001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### With Date Range Filter
```bash
curl -X GET "http://localhost:8080/api/edc/echo?timestampFrom=2024-01-01&timestampTo=2024-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Combined Parameters
```bash
curl -X GET "http://localhost:8080/api/edc/echo?page=0&size=10&sortBy=timestamp&sortDirection=desc&terminalId=EDC&timestampFrom=2024-01-01" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Expected Response Format

```json
{
  "message": "Echo logs retrieved successfully",
  "data": {
    "echoLogs": [
      {
        "id": 1,
        "terminalId": "EDC-001",
        "timestamp": "2024-01-15T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false,
    "numberOfElements": 10,
    "sortBy": "timestamp",
    "sortDirection": "desc",
    "appliedFilters": "terminalId=EDC, timestampFrom=2024-01-01"
  }
}
```

## Filter Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| page | Integer | Page number (0-based) | 0 |
| size | Integer | Page size (1-100) | 10 |
| sortBy | String | Field to sort by | timestamp |
| sortDirection | String | Sort direction (asc/desc) | desc |
| terminalId | String | Filter by terminal ID (contains search, case insensitive) | EDC |
| timestampFrom | String | Filter from date (YYYY-MM-DD) | 2024-01-01 |
| timestampTo | String | Filter to date (YYYY-MM-DD) | 2024-12-31 |

## Error Responses

### 401 Unauthorized (Missing/Invalid JWT)
```json
{
  "message": "Invalid token or token expired"
}
```

### 400 Bad Request (Invalid Parameters)
```json
{
  "message": "Validation failed",
  "errors": [
    "Page number must be 0 or greater",
    "Page size must be at least 1"
  ]
}
```

## Summary

1. **POST /api/edc/echo**: No JWT needed, uses signature validation for terminal authentication
2. **GET /api/edc/echo**: Requires JWT authentication for user/admin access
3. **Terminal ID Filter**: Uses LIKE pattern (case insensitive) for flexible searching
4. **Pagination**: Supports page, size, sortBy, and sortDirection
5. **Date Filtering**: Supports timestampFrom and timestampTo with YYYY-MM-DD format