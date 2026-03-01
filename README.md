# Auth Microservices + Angular UI

This workspace contains a microservices-based auth setup with Spring Boot and Angular.

## Tech Stack

- Spring Boot 3.3.5
- Spring Data JPA
- Spring Web
- Lombok
- H2 Database
- Eureka Server + Eureka Client
- Spring Cloud Gateway
- Angular (standalone routing)

## Services

- `backend/service-registry` (Eureka): `http://localhost:8761`
- `backend/auth-service` (Auth API): `http://localhost:8081`
- `backend/api-gateway` (Gateway): `http://localhost:8080`
- `frontend` (Angular UI): `http://localhost:4200`

## Features Implemented

- Customer registration with validations
- Login for customer and hardcoded officer
- Specific validation/error messages for invalid input
- Registration acknowledgment page with success message in green and attributes:
  - Random customer username
  - Customer name
  - Email
- Role-based home page menus
- Logout clears browser session and redirects to login

## Hardcoded Officer Login

- User ID: `officer01`
- Password: `Officer@123`

## Run Instructions

Open 4 terminals from workspace root (`c:\Users\HARSH\Desktop\SpringBoot\Auth`):

1. Start Eureka:

```powershell
Set-Location .\backend\service-registry
mvn spring-boot:run
```

1. Start Auth Service:

```powershell
Set-Location .\backend\auth-service
mvn spring-boot:run
```

1. Start API Gateway:

```powershell
Set-Location .\backend\api-gateway
mvn spring-boot:run
```

1. Start Angular UI:

```powershell
Set-Location .\frontend
npm install
npm start
```

## API Endpoints (via Gateway)

- `POST http://localhost:8080/auth/register`
- `POST http://localhost:8080/auth/login`

## Validation Rules Covered

- User ID: min 5, max 20
- Password: max 30, must include uppercase, lowercase, special character
- Customer Name: max 50
- Mobile: country code + 10 digit mobile number
- Confirm Password must match Password



