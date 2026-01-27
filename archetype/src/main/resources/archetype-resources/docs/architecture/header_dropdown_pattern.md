# Pattern Header Dropdown Contestuale

## Concetto

Ogni modulo può registrare il proprio menu dropdown contestuale che viene visualizzato nell'header principale dell'applicazione. A causa dello spazio limitato dell'header (orizzontale), il menu del modulo appare come un dropdown anziché come menu inline.

## Struttura Modulo

```
module/<module_name>/
├── Layout.svelte              # Entry point del modulo
├── header/
│   └── Layout.svelte          # UI dropdown menu del modulo
├── sidebar/
│   └── Layout.svelte          # UI sidebar del modulo
├── <entity>/
│   └── Component.svelte
└── store.js
```

## Implementazione

### 1. Header Globale

Il modulo header espone uno store e gestisce il dropdown:

**Store** (`module/header/store.js`):
```javascript
import { writable } from 'svelte/store';

// Componente header del modulo attivo
export const contextHeader = writable(null);

// Props da passare al componente header
export const contextHeaderProps = writable({});

// Setta il componente header del modulo
export function setContextHeader(component, props = {}) {
  contextHeader.set(component);
  contextHeaderProps.set(props);
}

// Rimuove il componente header
export function clearContextHeader() {
  contextHeader.set(null);
  contextHeaderProps.set({});
}
```

**Layout** (`module/header/Layout.svelte`):
```svelte
<script>
  import { contextHeader, contextHeaderProps } from './store.js';

  let dropdownOpen = $state(false);

  function closeDropdown() {
    dropdownOpen = false;
  }
</script>

<div class="header-layout">
  <!-- Parte statica sinistra -->
  <div class="header-left">
    <h2>CRM</h2>
  </div>

  <!-- Parte dinamica centro (dropdown) -->
  <div class="header-center">
    {#if $contextHeader}
      <div class="header-dropdown-container">
        <button onclick={() => dropdownOpen = !dropdownOpen}>
          <!-- Trigger: mostra solo titolo -->
          <svelte:component this={$contextHeader} {...$contextHeaderProps} />
        </button>

        {#if dropdownOpen}
          <div class="header-dropdown-menu">
            <!-- Menu: mostra voci -->
            <svelte:component
              this={$contextHeader}
              {...$contextHeaderProps}
              isDropdown={true}
              onClose={closeDropdown}
            />
          </div>
        {/if}
      </div>
    {/if}
  </div>

  <!-- Parte statica destra -->
  <div class="header-right">
    <span>User Info</span>
    <button>Logout</button>
  </div>
</div>
```

### 2. Modulo con Header Dropdown

**Layout Principale del Modulo** (`module/status/Layout.svelte`):
```svelte
<script>
  import { onMount, onDestroy } from 'svelte';
  import { setContextHeader, clearContextHeader } from '../header/store.js';
  import StatusHeaderLayout from './header/Layout.svelte';

  let currentView = $state('health');

  function handleViewChange(view) {
    currentView = view;
  }

  onMount(() => {
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  $effect(() => {
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  onDestroy(() => {
    clearContextHeader();
  });
</script>

<div class="status-container">
  {#if currentView === 'health'}
    <HealthLayout />
  {:else if currentView === 'logs'}
    <LogsLayout />
  {/if}
</div>
```

**Header Dropdown del Modulo** (`module/status/header/Layout.svelte`):
```svelte
<script>
  export let currentView = 'health';
  export let onViewChange = () => {};
  export let isDropdown = false;  // Determina la modalità di rendering
  export let onClose = () => {};

  function handleItemClick(view) {
    onViewChange(view);
    onClose();  // Chiude il dropdown dopo la selezione
  }
</script>

{#if isDropdown}
  <!-- Modalità dropdown: mostra voci del menu -->
  <div class="status-header-dropdown">
    <button
      class:active={currentView === 'health'}
      onclick={() => handleItemClick('health')}
    >
      Health Check
    </button>
    <button
      class:active={currentView === 'logs'}
      onclick={() => handleItemClick('logs')}
    >
      System Logs
    </button>
  </div>
{:else}
  <!-- Modalità trigger: mostra solo titolo -->
  <span>Status</span>
{/if}

<style>
  .status-header-dropdown {
    display: flex;
    flex-direction: column;
    padding: 0.25rem 0;
  }

  .status-header-dropdown button {
    padding: 0.75rem 1rem;
    border: none;
    background: none;
    text-align: left;
    cursor: pointer;
  }

  .status-header-dropdown button:hover {
    background: #f8f9fa;
  }

  .status-header-dropdown button.active {
    background: #e3f2fd;
    color: #007bff;
  }
</style>
```

