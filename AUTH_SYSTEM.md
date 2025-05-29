# Authentication System Documentation

This Spring Boot project now includes a comprehensive JWT-based authentication system equivalent to Next.js auth.js functionality.

## Features

- **User Registration**: Create new user accounts with email validation
- **User Login**: Authenticate users with email and password
- **JWT Tokens**: Secure, stateless authentication using JSON Web Tokens
- **Password Encryption**: BCrypt password hashing for security
- **Role-Based Access Control**: Support for STUDENT, ADMIN, FACULTY, MANAGER roles
- **Password Change**: Secure password update functionality
- **Token Refresh**: Extend authentication sessions
- **Current User Info**: Get authenticated user details

## API Endpoints

### Authentication Endpoints

All authentication endpoints are prefixed with `/api/auth/`

#### POST `/api/auth/register`

Register a new user account.

**Request Body:**

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "profileId": "john123",
  "password": "securePassword123",
  "phoneNumber": "+1234567890",
  "role": "STUDENT"
}
```

**Response:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "profileId": "john123",
      "role": "STUDENT",
      "phoneNumber": "+1234567890",
      "image": null,
      "isActive": true
    }
  }
}
```

#### POST `/api/auth/login`

Authenticate user and receive JWT token.

**Request Body:**

```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "profileId": "john123",
      "role": "STUDENT",
      "phoneNumber": "+1234567890",
      "image": null,
      "isActive": true
    }
  }
}
```

#### GET `/api/auth/me`

Get current authenticated user information.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**

```json
{
  "success": true,
  "message": "User found",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "profileId": "john123",
    "role": "STUDENT",
    "phoneNumber": "+1234567890",
    "image": null,
    "isActive": true
  }
}
```

#### POST `/api/auth/change-password`

Change user password.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body:**

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newSecurePassword456"
}
```

#### POST `/api/auth/refresh-token`

Refresh the JWT token.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**

```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### POST `/api/auth/logout`

Logout user (stateless - client should remove token).

**Response:**

```json
{
  "success": true,
  "message": "Logout successful"
}
```

## Security Configuration

The system supports two security profiles:

### Development Profile (dev)

- Uses JWT-based authentication
- More permissive CORS settings
- Debug logging enabled

### Production Profile (prod)

- Uses OAuth2/Keycloak authentication
- Strict CORS configuration
- Production-ready security settings

## Token Usage

Include the JWT token in the Authorization header for protected endpoints:

```javascript
// JavaScript/Frontend usage
const token = localStorage.getItem("authToken");
const response = await fetch("/api/protected-endpoint", {
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
});
```

## Role-Based Access Control

- **STUDENT**: Basic user access
- **FACULTY**: Faculty-specific endpoints
- **MANAGER**: Management-level access
- **ADMIN**: Full system access

Access is controlled using `@PreAuthorize` annotations:

```kotlin
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin-only")
fun adminOnlyEndpoint() { ... }
```

## Configuration

JWT settings can be configured in `application.properties`:

```properties
# JWT Configuration
jwt.secret=myVerySecureSecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Error Handling

The system returns consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:

- `200`: Success
- `400`: Bad Request (validation errors, invalid credentials)
- `401`: Unauthorized (missing/invalid token)
- `403`: Forbidden (insufficient permissions)
- `500`: Internal Server Error
