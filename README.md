# Spring Tools

Maven archetype e installer per progetti Spring Boot + Svelte/Vite con SQLite.

## Contenuto

```
springtools/
├── archetype/              # Maven archetype
│   ├── pom.xml
│   └── src/main/resources/
│       ├── META-INF/maven/
│       │   └── archetype-metadata.xml
│       └── archetype-resources/
│           ├── pom.xml
│           ├── .env
│           ├── src/
│           ├── gui/
│           └── bin/
├── install.sh              # Installer automatico
└── README.md
```

## Quick Start

### 1. Clona il repository con il nome del tuo progetto

```bash
git clone <repository-url> myproject
cd myproject
```

### 2. Prima esecuzione - Genera configurazione

```bash
./install.sh
```

Questo crea il file `.env` con la configurazione di default.

### 3. Personalizza la configurazione

Modifica il file `.env`:

```bash
nano .env
```

Parametri principali:
- `PROJECT_NAME`: Nome del progetto (default: nome cartella)
- `GROUP_ID`: GroupId Maven (default: dev.<nome_cartella>)
- `DEV_PORT_HOST`: Porta host per sviluppo (default: 2310)
- `VITE_PORT`: Porta Vite dev server (default: 2350)
- `DEBUG_PORT`: Porta debug Java (default: 5005)

### 4. Seconda esecuzione - Crea container Docker

```bash
./install.sh
```

Questo:
- Crea una Docker network
- Avvia un container Maven con JDK 21
- Mappa la cartella corrente come volume `/usr/src/app`
- Espone le porte configurate

### 5. Terza esecuzione - Genera progetto dall'archetipo

```bash
./install.sh --dev
```

Questo:
- Installa l'archetipo nel repository Maven locale
- Genera il progetto Spring Boot dall'archetipo usando i parametri da `.env`
- Installa Node.js nel container
- Installa le dipendenze Svelte/Vite

## Struttura Progetto Generato

```
myproject/
├── pom.xml                      # Maven configuration
├── .env                         # Environment variables
├── .gitignore
├── README.md
├── mvnw, mvnw.cmd, .mvn/       # Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Application.java
│   │   │   ├── HelloController.java
│   │   │   └── utils/          # 7 utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/   # Flyway migrations
│   │       └── static/         # Static files
│   └── test/
├── gui/                        # Svelte/Vite frontend
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── bin/                        # Utility scripts
├── data/                       # SQLite database directory
├── logs/                       # Application logs directory
├── bin/release                 # Release script for production
└── archetype/                  # Archetipo originale (puoi rimuoverlo)
```

## Comandi Utili

### Backend Spring Boot

```bash
# Avvia applicazione
docker exec -it myproject-dev mvn spring-boot:run

# Accedi: http://localhost:2310
# API:    http://localhost:2310/api/hello
```

### Frontend Svelte

```bash
# Dev server (con hot reload)
docker exec -it myproject-dev bash -c 'cd gui && npm run dev'
# Accedi: http://localhost:2350

# Build per produzione (output in src/main/resources/static/)
docker exec -it myproject-dev bash -c 'cd gui && npm run build'
```

### Shell nel container

```bash
docker exec -it myproject-dev bash
```

### Debug

Configurazione debug su porta 5005 (configurabile in `.env`).

## Stack Tecnologico

- **Backend**: Spring Boot 3.5.0, Java 21
- **Database**: SQLite 3.47.1.0 con Flyway migrations
- **Frontend**: Svelte + Vite
- **Utility**: 7 classi helper (JSON, HTTP, DB, DateTime, File, Env)
- **Container**: Maven 3 con Eclipse Temurin 21
- **Release**: Script `bin/release` per build e deploy in produzione

## Evolvere l'Archetipo

Se modifichi l'archetipo in `archetype/`:

```bash
# Reinstalla l'archetipo
docker exec -it myproject-dev mvn -f archetype/pom.xml clean install

# Genera un nuovo progetto di test
docker exec -it myproject-dev mvn archetype:generate \
  -DarchetypeGroupId=dev.springtools \
  -DarchetypeArtifactId=spring-svelte-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.test \
  -DartifactId=testproject \
  -Dversion=1.0.0-SNAPSHOT \
  -DinteractiveMode=false
```

## Pulizia

```bash
# Ferma e rimuovi il container
docker stop myproject-dev
docker rm myproject-dev

# Rimuovi la network
docker network rm myproject-dev
```

## Licenza

MIT
