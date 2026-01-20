#!/bin/bash
set -e

ARCHETYPE_DIR="archetype"

# Funzione per generare .env se non esiste
generate_env_file() {
    local project_dir=$(basename "$PWD")
    cat > .env << 'EOF'
# Configurazione Progetto Spring Boot
# Generato automaticamente da install.sh

# ========================================
# Configurazione Comune
# ========================================
PROJECT_NAME=PROJECT_DIR_PLACEHOLDER
GROUP_ID=com.example
ARTIFACT_VERSION=0.0.1-SNAPSHOT
JAVA_VERSION=21
SPRING_BOOT_VERSION=3.5.0

# Database SQLite
DB_DIR=data
SQLITE_VERSION=3.47.1.0

# ========================================
# Configurazione Sviluppo (DEV_)
# ========================================
DEV_PORT=8080
DEV_PORT_HOST=2310
DEV_NETWORK_SUFFIX=-dev
DEV_MAVEN_IMAGE=maven:3-eclipse-temurin-21

# Vite/Svelte GUI
VITE_PORT=2350

# Debug
DEBUG_PORT=5005

# ========================================
# Configurazione Release (REL_)
# ========================================
REL_PORT=8080
REL_JRE_IMAGE=eclipse-temurin:21-jre

# ========================================
# Git (per cmd git push/pull/sync)
# ========================================
GIT_USER=
GIT_EMAIL=
GIT_TOKEN=
EOF
    sed -i "s|PROJECT_DIR_PLACEHOLDER|$project_dir|g" .env
    echo "File .env generato con configurazione di default"
}

# Genera o carica .env
if [ ! -f .env ]; then
    echo "File .env non trovato, genero configurazione di default..."
    generate_env_file
    echo ""
    echo "=========================================="
    echo "PRIMA ESECUZIONE - CONFIGURAZIONE RICHIESTA"
    echo "=========================================="
    echo ""
    echo "Il file .env è stato generato con i valori di default."
    echo "Modifica il file .env per personalizzare:"
    echo "  - PROJECT_NAME: nome del progetto (default: nome cartella)"
    echo "  - GROUP_ID: groupId Maven (default: com.example)"
    echo "  - DEV_PORT_HOST: porta per l'ambiente di sviluppo (default: 2310)"
    echo "  - Altri parametri di configurazione"
    echo ""
    echo "Dopo aver modificato il file .env, riesegui:"
    echo "  ./install.sh"
    echo ""
    exit 0
fi

# Carica variabili da .env
set -a
source .env
set +a

echo "Configurazione caricata da .env"

# Variabili derivate per sviluppo
DEV_NETWORK="$PROJECT_NAME$DEV_NETWORK_SUFFIX"
DEV_CONTAINER="$PROJECT_NAME-dev"

# Step 1: Crea e avvia il container
if [ "$1" != "--dev" ]; then
    # Verifica se il container esiste già
    if docker ps -a --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
        echo "Container '$DEV_CONTAINER' già esistente."
        if ! docker ps --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
            echo "Avvio container..."
            docker start "$DEV_CONTAINER"
        else
            echo "Container già in esecuzione."
        fi
    else
        # Crea la rete se non esiste
        if ! docker network ls --format '{{.Name}}' | grep -q "^$DEV_NETWORK$"; then
            docker network create "$DEV_NETWORK"
            echo "Docker network '$DEV_NETWORK' creata."
        fi

        # Avvia container di sviluppo
        docker run -it -d \
            --name "$DEV_CONTAINER" \
            -v "$PWD":/usr/src/app \
            -w /usr/src/app \
            -p $DEV_PORT_HOST:$DEV_PORT \
            -p $VITE_PORT:$VITE_PORT \
            -p $DEBUG_PORT:$DEBUG_PORT \
            --network "$DEV_NETWORK" \
            $DEV_MAVEN_IMAGE \
            tail -f /dev/null

        echo "Container '$DEV_CONTAINER' creato e avviato."
    fi

    echo ""
    echo "Esegui './install.sh --dev' per generare l'applicazione Spring Boot dall'archetipo."
    exit 0
