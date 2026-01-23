#!/bin/sh
set -e

ARCHETYPE_DIR="archetype"

# Funzione per generare .env se non esiste
generate_env_file() {
    project_dir=$(basename "$PWD")
    cat > .env << EOF
# Configurazione Progetto Spring Boot
# Generato automaticamente da install.sh

# ========================================
# Configurazione Comune
# ========================================
PROJECT_NAME=PROJECT_DIR_PLACEHOLDER
GROUP_ID=GROUP_ID_PLACEHOLDER
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

# ========================================
# Database Containers
# ========================================
# MariaDB
MARIADB_ENABLED=n
MARIADB_IMAGE=mariadb:latest
MARIADB_PORT=2330
MARIADB_ROOT_USER=root
MARIADB_ROOT_PASSWORD=root
MARIADB_NAME=appdb
MARIADB_USER=appuser
MARIADB_PASSWORD=apppass
MARIADB_JDBC_URL=jdbc:mariadb://PROJECT_DIR_PLACEHOLDER-mariadb:3306/appdb

# PostgreSQL
PGSQL_ENABLED=n
PGSQL_IMAGE=postgres:latest
PGSQL_PORT=2340
PGSQL_ROOT_USER=postgres
PGSQL_ROOT_PASSWORD=postgres
PGSQL_NAME=appdb
PGSQL_USER=appuser
PGSQL_PASSWORD=apppass
PGSQL_JDBC_URL=jdbc:postgresql://PROJECT_DIR_PLACEHOLDER-postgres:5432/appdb
EOF

    # sostituzioni compatibili sh
    sed "s|PROJECT_DIR_PLACEHOLDER|$project_dir|g" .env > .env.tmp && mv .env.tmp .env
    sed "s|GROUP_ID_PLACEHOLDER|dev.$project_dir|g" .env > .env.tmp && mv .env.tmp .env

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
    echo "  - GROUP_ID: groupId Maven (default: dev.<nome_cartella>)"
    echo "  - DEV_PORT_HOST: porta per l'ambiente di sviluppo (default: 2310)"
    echo "  - Altri parametri di configurazione"
    echo ""
    echo "Dopo aver modificato il file .env, riesegui:"
    echo "  ./install.sh"
    echo ""
    exit 0
fi

# Carica variabili da .env (sh compatibile)
. ./.env
echo "Configurazione caricata da .env"

# Variabili derivate per sviluppo
DEV_NETWORK="$PROJECT_NAME$DEV_NETWORK_SUFFIX"
DEV_CONTAINER="$PROJECT_NAME"

# Variabili derivate per database containers
MARIADB_CONTAINER="$PROJECT_NAME-mariadb"
MARIADB_VOLUME="$PROJECT_NAME-mariadb-data"
PGSQL_CONTAINER="$PROJECT_NAME-postgres"
PGSQL_VOLUME="$PROJECT_NAME-postgres-data"

# Creates MariaDB container
# - Network: ${DEV_NETWORK} (<project>-dev)
# - Container: ${MARIADB_CONTAINER} (<project>-mariadb)
# - Volume: ${MARIADB_VOLUME} (<project>-mariadb-data)
# - Port: ${MARIADB_PORT}:3306 (default 2330:3306)
# - Database: ${MARIADB_NAME}, User: ${MARIADB_USER}
# - Starts existing container if present
create_mariadb_container() {
    if ! docker network ls --format "{{.Name}}" | grep -q "^${DEV_NETWORK}$"; then
        docker network create "$DEV_NETWORK" >/dev/null 2>&1 || true
    fi

    if docker ps -a --format "{{.Names}}" | grep -q "^${MARIADB_CONTAINER}$"; then
        if docker ps --format "{{.Names}}" | grep -q "^${MARIADB_CONTAINER}$"; then
            echo "MariaDB container già in esecuzione."
            return 0
        fi
        docker start "$MARIADB_CONTAINER" >/dev/null 2>&1 || {
            echo "Errore nell'avvio del container MariaDB."
            return 1
        }
        echo "Container MariaDB avviato."
        return 0
    fi

    if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${MARIADB_IMAGE}$"; then
        echo "Download immagine MariaDB..."
        docker pull "$MARIADB_IMAGE" >/dev/null 2>&1
    fi

    echo "Creazione container MariaDB..."
    docker run -d --name "$MARIADB_CONTAINER" --network "$DEV_NETWORK" \
        -e MYSQL_ROOT_PASSWORD="$MARIADB_ROOT_PASSWORD" \
        -e MYSQL_DATABASE="$MARIADB_NAME" \
        -e MYSQL_USER="$MARIADB_USER" \
        -e MYSQL_PASSWORD="$MARIADB_PASSWORD" \
        -p "$MARIADB_PORT:3306" \
        -v "$MARIADB_VOLUME:/var/lib/mysql" \
        "$MARIADB_IMAGE" >/dev/null 2>&1 || {
            echo "Errore nella creazione del container MariaDB."
            return 1
        }

    echo "Container MariaDB creato e avviato."
    echo "  Host: localhost:$MARIADB_PORT"
    echo "  Database: $MARIADB_NAME"
    echo "  User: $MARIADB_USER"
    echo "  Password: $MARIADB_PASSWORD"
}

# Creates PostgreSQL container
# - Network: ${DEV_NETWORK} (<project>-dev)
# - Container: ${PGSQL_CONTAINER} (<project>-postgres)
# - Volume: ${PGSQL_VOLUME} (<project>-postgres-data)
# - Port: ${PGSQL_PORT}:5432 (default 2340:5432)
# - Credentials: user=postgres, password=postgres
# - Starts existing container if present
create_pgsql_container() {
    if ! docker network ls --format "{{.Name}}" | grep -q "^${DEV_NETWORK}$"; then
        docker network create "$DEV_NETWORK" >/dev/null 2>&1 || true
    fi

    if docker ps -a --format "{{.Names}}" | grep -q "^${PGSQL_CONTAINER}$"; then
        if docker ps --format "{{.Names}}" | grep -q "^${PGSQL_CONTAINER}$"; then
            echo "PostgreSQL container già in esecuzione."
            return 0
        fi
        docker start "$PGSQL_CONTAINER" >/dev/null 2>&1 || {
            echo "Errore nell'avvio del container PostgreSQL."
            return 1
        }
        echo "Container PostgreSQL avviato."
        return 0
    fi

    if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${PGSQL_IMAGE}$"; then
        echo "Download immagine PostgreSQL..."
        docker pull "$PGSQL_IMAGE" >/dev/null 2>&1
    fi

    echo "Creazione container PostgreSQL..."
    docker run -d --name "$PGSQL_CONTAINER" --network "$DEV_NETWORK" \
        -e POSTGRES_USER="$PGSQL_ROOT_USER" \
        -e POSTGRES_PASSWORD="$PGSQL_ROOT_PASSWORD" \
        -p "$PGSQL_PORT:5432" \
        -v "$PGSQL_VOLUME:/var/lib/postgresql" \
        "$PGSQL_IMAGE" >/dev/null 2>&1 || {
            echo "Errore nella creazione del container PostgreSQL."
            return 1
        }

    echo "Container PostgreSQL creato e avviato."
    echo "  Host: localhost:$PGSQL_PORT"
    echo "  User: $PGSQL_ROOT_USER"
    echo "  Password: $PGSQL_ROOT_PASSWORD"
}

install_claude() {
    echo "Verifica installazione Claude Code..."
    
    if docker exec "$DEV_CONTAINER" sh -c "command -v claude >/dev/null 2>&1"; then
        echo "Claude Code già installato."
        return 0
    fi
    
    if ! docker exec "$DEV_CONTAINER" sh -c "command -v node >/dev/null 2>&1"; then
        echo "ERRORE: Node.js non trovato nel container."
        echo "Node.js verrà installato durante la generazione dell'applicazione (./install.sh --dev)"
        return 1
    fi
    
    echo "Installazione Claude Code CLI..."
    if ! docker exec "$DEV_CONTAINER" npm install -g @anthropic-ai/claude-code; then
        echo "ERRORE: Installazione Claude Code fallita."
        return 1
    fi
    
    if ! docker exec "$DEV_CONTAINER" sh -c "command -v claude >/dev/null 2>&1"; then
        echo "ERRORE: Verifica installazione Claude Code fallita."
        return 1
    fi
    
    # Copy Claude Code configuration from archetype if exists
    if [ -d "springtools/archetype/src/main/resources/archetype-resources/.claude" ]; then
        echo "Installazione configurazione Claude Code..."
        
        # Preserve settings.local.json if exists
        if [ -f ".claude/settings.local.json" ]; then
            cp .claude/settings.local.json /tmp/claude-settings.local.json.bak
        fi
        
        rm -rf .claude
        cp -r springtools/archetype/src/main/resources/archetype-resources/.claude .claude
        
        # Restore settings.local.json
        if [ -f "/tmp/claude-settings.local.json.bak" ]; then
            cp /tmp/claude-settings.local.json.bak .claude/settings.local.json
            rm /tmp/claude-settings.local.json.bak
            echo "Preservato settings.local.json esistente"
        fi
        
        echo "Configurazione Claude Code installata in .claude/"
    fi
    
    echo "Claude Code installato con successo."
    echo "Usa 'docker exec -it $DEV_CONTAINER claude' per avviare Claude Code CLI"
}

# Gestione opzione --origin
if [ "$1" = "--origin" ]; then
    echo "Clonazione repository originale..."
    if [ -n "$(ls -A | grep -v '^\.env$' | grep -v '^install\.sh$')" ]; then
        echo "ATTENZIONE: La cartella corrente non è vuota."
        echo "Il clone potrebbe sovrascrivere file esistenti."
        echo -n "Continuare? (s/n): "
        read confirm
        case "$confirm" in
            s|S) ;;
            *) echo "Operazione annullata."; exit 0;;
        esac
    fi

    git clone https://github.com/riccardovacirca/springtools.git .
    echo ""
    echo "=========================================="
    echo "Repository originale clonato con successo!"
    echo "=========================================="
    echo ""
    echo "Puoi ora eseguire:"
    echo "  ./install.sh             # Per creare il container"
    echo "  ./install.sh --dev       # Per generare l'applicazione"
    echo "  ./install.sh --mariadb   # Per creare container MariaDB"
    echo "  ./install.sh --postgres  # Per creare container PostgreSQL"
    echo ""
    exit 0
