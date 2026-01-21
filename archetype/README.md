# Spring Boot + Svelte Maven Archetype

Maven archetype per generare progetti Spring Boot con frontend Svelte/Vite e database SQLite.

## Installazione Archetipo

Dalla cartella `tmp/springtools/archetype`:

```bash
mvn clean install
```

Questo installa l'archetipo nel repository locale Maven (~/.m2/repository).

## Generazione Progetto

Crea un nuovo progetto usando l'archetipo:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=dev.springtools \
  -DarchetypeArtifactId=spring-svelte-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=dev.myproject \
  -DartifactId=myproject \
  -Dversion=1.0.0-SNAPSHOT \
  -DinteractiveMode=false
```

O in modalità interattiva:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=dev.springtools \
  -DarchetypeArtifactId=spring-svelte-archetype \
  -DarchetypeVersion=1.0.0
```

## Struttura Progetto Generato

```
myproject/
├── pom.xml                      # Maven configuration
├── .env                         # Environment variables
├── .gitignore                   # Git ignore rules
├── README.md                    # Project documentation
├── mvnw, mvnw.cmd, .mvn/       # Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/               # Spring Boot application
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/   # Flyway migrations
│   │       └── static/         # Static files / Svelte build output
│   └── test/                   # Tests
├── gui/                        # Svelte/Vite frontend
│   ├── src/
│   ├── package.json
│   └── vite.config.js
└── bin/                        # Utility scripts
```

## Contenuto Archetipo

**Include:**
- ✅ Spring Boot 3.5.0 con Java 21
- ✅ SQLite database con driver JDBC
- ✅ Flyway per migration database
- ✅ Svelte + Vite frontend
- ✅ REST API di esempio
- ✅ Maven wrapper
- ✅ Script utility in `bin/`
- ✅ Configurazione `.env`

**Esclude:**
- ❌ File di build (target/, node_modules/)
- ❌ File IDE (.vscode/, .idea/)
- ❌ Tool di installazione (.springtools/)
- ❌ Database (data/)

## Aggiornamento Archetipo

Per aggiornare l'archetipo dopo modifiche:

```bash
cd tmp/springtools/archetype
mvn clean install
```

## Pubblicazione

Per pubblicare su repository Maven remoto:

```bash
mvn clean deploy
```

Configura `distributionManagement` nel pom.xml prima di pubblicare.
