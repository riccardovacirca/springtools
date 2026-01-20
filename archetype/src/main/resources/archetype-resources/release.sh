#!/bin/bash
set -e

TEMPLATE_DIR="template/release"

# Funzione per sostituire i placeholder in un file
replace_placeholders() {
    local input="$1"
    local output="$2"

    sed -e "s|{{PROJECT_NAME}}|$PROJECT_NAME|g" \
        -e "s|{{GROUP_ID}}|$GROUP_ID|g" \
        -e "s|{{ARTIFACT_VERSION}}|$ARTIFACT_VERSION|g" \
        -e "s|{{JAVA_VERSION}}|$JAVA_VERSION|g" \
        -e "s|{{SPRING_BOOT_VERSION}}|$SPRING_BOOT_VERSION|g" \
        -e "s|{{DB_DIR}}|$DB_DIR|g" \
        -e "s|{{SQLITE_VERSION}}|$SQLITE_VERSION|g" \
        -e "s|{{DEV_PORT}}|$DEV_PORT|g" \
        -e "s|{{DEV_NETWORK_SUFFIX}}|$DEV_NETWORK_SUFFIX|g" \
        -e "s|{{DEV_MAVEN_IMAGE}}|$DEV_MAVEN_IMAGE|g" \
        -e "s|{{REL_PORT}}|$REL_PORT|g" \
        -e "s|{{REL_JRE_IMAGE}}|$REL_JRE_IMAGE|g" \
        -e "s|{{JAR_FILE}}|$JAR_FILE|g" \
        "$input" > "$output"
}

# Verifica esistenza .env
if [ ! -f .env ]; then
    echo "Errore: file .env non trovato"
    echo "Esegui prima './install.sh' per generare la configurazione"
    exit 1
fi

# Carica variabili da .env
set -a
source .env
set +a

echo "Configurazione caricata da .env"

# Variabili derivate
DEV_CONTAINER="$PROJECT_NAME-dev"
REL_CONTAINER_PREFIX="$PROJECT_NAME-release"

# Parse arguments
VERSION=""
DIST_MODE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--vers)
            VERSION="$2"
            shift 2
            ;;
        -d|--dist)
            DIST_MODE=true
            shift
            ;;
        *)
            echo "Opzione sconosciuta: $1"
            echo "Uso: $0 -v|--vers <versione> [-d|--dist]"
            echo ""
            echo "Step 1 - Build e test:"
            echo "  $0 -v 1.0.0"
            echo "  Compila il progetto e crea un container di rilascio per test"
            echo ""
            echo "Step 2 - Distribuzione:"
            echo "  $0 -v 1.0.0 -d"
            echo "  Genera il file tar dell'immagine per la distribuzione"
            exit 1
            ;;
    esac
done

# Verifica che la versione sia stata specificata
if [ -z "$VERSION" ]; then
    echo "Errore: versione non specificata"
    echo "Uso: $0 -v|--vers <versione> [-d|--dist]"
    exit 1
fi

IMAGE_NAME="$PROJECT_NAME:v$VERSION"
REL_CONTAINER="$REL_CONTAINER_PREFIX-v$VERSION"
TAR_FILE="$PROJECT_NAME-v$VERSION.tar"
JAR_FILE="target/$PROJECT_NAME-$ARTIFACT_VERSION.jar"
DIST_DIR="dist"
DIST_JAR="$DIST_DIR/$PROJECT_NAME-v$VERSION.jar"
DIST_TAR="$DIST_DIR/$PROJECT_NAME-v$VERSION.tar"

# Crea directory dist se non esiste
mkdir -p "$DIST_DIR"

# STEP 2: Genera distribuzione tar
if [ "$DIST_MODE" = true ]; then
    echo "=========================================="
    echo "Step 2: Generazione distribuzione"
    echo "=========================================="

    # Verifica che l'immagine esista
    if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^$IMAGE_NAME$"; then
        echo "Errore: immagine '$IMAGE_NAME' non trovata"
        echo "Esegui prima: $0 -v $VERSION"
        exit 1
    fi

    # Verifica che il JAR esista in dist
    if [ ! -f "$DIST_JAR" ]; then
        echo "Errore: JAR non trovato in $DIST_JAR"
        echo "Il JAR viene generato durante lo step 1 (build)"
        exit 1
    fi

    # Esporta in tar
    echo "Esportazione immagine in $DIST_TAR..."
    docker save "$IMAGE_NAME" -o "$DIST_TAR"

    echo ""
    echo "=========================================="
    echo "Distribuzione generata!"
    echo "=========================================="
    echo "JAR: $DIST_JAR ($(du -h "$DIST_JAR" | cut -f1))"
    echo "TAR: $DIST_TAR ($(du -h "$DIST_TAR" | cut -f1))"
    echo ""
    echo "Contenuto cartella dist:"
    ls -lh "$DIST_DIR" | grep -E "^-|^d" | awk '{print "  " $9 " (" $5 ")"}'
    echo ""
    echo "Per caricare l'immagine: docker load -i $DIST_TAR"
    exit 0