fi

# Configure application.properties for MariaDB
configure_mariadb_properties() {
    PROPS_FILE="src/main/resources/application.properties"

    if [ ! -f "$PROPS_FILE" ]; then
        echo "ERRORE: File $PROPS_FILE non trovato. Esegui prima './install.sh --dev'"
        return 1
    fi

    echo "  Configurazione application.properties per MariaDB..."

    # Comment out SQLite configuration
    sed 's/^spring\.datasource\.url=jdbc:sqlite:/#spring.datasource.url=jdbc:sqlite:/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed 's/^spring\.datasource\.driver-class-name=org\.sqlite\.JDBC/#spring.datasource.driver-class-name=org.sqlite.JDBC/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"

    # Uncomment MariaDB configuration
    sed "s|^#spring\.datasource\.url=jdbc:mariadb://.*|spring.datasource.url=jdbc:mariadb://$MARIADB_CONTAINER:3306/$MARIADB_NAME|" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed 's/^#spring\.datasource\.driver-class-name=org\.mariadb\.jdbc\.Driver/spring.datasource.driver-class-name=org.mariadb.jdbc.Driver/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed "s/^#spring\.datasource\.username=appuser/spring.datasource.username=$MARIADB_USER/" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed "s/^#spring\.datasource\.password=apppass/spring.datasource.password=$MARIADB_PASSWORD/" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"

    echo "  application.properties configurato per MariaDB"
}

