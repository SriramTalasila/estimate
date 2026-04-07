# Estimate

Estimate is a Spring Boot 4 backend for creating and managing business estimates or quotations. It supports user authentication with JWT cookies, business and catalog management, estimate creation, sales stats, and PDF export.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- springdoc OpenAPI / Swagger UI
- OpenHTMLtoPDF

## What The Project Does

This application is built around three main areas:

- Authentication: sign up, sign in, and sign out users with role-based access.
- Business management: create a business, define categories, and manage products.
- Estimate management: create estimates for customers, attach line items and notes, apply discounts, calculate totals, fetch sales stats, and generate PDFs.

## Roles

The application seeds these roles from `src/main/resources/import.sql`:

- `ROLE_BUSINESS_OWNER`
- `ROLE_EMPLOYEE`
- `ROLE_ADMIN`

In practice:

- Business owners can create and manage their business, categories, products, and estimates.
- Employees can access business data and work with estimates for the business they belong to.
- Admin exists in the model, but this codebase does not currently expose dedicated admin-only management endpoints.

## Project Structure

`src/main/java/com/talasila/estimate`

- `controller`: REST endpoints for auth, business management, and estimates.
- `dto`: request and response payload classes.
- `model`: JPA entities such as `User`, `Business`, `Category`, `Product`, `Customer`, and `Estimate`.
- `repository`: Spring Data repositories.
- `security`: JWT filter, auth entry point, token utilities, and security config.
- `service`: business logic, security ownership checks, OpenAPI config, and user details services.

`src/main/resources`

- `application.yaml`: database and JWT configuration.
- `import.sql`: inserts the default roles.
- `static/index.html`: bundled frontend page.
- `templates/pdf-Template.html`: HTML template used for PDF generation.

## Core Domain Model

- A `User` belongs to one `Business` and has one or more roles.
- A `Business` owns categories, products, customers, and estimates.
- A `Category` belongs to a business.
- A `Product` belongs to a category.
- An `Estimate` belongs to a business and a customer.
- An estimate contains:
  - `EstimateItem` rows
  - `EstimateNote` entries
  - `EstimateCategoryDiscount` records

## Main API Areas

### 1. Authentication

Base path: `/api/auth`

- `POST /signin`
- `POST /signup`
- `POST /signout`

Successful sign-in returns user info and sets an HTTP-only JWT cookie.

### 2. Business And Catalog Management

Base path: `/api/manage`

- Create and update a business
- Get current user’s business
- Create and list categories
- Create, list, update, and delete products

These endpoints use method-level security checks to ensure the current user owns or belongs to the target business.

### 3. Estimate Management

Base path: `/api`

- `POST /business/{businessId}/estimates`
- `GET /business/{businessId}/estimates`
- `GET /business/{businessId}/estimates/stats`
- `PUT /estimates/{estimateId}`
- `DELETE /estimates/{estimateId}`
- `GET /estimates/{estimateId}/pdf`

Estimate requests support:

- existing or new customer details
- line items
- estimate notes
- category-level discounts
- additional discount
- tax amount
- final amount

## Security Model

Security is configured in `WebSecurityConfig`.

- Public routes include `/`, `/index.html`, `/api/auth/**`, and Swagger endpoints.
- Other routes require authentication.
- Authorization is enforced with `@PreAuthorize`.
- JWT is stored in a cookie.
- CORS is enabled for a few explicit frontend origins.
- Session policy is stateless.

## API Documentation

Swagger / OpenAPI is enabled through `springdoc`.

Once the app is running, check:

- `/swagger-ui.html`
- `/swagger-ui/index.html`
- `/v3/api-docs`

## Database Configuration

Current configuration in `src/main/resources/application.yaml`:

- URL: `jdbc:postgresql://localhost:5432/estimate`
- Username: `estimate`
- Password: `Estimate@123`
- Hibernate DDL: `update`
- Schema: `estimate`

Before running the app, make sure:

- PostgreSQL is available locally
- a database named `estimate` exists
- the `estimate` schema exists or is created as part of setup
- the configured user has permission to access it

## Running The Project

### Prerequisites

- Java 21
- PostgreSQL
- Maven wrapper included in repo

### Start the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Or build first:

```powershell
.\mvnw.cmd clean package
java -jar target\estimate-0.0.1-SNAPSHOT.jar
```

## Important Implementation Notes

- `EstimateService` calculates `totalBeforeDiscount` from items, but `totalDiscount`, `taxAmount`, and `finalAmount` are currently accepted from the request payload rather than recomputed on the server.
- Customers are reused by phone number within a business. If no phone number is provided, the service creates a synthetic `N/A-<timestamp>` phone value for a walk-in customer.
- Sales stats are exposed as totals for today, this month, and this year.
- PDF generation is handled with an HTML template and OpenHTMLtoPDF.

## Current Gaps / Risks

- `README.md` was previously empty, so this document is inferred from the codebase.
- `HELP.md` is still the default Spring initializer help file.
- The configured OpenAPI cookie name (`jwt-cookie`) does not match `application.yaml` (`talasila-jwt`).
- The PDF template file is named `pdf-Template.html`, while the service loads `templates/pdf-template.html`; on case-sensitive filesystems this can fail.
- There is only one basic test class and no meaningful automated coverage yet.

## Suggested Next Documentation

If you want, the next useful documents to add would be:

- API reference with sample requests and responses
- database schema notes
- frontend usage guide for the bundled `index.html`
- deployment guide for production
