# Smart Fleet Tracking System - Backend Setup Guide

## Overview
Enterprise-grade IoT Fleet Management System built with Spring Boot 3, PostgreSQL, and JWT authentication. Includes real-time GPS tracking, email notifications, WebSocket support, and role-based access control.

## Prerequisites
- **Java 17+** (Download from [Eclipse Adoptium](https://adoptium.net/))
- **Maven 3.8+** (Included with most Java IDEs)
- **PostgreSQL 14+** (Download from [postgresql.org](https://www.postgresql.org/))
- **Git** (for version control)

## Database Setup

### 1. Create PostgreSQL Database
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE fleetdb;

-- Connect to the database
\c fleetdb

-- Create tables
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE driver (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    license VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vehicle (
    id SERIAL PRIMARY KEY,
    plate VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trip (
    id SERIAL PRIMARY KEY,
    driver_id INTEGER REFERENCES driver(id),
    vehicle_id INTEGER REFERENCES vehicle(id),
    start_location VARCHAR(255),
    end_location VARCHAR(255),
    start_lat DOUBLE PRECISION,
    start_lng DOUBLE PRECISION,
    end_lat DOUBLE PRECISION,
    end_lng DOUBLE PRECISION,
    km DOUBLE PRECISION,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

CREATE TABLE fuel_prediction (
    id SERIAL PRIMARY KEY,
    trip_id INTEGER REFERENCES trip(id),
    predicted_liters DOUBLE PRECISION,
    model_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Create Demo Admin User (Optional)
```sql
-- Insert admin user (password: admin123)
INSERT INTO users (username, password, email, role, status)
VALUES ('admin', '$2a$10$YK5Rl7W.Yr.LDW8pq.h.z.i1L4kAGDGCIKNVvyW0PvWNpVvNNrFa2', 'admin@smartfleet.com', 'ROLE_ADMIN', 'ACTIVE');
```

## Configuration

### 1. Update application.properties
Edit `src/main/resources/application.properties`:

```properties
# Database (Already configured)
spring.datasource.url=jdbc:postgresql://localhost:5432/fleetdb
spring.datasource.username=postgres
spring.datasource.password=Utcn

# Email Configuration - IMPORTANT!
# 1. Enable 2-Step Verification: https://myaccount.google.com/security
# 2. Generate App Password: https://myaccount.google.com/apppasswords
# 3. Update these values:
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
app.fleet.manager.email=manager@smartfleet.com
```

## Running the Backend

### Option 1: Using IDE (IntelliJ IDEA or VS Code)
1. Open the project in your IDE
2. Right-click on `SmartFleetTrackingApplication.java`
3. Click "Run" or press `Shift + F10`
4. Server starts on http://localhost:8080

### Option 2: Using Maven Command
```bash
cd backend
mvn spring-boot:run
```

### Option 3: Build and Run JAR
```bash
cd backend
mvn clean package
java -jar target/smart-fleet-tracking-2.0.0.jar
```

## API Endpoints

### Authentication
- **POST** `/api/auth/login` - User login
- **POST** `/api/auth/register` - Driver registration
- **GET** `/api/auth/health` - Health check

### Vehicles (16 endpoints)
- **GET** `/api/vehicles` - Get all vehicles
- **GET** `/api/vehicles/{id}` - Get vehicle by ID
- **POST** `/api/vehicles` - Create vehicle (ADMIN only)
- **PUT** `/api/vehicles/{id}` - Update vehicle (ADMIN only)
- **DELETE** `/api/vehicles/{id}` - Delete vehicle (ADMIN only)
- **GET** `/api/vehicles/status/{status}` - Filter by status
- **GET** `/api/vehicles/type/{type}` - Filter by type
- **GET** `/api/vehicles/brand/{brand}` - Filter by brand
- **GET** `/api/vehicles/search?plate=...` - Search by plate
- **PUT** `/api/vehicles/{id}/location` - Update GPS location

### Drivers
- **GET** `/api/drivers` - Get all drivers
- **GET** `/api/drivers/{id}` - Get driver by ID
- **GET** `/api/drivers/status/{status}` - Filter by status
- **GET** `/api/drivers/user/{userId}` - Get driver by user
- **PUT** `/api/drivers/{id}/status` - Update driver status

### Trips
- **GET** `/api/trips` - Get all trips
- **GET** `/api/trips/{id}` - Get trip by ID
- **GET** `/api/trips/driver/{driverId}` - Get driver trips
- **GET** `/api/trips/vehicle/{vehicleId}` - Get vehicle trips

### Fuel Analytics
- **GET** `/api/fuel-predictions` - Get all predictions
- **GET** `/api/fuel-predictions/{id}` - Get prediction by ID
- **GET** `/api/fuel-predictions/trip/{tripId}` - Get trip predictions

## WebSocket Endpoints
- **WS** `/ws` - STOMP WebSocket connection
- **Topic**: `/topic/vehicles` - Real-time vehicle updates

## Key Features

### 1. JWT Authentication
- Tokens valid for 24 hours
- Secure password hashing with BCrypt
- Role-based access control (ADMIN, DRIVER)

### 2. Email Notifications
- Welcome email on driver registration
- Login alerts to fleet manager
- Async processing with @EnableAsync

### 3. Vehicle Management
- Brand field required for all vehicle operations
- Real-time GPS location tracking
- Status monitoring (ACTIVE, IDLE, MAINTENANCE)

### 4. Security
- CORS enabled for frontend integration
- JWT filter on protected endpoints
- Method-level authorization

## Troubleshooting

### Connection Refused to PostgreSQL
```bash
# Check if PostgreSQL is running
psql -U postgres -d fleetdb

# If not running, start PostgreSQL
# Windows: Services > PostgreSQL
# macOS: brew services start postgresql
# Linux: sudo systemctl start postgresql
```

### Port 8080 Already in Use
```bash
# Change port in application.properties
server.port=8081
```

### Email Not Sending
1. Check Gmail 2-Step Verification is enabled
2. Generate new App Password
3. Update `spring.mail.password` in application.properties
4. Ensure `app.fleet.manager.email` is valid

### JWT Token Issues
- Regenerate token: Login again
- Check token expiration: 24 hours from creation
- Verify Authorization header: `Bearer <token>`

## Project Structure
```
backend/
├── pom.xml
├── src/main/java/com/smartfleet/
│   ├── SmartFleetTrackingApplication.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── VehicleController.java
│   │   ├── DriverController.java
│   │   ├── TripController.java
│   │   └── FuelPredictionController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── EmailService.java
│   │   ├── VehicleService.java
│   │   ├── DriverService.java
│   │   ├── TripService.java
│   │   └── FuelPredictionService.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Driver.java
│   │   ├── Vehicle.java
│   │   ├── Trip.java
│   │   └── FuelPrediction.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── DriverRepository.java
│   │   ├── VehicleRepository.java
│   │   ├── TripRepository.java
│   │   └── FuelPredictionRepository.java
│   ├── dto/
│   │   ├── LoginRequestDTO.java
│   │   ├── RegisterRequestDTO.java
│   │   ├── AuthResponseDTO.java
│   │   ├── VehicleCreateDTO.java
│   │   ├── VehicleUpdateDTO.java
│   │   ├── VehicleResponseDTO.java
│   │   ├── DriverResponseDTO.java
│   │   ├── TripResponseDTO.java
│   │   └── FuelPredictionDTO.java
│   └── security/
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       ├── SecurityConfig.java
│       └── WebSocketConfig.java
└── src/main/resources/
    └── application.properties
```

## Testing with Postman/Curl

### 1. Register as Driver
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_driver",
    "password": "password123",
    "email": "john@example.com",
    "fullName": "John Doe",
    "license": "B123456"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_driver",
    "password": "password123"
  }'
```

### 3. Get All Vehicles (with token)
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Create Vehicle (ADMIN only)
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{
    "plate": "AB-123-CD",
    "brand": "Volvo",
    "type": "Truck",
    "status": "ACTIVE"
  }'
```

## Docker Deployment (Optional)

### Create Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run
```bash
mvn clean package
docker build -t smart-fleet-backend .
docker run -p 8080:8080 smart-fleet-backend
```

## Performance Optimization

1. **Database Indexing**: Create indexes on frequently queried columns
```sql
CREATE INDEX idx_vehicle_plate ON vehicle(plate);
CREATE INDEX idx_driver_user_id ON driver(user_id);
CREATE INDEX idx_trip_driver_id ON trip(driver_id);
```

2. **Connection Pooling**: Configured in Spring Boot (10 connections by default)

3. **Caching**: Can be added using Spring Cache abstraction

## Security Notes

- Change JWT secret in production
- Use HTTPS in production
- Implement rate limiting
- Add API versioning
- Regular dependency updates

## Next Steps

1. Set up the frontend (see `frontend/README.md`)
2. Configure email notifications
3. Deploy to production
4. Monitor with logs and metrics

## Support

For issues or questions:
- Check application logs in console
- Review error messages in responses
- Verify database connectivity
- Ensure all dependencies are installed

---

**Version**: 2.0.0  
**Last Updated**: December 2024