fi

# STEP 1: Build e container di rilascio
echo "=========================================="
echo "Step 1: Build e test release"
echo "=========================================="

# Verifica che il container di sviluppo sia in esecuzione
if ! docker ps --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
    echo "Errore: container di sviluppo '$DEV_CONTAINER' non in esecuzione"
    echo "Avvia con: docker start $DEV_CONTAINER"
    exit 1
fi

# Build applicazione Svelte
if [ -d "gui" ] && [ -f "gui/package.json" ]; then
    echo "Build applicazione Svelte..."
    docker exec "$DEV_CONTAINER" bash -c "cd gui && npm run build"
    echo "Build Svelte completata (output in src/main/resources/static/)"
else
    echo "Cartella gui/ non trovata, skip build Svelte"
fi

# Compila il jar nel container di sviluppo
echo "Compilazione progetto Spring Boot..."
docker exec "$DEV_CONTAINER" mvn clean package -DskipTests

# Verifica che il jar esista
if [ ! -f "$JAR_FILE" ]; then
    echo "Errore: jar non trovato in $JAR_FILE"
    exit 1
fi

echo "JAR compilato: $JAR_FILE ($(du -h "$JAR_FILE" | cut -f1))"

# Copia JAR nella cartella dist
echo "Copia JAR in $DIST_JAR..."
cp "$JAR_FILE" "$DIST_JAR"

# Crea Dockerfile per il container di rilascio
replace_placeholders "$TEMPLATE_DIR/Dockerfile.release.template" Dockerfile.release

# Costruisce immagine di rilascio
echo "Creazione immagine di rilascio: $IMAGE_NAME..."
docker build -f Dockerfile.release -t "$IMAGE_NAME" .

# Pulizia file temporanei
rm Dockerfile.release

# Ferma e rimuove eventuale container di rilascio precedente
if docker ps -a --format '{{.Names}}' | grep -q "^$REL_CONTAINER$"; then
    echo "Rimozione container di rilascio precedente..."
    docker stop "$REL_CONTAINER" 2>/dev/null || true
    docker rm "$REL_CONTAINER" 2>/dev/null || true
fi

# Crea e avvia il container di rilascio
echo "Avvio container di rilascio: $REL_CONTAINER..."
docker run -d \
    --name "$REL_CONTAINER" \
    -p $REL_PORT:$REL_PORT \
    "$IMAGE_NAME"

# Attende che l'applicazione sia pronta
echo "Attendo avvio applicazione..."
sleep 5

# Verifica che il container sia in esecuzione
if ! docker ps --format '{{.Names}}' | grep -q "^$REL_CONTAINER$"; then
    echo ""
    echo "Errore: il container non è in esecuzione"
    echo "Log del container:"
    docker logs "$REL_CONTAINER"
    exit 1
fi

# Test endpoint
echo "Test endpoint..."
sleep 5
if curl -s -f "http://localhost:$REL_PORT/api/hello" > /dev/null; then
    RESPONSE=$(curl -s "http://localhost:$REL_PORT/api/hello")
    echo "✓ Endpoint funzionante!"
    echo "Risposta: $RESPONSE"
else
    echo "⚠ Endpoint non raggiungibile (l'app potrebbe essere ancora in avvio)"
fi

echo ""
echo "=========================================="
echo "Release v$VERSION pronta per test!"
echo "=========================================="
echo "Container: $REL_CONTAINER"
echo "Immagine: $IMAGE_NAME"
echo "Porta: $REL_PORT"
echo ""
echo "Artifacts salvati:"
echo "  - JAR: $DIST_JAR ($(du -h "$DIST_JAR" | cut -f1))"
echo ""
echo "Database e Migration:"
echo "  - Il database SQLite viene creato automaticamente al primo avvio"
echo "  - Le migration Flyway vengono eseguite automaticamente all'avvio"
echo ""
echo "Test URL:"
echo "  - API: http://localhost:$REL_PORT/api/hello"
echo "  - Web: http://localhost:$REL_PORT/"
echo ""
echo "Comandi utili:"
echo "  - Log: docker logs -f $REL_CONTAINER"
echo "  - Stop: docker stop $REL_CONTAINER"
echo "  - Start: docker start $REL_CONTAINER"
echo "  - Remove: docker rm -f $REL_CONTAINER"
echo "  - Shell: docker exec -it $REL_CONTAINER bash"
echo ""
echo "Genera distribuzione: $0 -v $VERSION -d"
echo "=========================================="