## Doppia Modalità di Rendering

Il componente header del modulo viene renderizzato **due volte** con prop `isDropdown` diversa:

### Modalità Trigger (isDropdown = false)
- Nel bottone trigger dell'header
- Mostra solo il **titolo del menu** (es. "Status")
- Rendering compatto per adattarsi al trigger

### Modalità Dropdown (isDropdown = true)
- Nel menu dropdown che si apre
- Mostra tutte le **voci del menu**
- Riceve `onClose` per chiudere il dropdown dopo la selezione

## Vantaggi

1. **Spazio ottimizzato** - Menu compatto che non occupa spazio prezioso nell'header
2. **Consistenza UI** - Tutti i moduli usano lo stesso pattern dropdown
3. **Flessibilità** - Ogni modulo definisce liberamente le proprie voci
4. **Riusabilità** - Componente testabile in isolamento
5. **UX migliore** - Chiusura automatica dopo selezione

## Pattern di Comunicazione

### Props Down
```svelte
currentView: string           // Stato corrente
onViewChange: (view) => void  // Callback cambio vista
isDropdown: boolean           // Modalità rendering
onClose: () => void           // Callback chiusura (solo dropdown)
```

### Events Up
```svelte
onViewChange('health')  // Cambio vista
onClose()              // Chiusura dropdown
```

## Header Layout Anatomy

```
┌─────────────────────────────────────────────────────┐
│ [Logo] | [Status ▼] | [User Info] [Logout]         │
│  Left      Center              Right                 │
└─────────────────────────────────────────────────────┘
           │
           ▼ Click
        ┌──────────────┐
        │ Health Check │ ← Active
        │ System Logs  │
        └──────────────┘
```

## Casi d'Uso Avanzati

### Menu con Icone

Esempio reale dal modulo status:

```svelte
<script>
  export let currentView = 'health';
  export let onViewChange = () => {};
  export let isDropdown = false;
  export let onClose = () => {};

  function handleItemClick(view) {
    onViewChange(view);
    onClose();
  }
</script>

{#if isDropdown}
  <div class="status-header-dropdown">
    <button
      class="dropdown-item"
      class:active={currentView === 'health'}
      onclick={() => handleItemClick('health')}
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      Health Check
    </button>
    <button
      class="dropdown-item"
      class:active={currentView === 'logs'}
      onclick={() => handleItemClick('logs')}
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M2 2h12M2 6h12M2 10h12M2 14h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      System Logs
    </button>
  </div>
{:else}
  <span class="status-header-title">Status</span>
{/if}

<style>
  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    width: 100%;
    padding: 0.75rem 1rem;
    background: none;
    border: none;
    text-align: left;
    cursor: pointer;
    font-size: 0.9rem;
    color: #495057;
    transition: background 0.15s;
  }

  .dropdown-item:hover {
    background: #f8f9fa;
  }

  .dropdown-item.active {
    background: #e3f2fd;
    color: #007bff;
  }

  .dropdown-item svg {
    flex-shrink: 0;
    opacity: 0.7;
  }

  .dropdown-item.active svg {
    opacity: 1;
  }
</style>
```

### Menu Gerarchico
```svelte
{#if isDropdown}
  <div class="dropdown">
    <div class="dropdown-section">
      <div class="section-title">Monitoring</div>
      <button onclick={() => handleItemClick('health')}>Health</button>
      <button onclick={() => handleItemClick('metrics')}>Metrics</button>
    </div>
    <div class="dropdown-divider"></div>
    <div class="dropdown-section">
      <div class="section-title">Logs</div>
      <button onclick={() => handleItemClick('app-logs')}>Application</button>
      <button onclick={() => handleItemClick('error-logs')}>Errors</button>
    </div>
  </div>
{:else}
  <span>Monitoring</span>
{/if}
```

