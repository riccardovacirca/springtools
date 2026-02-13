# Context del Progetto

## Ambiente di Sviluppo

Questo è l'ambiente di sviluppo di un progetto Spring dockerizzato.
L'esecuzione avviene all'interno di Docker.

## Architettura del Progetto

### Filosofia
- Progetto minimalista, senza magia
- Organizzazione modulare
- Utilizzo di librerie interne per la maggior parte delle operazioni

### Stack Tecnologico

#### Backend
- Framework: Spring
- Database di default: SQLite3
- Migration: Flyway
- Connessione DB: JDBC
- Database alternativi supportati: MariaDB, PostgreSQL

#### Frontend
- Framework: Svelte/Vite
- Posizione: /gui

### Libreria Interna
Percorso: /usr/src/app/src/main/java/dev/springtools/util

La libreria interna contiene utility per la maggior parte delle operazioni.
Se una funzionalità manca, deve essere aggiunta alla libreria.

#### Utility Disponibili

* **DB** - Astrazione database con supporto JDBC
* **Excel** - Importazione e parsing file Excel/CSV
  * ExcelReader - Lettura file Excel
  * ExcelImporter - Import con mapping e normalizzazione
  * Strategie di normalizzazione personalizzabili
* **DateTime** - Gestione date e timestamp
* **HttpRequest/HttpResponse** - Client HTTP
* **JSON** - Manipolazione JSON
* **File** - Operazioni su file
* **Env** - Gestione variabili d'ambiente

## Struttura dei Moduli

Un modulo completo si compone di tre parti:

1. Parte Spring
2. Migration per il database
3. Parte Svelte

### Esempio: Modulo Status (preinstallato)

- Parte Spring: src/main/java/dev/crm/module/status
- Parte GUI: svelte/src/module/status
- Migration SQL: src/main/resources/db/migration/V1__init_database.sql

## Gestione del Progetto

### Cartella .springtools
Contiene il repository del progetto di origine (springtools) da cui
l'applicazione è stata generata.

Caratteristiche:
- Mantiene il repository .git originale
- Permette di far evolvere il progetto di origine durante lo sviluppo

### Tool di Gestione
Percorso: /usr/src/app/bin/cmd

Utilizzato per molti comandi di gestione del progetto.

### Documentazione
Percorso: /docs

Contiene:
- Documentazione del progetto
- DSL (Domain Specific Language) per garantire un modello standard di sviluppo

### Configurazione
File: .env

Contiene la configurazione di installazione del progetto.
