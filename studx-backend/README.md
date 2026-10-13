# 🎓 STUDx — Spring Boot Backend (Java 21 + Spring Boot 3.x + MySQL 8)

This directory contains the migrated Java Spring Boot backend for the **STUDx** campus marketplace, replacing the legacy Node.js/MongoDB backend while fully integrating with the existing React frontend.

---

## 🚀 Tech Stack

- **Java Version:** 21 (tested & compatible with Java 21 to 26)
- **Framework:** Spring Boot 3.4.3
- **Database:** MySQL 8.0
- **Database Migrations:** Flyway (`V1__init_schema.sql`, `V2__seed_initial_data.sql`)
- **ORM:** Spring Data JPA + Hibernate
- **Security:** Spring Security + JWT (HMAC-SHA256 with JJWT 0.12.6) + BCrypt
- **API Documentation:** Springdoc OpenAPI 3 / Swagger UI
- **Testing:** JUnit 5, Mockito, Spring Boot Integration Tests with H2 in-memory DB

---

## 📋 Prerequisites

1. **Java JDK 21+**: Installed and available on `PATH` (`java -version`).
2. **Apache Maven 3.8+**: Installed and available on `PATH` (`mvn -version`).
3. **MySQL 8.0**: Service running on `localhost:3306`.
4. **Node.js & npm**: (For running the React frontend) Node 18+ and npm 9+.

---

## ⚙️ Configuration & Environment Variables

The backend uses sensible defaults in `src/main/resources/application.yml` and can be customized using environment variables or JVM system properties:

| Variable | Default Value | Description |
|---|---|---|
| `PORT` | `8080` | HTTP port for Spring Boot application |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `studx_db` | MySQL database name (`createDatabaseIfNotExist=true`) |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `JWT_SECRET` | *(Default 256-bit secure key)* | Secret key for JWT signing & verification |
| `UPLOAD_DIR` | `./uploads` | Directory to store uploaded listing images |

---

## 🛠️ Step-by-Step Run Instructions (Windows PowerShell)

### 1. Configure MySQL Credentials
Ensure your local MySQL service is running. If your MySQL password is not `root`, set your credentials in PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YourMySQLPassword"
```

### 2. Build and Run the Spring Boot Backend

From the repository root:

```powershell
cd server-spring
mvn spring-boot:run
```

Or run the packaged JAR:

```powershell
java -jar target/studx-backend-1.0.0-SNAPSHOT.jar
```

The server will start at `http://localhost:8080`.
Flyway will automatically create all tables and seed categories, default sellers, and products.

### 3. Run the React Frontend

Open a second terminal window:

```powershell
cd client
npm install
npm run dev
```

The frontend will start at `http://localhost:5173`.
Vite is preconfigured with a proxy that seamlessly routes all `/api` and `/uploads` requests to the Spring Boot backend at `http://localhost:8080`.

---

## 📖 Interactive API Documentation (Swagger UI)

Once the backend is running, open your browser to:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

You can explore and test all endpoints directly from the browser. For protected endpoints, click the **Authorize** button at the top and paste your JWT token.

---

## 🧪 Running Automated Tests

All tests run against an isolated in-memory H2 database, requiring no MySQL connection:

```powershell
cd server-spring
mvn test
```

Test suite coverage includes:
- **`AuthServiceTest`**: College email domain verification, duplicate checks, password validation.
- **`ProductServiceTest`**: Product creation, feature parsing, concurrency-safe reservation, ownership validation.
- **`OfferServiceTest`**: Buyer offer submission, seller offer acceptance with pessimistic write lock, auto-rejection of competing offers.
- **`FairPriceServiceTest`**: Campus market pricing aggregates and condition multipliers.
- **`ProductControllerIntegrationTest`**: End-to-end `MockMvc` integration tests for categories, authentication flow, and product publishing.

---

## 🔐 Default Seed Accounts

The initial Flyway migration seeds the following demo accounts (Password for all: `Password@123`):

| Role | Name | Email | Password | College / Verification |
|---|---|---|---|---|
| User | Ankit Sharma | `ankit@delhi.edu.in` | `Password@123` | Delhi University (Verified) |
| User | Priya Verma | `priya@mumbai.ac.in` | `Password@123` | Mumbai University (Verified) |
| Admin | Admin User | `admin@studx.edu` | `Password@123` | Campus Hub (Admin, Verified) |