### Menu con Badge/Contatori
```svelte
<script>
  export let errorCount = 0;
  export let isDropdown = false;
</script>

{#if isDropdown}
  <div class="dropdown">
    <button>
      Health Check
      {#if errorCount > 0}
        <span class="badge">{errorCount}</span>
      {/if}
    </button>
  </div>
{:else}
  <span>
    Status
    {#if errorCount > 0}
      <span class="badge-small">{errorCount}</span>
    {/if}
  </span>
{/if}
```

### Menu con Descrizioni
```svelte
{#if isDropdown}
  <div class="dropdown">
    <button class="dropdown-item-detailed">
      <div class="item-title">Health Check</div>
      <div class="item-description">System status and uptime</div>
    </button>
    <button class="dropdown-item-detailed">
      <div class="item-title">System Logs</div>
      <div class="item-description">Application and error logs</div>
    </button>
  </div>
{:else}
  <span>Status</span>
{/if}
```

## Best Practices

1. **Titolo Conciso**: Nel trigger usa max 1-2 parole (es. "Status", "Reports")
2. **Chiusura Automatica**: Chiama sempre `onClose()` dopo aver gestito un click
3. **Props Reattive**: Usa `$effect()` per mantenere sincronizzate le props
4. **Cleanup**: Chiama sempre `clearContextHeader()` in `onDestroy()`
5. **Accessibilità**:
   - Usa pulsanti semantici con `<button>`
   - Aggiungi focus visibile
   - Supporta navigazione da tastiera (Enter, Escape)
6. **Visual Feedback**: Evidenzia la voce attiva nel menu
7. **Max Voci**: Limita a 5-7 voci per mantenere usabilità

## Comportamento Dropdown

### Apertura
- Click sul trigger
- Toggle stato `dropdownOpen`
- Animazione smooth dell'icona freccia

### Chiusura
- Click su una voce del menu → chiama `onClose()`
- Click fuori dal dropdown → `handleClickOutside`
- Pressione tasto Escape (implementare se necessario)

### Posizionamento
- `position: absolute`
- `top: calc(100% + 0.5rem)` - Sotto il trigger con gap
- `left: 0` - Allineato a sinistra (o `right: 0` se serve)
- `z-index: 1000` - Sopra gli altri elementi
- `box-shadow` - Per elevazione visiva

## Checklist Implementazione

- [ ] Creare cartella `module/<name>/header/`
- [ ] Creare `module/<name>/header/Layout.svelte`
- [ ] Definire props: `currentView`, `onViewChange`, `isDropdown`, `onClose`
- [ ] Implementare doppia modalità con `{#if isDropdown}`
- [ ] Modalità trigger: mostra solo titolo
- [ ] Modalità dropdown: mostra voci del menu
- [ ] Gestire click su voce: `onViewChange()` + `onClose()`
- [ ] Importare `setContextHeader` e `clearContextHeader` nel Layout principale
- [ ] Registrare componente header in `onMount()`
- [ ] Aggiornare props in `$effect()`
- [ ] Rimuovere componente in `onDestroy()`
- [ ] Testare apertura/chiusura dropdown
- [ ] Testare cambio vista e sincronizzazione stato

## Integrazione con Sidebar

I moduli complessi registrano **entrambi** sidebar e header:

```svelte
<script>
  import { setContextSidebar, clearContextSidebar } from '../sidebar/store.js';
  import { setContextHeader, clearContextHeader } from '../header/store.js';
  import StatusSidebarLayout from './sidebar/Layout.svelte';
  import StatusHeaderLayout from './header/Layout.svelte';

  onMount(() => {
    setContextSidebar(StatusSidebarLayout, { currentView, onViewChange });
    setContextHeader(StatusHeaderLayout, { currentView, onViewChange });
  });

  $effect(() => {
    setContextSidebar(StatusSidebarLayout, { currentView, onViewChange });
    setContextHeader(StatusHeaderLayout, { currentView, onViewChange });
  });

  onDestroy(() => {
    clearContextSidebar();
    clearContextHeader();
  });
</script>
```

Questo permette:
- **Sidebar**: Navigazione verticale con più spazio
- **Header**: Accesso rapido da qualsiasi punto dell'applicazione
- **Consistenza**: Entrambi mostrano lo stesso stato `currentView`