fi

# Step 2: Genera l'applicazione dall'archetipo nel container
echo "Verifica stato container..."
if ! docker ps --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
    echo "Errore: container '$DEV_CONTAINER' non in esecuzione."
    echo "Esegui prima './install.sh' per avviare il container."
    exit 1
fi

if [ -f pom.xml ]; then
    echo "Applicazione già generata (pom.xml esistente)."
    exit 0
fi

echo "Installazione dipendenze nel container..."
docker exec "$DEV_CONTAINER" bash -c "apt-get update -qq && apt-get install -y -qq sqlite3 git > /dev/null 2>&1"

echo "Installazione archetipo nel repository Maven locale..."
docker exec "$DEV_CONTAINER" mvn -f "$ARCHETYPE_DIR/pom.xml" clean install -q

echo "Generazione applicazione Spring Boot dall'archetipo..."
docker exec "$DEV_CONTAINER" bash -c "
    mvn archetype:generate \
        -DarchetypeGroupId=dev.springtools \
        -DarchetypeArtifactId=spring-svelte-archetype \
        -DarchetypeVersion=1.0.0 \
        -DgroupId=$GROUP_ID \
        -DartifactId=$PROJECT_NAME \
        -Dversion=$ARTIFACT_VERSION \
        -Dpackage=$GROUP_ID \
        -DinteractiveMode=false \
        -B

    # Sposta i file generati dalla sottocartella alla root
    shopt -s dotglob
    mv $PROJECT_NAME/* . 2>/dev/null || true
    rmdir $PROJECT_NAME 2>/dev/null || true
"

echo "Installazione Node.js nel container..."
if ! docker exec "$DEV_CONTAINER" bash -c "command -v node >/dev/null 2>&1"; then
    docker exec "$DEV_CONTAINER" bash -c "
        curl -fsSL https://deb.nodesource.com/setup_20.x | bash - >/dev/null 2>&1
        apt-get install -y nodejs >/dev/null 2>&1
    "
    echo "Node.js installato: $(docker exec "$DEV_CONTAINER" node --version)"
fi

echo "Installazione dipendenze Svelte..."
docker exec "$DEV_CONTAINER" bash -c "cd gui && npm install >/dev/null 2>&1"

echo ""
echo "=========================================="
echo "Applicazione Spring Boot + Svelte generata!"
echo "=========================================="
echo ""
echo "Spring Boot Backend:"
echo "  - Endpoint REST: http://localhost:$DEV_PORT_HOST/api/hello"
echo "  - Pagina HTML:   http://localhost:$DEV_PORT_HOST/"
echo ""
echo "Database SQLite:"
echo "  - Percorso: $DB_DIR/$PROJECT_NAME.db"
echo "  - Driver: sqlite-jdbc $SQLITE_VERSION"
echo ""
echo "Flyway Migrations:"
echo "  - Migration: V1__init_database.sql"
echo "  - Le migration vengono eseguite automaticamente all'avvio"
echo ""
echo "Svelte Frontend (gui/):"
echo "  - Dev server: http://localhost:$VITE_PORT/"
echo "  - Build output: src/main/resources/static/"
echo ""
echo "Comandi utili:"
echo "  - Avvia Spring Boot: docker exec -it $DEV_CONTAINER mvn spring-boot:run"
echo "  - Avvia Vite dev:    docker exec -it $DEV_CONTAINER bash -c 'cd gui && npm run dev'"
echo "  - Build Svelte:      docker exec -it $DEV_CONTAINER bash -c 'cd gui && npm run build'"
echo "  - Shell container:   docker exec -it $DEV_CONTAINER bash"
echo ""
echo "Debug (VSCode):"
echo "  - Debug port: $DEBUG_PORT"
echo ""
echo "NOTA: All'avvio Flyway eseguirà le migration e creerà il database"
echo ""
