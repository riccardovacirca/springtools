# ${artifactId}

Spring Boot + Svelte/Vite + SQLite

## Deploy da Git

```bash
# 1. Clona springtools e installa
git clone https://github.com/riccardovacirca/springtools.git ${artifactId}
cd ${artifactId}
./install.sh && ./install.sh --dev

# 2. Configura git e sovrascrivi con progetto reale
git init
git remote add origin https://github.com/YOUR_USERNAME/${artifactId}.git
git fetch origin main && git reset --hard origin/main

# 3. Configura e avvia
cp .env.example .env
# Modifica .env con i tuoi secrets
docker exec -it ${artifactId}-dev cmd run
```

- Backend: http://localhost:8080
- Frontend: http://localhost:2350

## Sviluppo

```bash
docker exec -it ${artifactId}-dev bash
cmd run       # Avvia dev
cmd build     # Build completo
cmd test      # Test
cmd release   # Container produzione
```

## Struttura

- `src/` - Spring Boot
- `gui/` - Svelte frontend
- `bin/` - Script utility
- `.toolchain/` - Springtools clonato
- `.env.example` - Template configurazione (copia in `.env`)

## API

- `GET /api/hello` - Demo endpoint
- `GET /api/status/health` - Health check
- `POST /api/status/log` - Log message
- `GET /api/status/logs` - Retrieve logs

## Database

SQLite in `data/${artifactId}.db` (migrations in `src/main/resources/db/migration/`)

Opzionale: MariaDB/PostgreSQL via `./install.sh --mariadb` o `--postgres`
