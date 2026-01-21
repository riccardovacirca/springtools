# ${artifactId}

Spring Boot application with Svelte/Vite frontend and SQLite database.

## Prerequisites

- Java 21
- Maven 3.x
- Node.js 20.x (for Svelte GUI)

## Structure

- `src/` - Spring Boot application
- `gui/` - Svelte/Vite frontend
- `bin/` - Utility scripts
- `data/` - SQLite database (auto-created)
- `logs/` - Application logs
- `bin/release` - Release script (Docker container for production)

## Run

### Backend (Spring Boot)
```bash
./mvnw spring-boot:run
```

Access: http://localhost:8080

### Frontend (Svelte Dev)
```bash
cd gui
npm install
npm run dev
```

Access: http://localhost:2350

### Build Frontend for Production
```bash
cd gui
npm run build
```

This builds the Svelte app into `src/main/resources/static/`

## API Endpoints

- `GET /api/hello` - Demo endpoint with database query

## Database

- SQLite database in `data/${artifactId}.db`
- Flyway migrations in `src/main/resources/db/migration/`

## Configuration

Edit `.env` file to customize ports and settings.