# Configure application.properties for PostgreSQL
configure_pgsql_properties() {
    PROPS_FILE="src/main/resources/application.properties"

    if [ ! -f "$PROPS_FILE" ]; then
        echo "ERRORE: File $PROPS_FILE non trovato. Esegui prima './install.sh --dev'"
        return 1
    fi

    echo "  Configurazione application.properties per PostgreSQL..."

    # Comment out SQLite configuration
    sed 's/^spring\.datasource\.url=jdbc:sqlite:/#spring.datasource.url=jdbc:sqlite:/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed 's/^spring\.datasource\.driver-class-name=org\.sqlite\.JDBC/#spring.datasource.driver-class-name=org.sqlite.JDBC/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"

    # Uncomment PostgreSQL configuration
    sed "s|^#spring\.datasource\.url=jdbc:postgresql://.*|spring.datasource.url=jdbc:postgresql://$PGSQL_CONTAINER:5432/$PGSQL_NAME|" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed 's/^#spring\.datasource\.driver-class-name=org\.postgresql\.Driver/spring.datasource.driver-class-name=org.postgresql.Driver/' "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed "s/^#spring\.datasource\.username=appuser/spring.datasource.username=$PGSQL_USER/" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"
    sed "s/^#spring\.datasource\.password=apppass/spring.datasource.password=$PGSQL_PASSWORD/" "$PROPS_FILE" > "$PROPS_FILE.tmp" && mv "$PROPS_FILE.tmp" "$PROPS_FILE"

    echo "  application.properties configurato per PostgreSQL"
}

