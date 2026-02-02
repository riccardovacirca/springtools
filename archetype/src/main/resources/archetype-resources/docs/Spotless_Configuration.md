# Configurazione Spotless - Stile Codice

## Panoramica

Il progetto usa **Spotless** con **Eclipse Formatter** per la formattazione automatica del codice Java.

## Comando

```bash
cmd app format
```

Oppure direttamente con Maven:
```bash
mvn spotless:apply    # Applica formattazione
mvn spotless:check    # Verifica formattazione (esegue in fase verify)
```

## Stile Implementato

### ✅ Supportato completamente

1. **Parentesi graffe metodi**: Su riga separata (Allman/BSD style)
   ```java
   public void myMethod()
   {
     // codice
   }
   ```

2. **Parentesi graffe blocchi interni**: Su stessa riga
   ```java
   if (condition) {
     // codice
   }
   ```

3. **Indentazione**: 2 spazi
4. **Lunghezza riga**: Max 120 caratteri
5. **Rimozione import inutilizzati**: Automatica
6. **Trailing whitespace**: Rimosso automaticamente
7. **End with newline**: Assicurato

### ⚠️ Parzialmente supportato

1. **Parametri metodo**: Su righe separate con indentazione
   ```java
   public ResponseEntity<Map<String, Object>> method(
       @RequestParam int param1,
       @RequestParam int param2)
   ```

2. **Throws clause**: Su riga separata
   ```java
   public void method(int param)
     throws Exception
   ```

   **Limitazione**: L'indentazione di `throws` non può essere impostata a un livello inferiore rispetto ai parametri. Eclipse Formatter allinea `throws` con i parametri.

### ❌ Non supportato automaticamente

1. **Dichiarazione e assegnamento separati**

   **Richiesto**:
   ```java
   List<StatusLogDto> logs;
   ResponseEntity<Map<String, Object>> resp;

   logs = service.getLogs(num, off);
   resp = HttpResponse.create().out(logs).build();
   ```

   **Motivo**: Nessun formatter automatico supporta questa convenzione. È una scelta stilistica che richiede implementazione manuale.

2. **Indentazione throws ridotta**

   **Richiesto**:
   ```java
   public void method(
       @Param int x,
       @Param int y)
     throws Exception  // Indentato 2 spazi invece di 4
   ```

   **Motivo**: Eclipse Formatter non supporta indentazione variabile per clausole diverse dello stesso metodo.

## Configurazione Attuale

File: `.spotless/eclipse-formatter.xml`

**Regole principali**:
- Parentesi metodi: `next_line`
- Parentesi blocchi: `end_of_line`
- Indentazione: 2 spazi
- Tab: Convertiti in spazi
- Lunghezza riga: 120 caratteri

## Raccomandazioni

Per ottenere lo stile richiesto completamente:

1. **Usare il formatter** per:
   - Posizionamento parentesi graffe
   - Indentazione generale
   - Import e whitespace

2. **Applicare manualmente**:
   - Separazione dichiarazione/assegnamento
   - Verificare indentazione throws se critica

3. **Code Review**: Controllare che lo stile sia rispettato prima dei commit

## File di Configurazione

- `pom.xml`: Configurazione plugin Spotless
- `.spotless/eclipse-formatter.xml`: Regole formattazione Eclipse
- `bin/cmd`: Script con comando `app format`

## Verifica in Build

Spotless è configurato per verificare la formattazione in fase `verify`:

```bash
mvn verify  # Include controllo formattazione
```

Se il codice non è formattato correttamente, la build fallisce.

## Note

- Lo stile richiesto (dichiarazione separata, throws con indentazione personalizzata) è **non standard**
- I formatter automatici seguono convenzioni Java consolidate (Google, Oracle, Eclipse)
- Per stili molto personalizzati, considera **regole checkstyle** invece che formatter automatici
- La formattazione manuale richiede disciplina nel team

## Alternative

Se lo stile richiesto è critico:

1. **Checkstyle**: Può verificare regole personalizzate ma non corregge
2. **Formatter personalizzato**: Sviluppare plugin Spotless custom (complesso)
3. **Pre-commit hooks**: Verificare manualmente prima del commit
4. **Code review**: Processo umano di verifica stile