# Setup MariaDB database
setup_mariadb_database() {
    echo "  Attesa disponibilità MariaDB..."
    sleep 3

    # Install MariaDB client if not present
    if ! docker exec "$DEV_CONTAINER" sh -c "command -v mysql >/dev/null 2>&1"; then
        echo "  Installazione client MariaDB nel container dev..."
        docker exec "$DEV_CONTAINER" sh -c "apt-get update -qq && apt-get install -y -qq mariadb-client >/dev/null 2>&1"
    fi

    # Wait for MariaDB to be ready
    echo "  Verifica connessione MariaDB..."
    for i in 1 2 3 4 5; do
        if docker exec "$DEV_CONTAINER" mysqladmin ping -h"$MARIADB_CONTAINER" -u"$MARIADB_ROOT_USER" -p"$MARIADB_ROOT_PASSWORD" >/dev/null 2>&1; then
            echo "  MariaDB pronto"
            break
        fi
        echo "  Tentativo $i/5..."
        sleep 2
    done

    if ! docker exec "$DEV_CONTAINER" mysqladmin ping -h"$MARIADB_CONTAINER" -u"$MARIADB_ROOT_USER" -p"$MARIADB_ROOT_PASSWORD" >/dev/null 2>&1; then
        echo "  [WARN] MariaDB non raggiungibile"
        return 1
    fi

    echo "  Configurazione database e permessi..."
    docker exec "$DEV_CONTAINER" mysql -h"$MARIADB_CONTAINER" -u"$MARIADB_ROOT_USER" -p"$MARIADB_ROOT_PASSWORD" \
        -e "GRANT ALL PRIVILEGES ON \`$MARIADB_NAME\`.* TO '$MARIADB_USER'@'%';" 2>/dev/null || true

    echo "  Setup MariaDB completato"
}

# Setup PostgreSQL database
setup_pgsql_database() {
    echo "  Attesa disponibilità PostgreSQL..."
    sleep 3

    # Install PostgreSQL client if not present
    if ! docker exec "$DEV_CONTAINER" sh -c "command -v psql >/dev/null 2>&1"; then
        echo "  Installazione client PostgreSQL nel container dev..."
        docker exec "$DEV_CONTAINER" sh -c "apt-get update -qq && apt-get install -y -qq postgresql-client >/dev/null 2>&1"
    fi

    # Wait for PostgreSQL to be ready
    echo "  Verifica connessione PostgreSQL..."
    for i in 1 2 3 4 5; do
        if docker exec "$DEV_CONTAINER" pg_isready -h"$PGSQL_CONTAINER" -U"$PGSQL_ROOT_USER" >/dev/null 2>&1; then
            echo "  PostgreSQL pronto"
            break
        fi
        echo "  Tentativo $i/5..."
        sleep 2
    done

    if ! docker exec "$DEV_CONTAINER" pg_isready -h"$PGSQL_CONTAINER" -U"$PGSQL_ROOT_USER" >/dev/null 2>&1; then
        echo "  [WARN] PostgreSQL non raggiungibile"
        return 1
    fi

    echo "  Configurazione database e permessi..."
    docker exec "$DEV_CONTAINER" sh -c "PGPASSWORD=\"$PGSQL_ROOT_PASSWORD\" psql -h\"$PGSQL_CONTAINER\" -U\"$PGSQL_ROOT_USER\" -d \"$PGSQL_NAME\" \
        -c \"GRANT ALL ON SCHEMA public TO \\\"$PGSQL_USER\\\";\"" 2>/dev/null || true

    echo "  Setup PostgreSQL completato"
}

# Gestione opzione --mariadb
if [ "$1" = "--mariadb" ]; then
    if [ "$MARIADB_ENABLED" != "y" ]; then
        echo "ERRORE: MariaDB non è abilitato nel file .env"
        echo "Imposta MARIADB_ENABLED=y nel file .env per continuare"
        exit 1
    fi

    echo "Configurazione MariaDB..."

    # Check if dev container is running
    if ! docker ps --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
        echo "ERRORE: Container dev '$DEV_CONTAINER' non in esecuzione."
        echo "Esegui prima './install.sh' per avviare il container dev."
        exit 1
    fi

    # Create MariaDB container
    create_mariadb_container

    # Check if application is generated
    if [ ! -f pom.xml ]; then
        echo ""
        echo "=========================================="
        echo "MariaDB container creato!"
        echo "=========================================="
        echo ""
        echo "Esegui './install.sh --dev' per generare l'applicazione"
        echo "e configurare automaticamente MariaDB."
        exit 0
    fi

    # Configure application.properties
    configure_mariadb_properties || exit 1

    # Setup database
    setup_mariadb_database

    echo ""
    echo "=========================================="
    echo "MariaDB configurato e pronto!"
    echo "=========================================="
    echo "  Host: localhost:$MARIADB_PORT"
    echo "  Database: $MARIADB_NAME"
    echo "  User: $MARIADB_USER"
    echo ""
    exit 0
fi

# Gestione opzione --postgres
if [ "$1" = "--postgres" ]; then
    if [ "$PGSQL_ENABLED" != "y" ]; then
        echo "ERRORE: PostgreSQL non è abilitato nel file .env"
        echo "Imposta PGSQL_ENABLED=y nel file .env per continuare"
        exit 1
    fi

    echo "Configurazione PostgreSQL..."

    # Check if dev container is running
    if ! docker ps --format '{{.Names}}' | grep -q "^$DEV_CONTAINER$"; then
        echo "ERRORE: Container dev '$DEV_CONTAINER' non in esecuzione."
        echo "Esegui prima './install.sh' per avviare il container dev."
        exit 1
    fi

    # Create PostgreSQL container
    create_pgsql_container

    # Check if application is generated
    if [ ! -f pom.xml ]; then
        echo ""
        echo "=========================================="
        echo "PostgreSQL container creato!"
        echo "=========================================="
        echo ""
        echo "Esegui './install.sh --dev' per generare l'applicazione"
        echo "e configurare automaticamente PostgreSQL."
        exit 0
    fi

    # Configure application.properties
    configure_pgsql_properties || exit 1

    # Setup database
    setup_pgsql_database

    echo ""
    echo "=========================================="
    echo "PostgreSQL configurato e pronto!"
    echo "=========================================="
    echo "  Host: localhost:$PGSQL_PORT"
    echo "  Database: $PGSQL_NAME"
    echo "  User: $PGSQL_USER"
    echo ""
    exit 0
fi

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
    echo "Comandi disponibili:"
    echo "  ./install.sh --dev        # Genera l'applicazione Spring Boot dall'archetipo"
    echo "  ./install.sh --mariadb    # Crea container MariaDB"
    echo "  ./install.sh --postgres   # Crea container PostgreSQL"
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
docker exec "$DEV_CONTAINER" sh -c "apt-get update -qq && apt-get install -y -qq sqlite3 git rsync >/dev/null 2>&1"

echo "Installazione archetipo nel repository Maven locale..."
docker exec "$DEV_CONTAINER" mvn -f "$ARCHETYPE_DIR/pom.xml" clean install -q

echo "Generazione applicazione Spring Boot dall'archetipo..."
docker exec "$DEV_CONTAINER" sh -c "
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
    # Prima sposta i file visibili
    for f in $PROJECT_NAME/*; do
        [ -e \"\$f\" ] && mv -f \"\$f\" .
    done
    # Poi sposta i file nascosti (se esistono)
    # Usa -f per forzare la sovrascrittura del .gitignore dell'archetipo
    for f in $PROJECT_NAME/.[!.]*; do
        [ -e \"\$f\" ] && mv -f \"\$f\" .
    done

    # Rimuovi la cartella del progetto (anche se non è vuota)
    rm -rf $PROJECT_NAME
"

echo "Installazione Node.js nel container..."
docker exec "$DEV_CONTAINER" sh -c "
    if ! command -v node >/dev/null 2>&1; then
        curl -fsSL https://deb.nodesource.com/setup_20.x | sh - >/dev/null 2>&1
        apt-get install -y nodejs >/dev/null 2>&1
    fi
"

echo "Installazione dipendenze Svelte..."

docker exec "$DEV_CONTAINER" sh -c "cd gui && npm install >/dev/null 2>&1"

echo "Installazione Claude Code CLI..."
install_claude

echo "Configurazione comando cmd..."
docker exec "$DEV_CONTAINER" sh -c "
    chmod +x /usr/src/app/bin/cmd
    ln -sf /usr/src/app/bin/cmd /usr/local/bin/cmd
"

echo "Configurazione alias cls..."
docker exec "$DEV_CONTAINER" sh -c "
    if ! grep -q 'alias cls=' /root/.bashrc 2>/dev/null; then
        echo \"alias cls='clear'\" >> /root/.bashrc
        echo 'Alias cls aggiunto al .bashrc'
    else
        echo 'Alias cls già presente nel .bashrc'
    fi
"

# Configure databases if enabled
if [ "$MARIADB_ENABLED" = "y" ]; then
    echo ""
    echo "MariaDB abilitato - configurazione..."

    # Check if MariaDB container exists
    if ! docker exec "$DEV_CONTAINER" mysqladmin ping -h"$MARIADB_CONTAINER" -u"$MARIADB_ROOT_USER" -p"$MARIADB_ROOT_PASSWORD" >/dev/null 2>&1; then
        echo "ERRORE: MariaDB è abilitato (MARIADB_ENABLED=y) ma il container '$MARIADB_CONTAINER' non è raggiungibile."
        echo "Installalo con: ./install.sh --mariadb"
        exit 1
    fi

    # Configure application.properties
    configure_mariadb_properties

    echo "MariaDB configurato in application.properties"
fi

if [ "$PGSQL_ENABLED" = "y" ]; then
    echo ""
    echo "PostgreSQL abilitato - configurazione..."

    # Check if PostgreSQL container exists
    if ! docker exec "$DEV_CONTAINER" pg_isready -h"$PGSQL_CONTAINER" -U"$PGSQL_ROOT_USER" >/dev/null 2>&1; then
        echo "ERRORE: PostgreSQL è abilitato (PGSQL_ENABLED=y) ma il container '$PGSQL_CONTAINER' non è raggiungibile."
        echo "Installalo con: ./install.sh --postgres"
        exit 1
    fi

    # Configure application.properties
    configure_pgsql_properties

    echo "PostgreSQL configurato in application.properties"
fi

echo ""
echo "=========================================="
echo "Applicazione Spring Boot + Svelte generata!"
echo "=========================================="

echo "Pulizia repository git locale..."
if [ -d .git ]; then
    rm -rf .git
    echo "Repository git locale rimosso"
fi

echo "Clone repository originale in .toolchain..."
if [ -d .toolchain ]; then
    echo ".toolchain già esistente, verrà aggiornato"
    rm -rf .toolchain
fi

if git clone https://github.com/riccardovacirca/springtools.git .toolchain; then
    echo "Repository originale clonato in .toolchain/"
else
    echo "ATTENZIONE: Clone del repository fallito. Verifica la connessione."
fi

echo "Pulizia file di installazione..."
docker exec "$DEV_CONTAINER" rm -rf archetype
docker exec "$DEV_CONTAINER" rm -f TODO.md
docker exec "$DEV_CONTAINER" rm -f install.sh
echo "File di installazione rimossi (disponibili in .toolchain/)"
