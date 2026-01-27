# Pattern Sidebar Contestuale

## Concetto

Ogni modulo può registrare il proprio componente sidebar personalizzato che viene visualizzato nella sidebar principale dell'applicazione. Questo approccio separa la logica di navigazione del modulo dalla sidebar globale.

## Struttura Modulo

```
module/<module_name>/
├── Layout.svelte              # Entry point del modulo
├── sidebar/
│   └── Layout.svelte          # UI sidebar specifica del modulo
├── <entity>/
│   └── Component.svelte
└── store.js                   # Store del modulo
```

## Implementazione

### 1. Sidebar Globale

Il modulo sidebar espone uno store per registrare componenti:

**Store** (`module/sidebar/store.js`):
```javascript
import { writable } from 'svelte/store';

// Componente sidebar del modulo attivo
export const contextSidebar = writable(null);

// Props da passare al componente sidebar
export const contextSidebarProps = writable({});

// Setta il componente sidebar del modulo
export function setContextSidebar(component, props = {}) {
  contextSidebar.set(component);
  contextSidebarProps.set(props);
}

// Rimuove il componente sidebar
export function clearContextSidebar() {
  contextSidebar.set(null);
  contextSidebarProps.set({});
}
```

**Layout** (`module/sidebar/Layout.svelte`):
```svelte
<script>
  import { contextSidebar, contextSidebarProps } from "./store.js";
</script>

<div class="sidebar-layout">
  <!-- Menu statico globale -->
  <nav class="sidebar-static">
    <button>Home</button>
  </nav>

  <!-- Area dinamica per il modulo -->
  {#if $contextSidebar}
    <div class="sidebar-dynamic">
      <svelte:component this={$contextSidebar} {...$contextSidebarProps} />
    </div>
  {/if}
</div>
```

### 2. Modulo con Sidebar Personalizzata

**Layout Principale** (`module/status/Layout.svelte`):
```svelte
<script>
  import { onMount, onDestroy } from 'svelte';
  import { setContextSidebar, clearContextSidebar } from '../sidebar/store.js';
  import StatusSidebarLayout from './sidebar/Layout.svelte';
  import HealthLayout from "./health/HealthLayout.svelte";
  import LogsLayout from "./logs/LogsLayout.svelte";

  let currentView = $state('health');

  function handleViewChange(view) {
    currentView = view;
  }

  // Registra il componente sidebar all'attivazione del modulo
  onMount(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  // Aggiorna props quando cambiano
  $effect(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  // Rimuove il componente sidebar quando il modulo viene disattivato
  onDestroy(() => {
    clearContextSidebar();
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

**Sidebar del Modulo** (`module/status/sidebar/Layout.svelte`):
```svelte
<script>
  export let currentView = 'health';
  export let onViewChange = () => {};
</script>

<div class="status-sidebar">
  <div class="sidebar-title">Status</div>
  <nav class="sidebar-nav">
    <button
      class:active={currentView === 'health'}
      onclick={() => onViewChange('health')}
    >
      Health
    </button>
    <button
      class:active={currentView === 'logs'}
      onclick={() => onViewChange('logs')}
    >
      Logs
    </button>
  </nav>
</div>

<style>
  .status-sidebar {
    padding: 0;
  }

  .sidebar-title {
    padding: 0.5rem 1rem;
    font-size: 0.75rem;
    text-transform: uppercase;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 0.05em;
  }

  .sidebar-nav {
    display: flex;
    flex-direction: column;
  }

  .sidebar-nav button {
    display: block;
    width: 100%;
    padding: 0.5rem 1rem 0.5rem 1.5rem;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.6);
    text-align: left;
    cursor: pointer;
    font-size: 0.9rem;
  }

  .sidebar-nav button:hover {
    background: rgba(255, 255, 255, 0.05);
    color: white;
  }

  .sidebar-nav button.active {
    color: #3498db;
  }
</style>
```

## Vantaggi

1. **Separazione delle responsabilità**: La logica di navigazione del modulo è contenuta nel suo componente sidebar
2. **Riusabilità**: Il componente sidebar può essere usato anche in test isolati
3. **Scalabilità**: Sidebar complesse con più livelli, filtri, ecc. hanno il loro spazio dedicato
4. **Manutenibilità**: Ogni modulo gestisce autonomamente la propria UI sidebar
5. **Flessibilità**: Ogni modulo può avere un layout sidebar completamente diverso

## Pattern di Comunicazione

### Props Down
Il Layout principale passa props reattive al componente sidebar:
- `currentView`: stato attuale
- Altri dati necessari per il rendering

### Events Up
Il componente sidebar comunica tramite callback:
- `onViewChange`: notifica cambio vista
- Altri eventi custom necessari

## Confronto con Pattern Precedente

### Prima (Object-based)
```svelte
// Nel Layout del modulo - codice complesso
onMount(() => {
  setContextMenu({
    title: 'Status',
    items: [
      { label: 'Health', active: currentView === 'health', action: () => currentView = 'health' },
      { label: 'Logs', active: currentView === 'logs', action: () => currentView = 'logs' }
    ]
  });
});

$effect(() => {
  // Ridefinire tutto il menu ad ogni cambio
  setContextMenu({ ... });
});
```

### Dopo (Component-based)
```svelte
// Nel Layout del modulo - codice semplice
onMount(() => {
  setContextSidebar(StatusSidebarLayout, {
    currentView,
    onViewChange: handleViewChange
  });
});

$effect(() => {
  // Aggiorna solo le props
  setContextSidebar(StatusSidebarLayout, {
    currentView,
    onViewChange: handleViewChange
  });
});
```

## Casi d'Uso Avanzati

### Sidebar Gerarchica
```svelte
<!-- module/reports/sidebar/Layout.svelte -->
<script>
  export let currentSection = 'sales';
  export let currentReport = null;
  export let onNavigate;
</script>

<div class="sidebar-title">Reports</div>

<nav class="sidebar-nav">
  <button class:active={currentSection === 'sales'} onclick={() => onNavigate('sales', null)}>
    Sales
  </button>
  {#if currentSection === 'sales'}
    <div class="submenu">
      <button class:active={currentReport === 'daily'} onclick={() => onNavigate('sales', 'daily')}>
        Daily
      </button>
      <button class:active={currentReport === 'monthly'} onclick={() => onNavigate('sales', 'monthly')}>
        Monthly
      </button>
    </div>
  {/if}

  <button class:active={currentSection === 'inventory'} onclick={() => onNavigate('inventory', null)}>
    Inventory
  </button>
</nav>
```

### Sidebar con Filtri
```svelte
<!-- module/contacts/sidebar/Layout.svelte -->
<script>
  export let filters = { status: 'all', type: 'all' };
  export let onFilterChange;
</script>

<div class="sidebar-title">Contacts</div>

<div class="filters">
  <div class="filter-group">
    <label>Status</label>
    <select bind:value={filters.status} onchange={() => onFilterChange(filters)}>
      <option value="all">All</option>
      <option value="active">Active</option>
      <option value="inactive">Inactive</option>
    </select>
  </div>

  <div class="filter-group">
    <label>Type</label>
    <select bind:value={filters.type} onchange={() => onFilterChange(filters)}>
      <option value="all">All</option>
      <option value="customer">Customer</option>
      <option value="supplier">Supplier</option>
    </select>
  </div>
</div>
```

## Best Practices

1. **Props Reattive**: Usa sempre `$effect()` per mantenere le props sincronizzate con lo stato del modulo
2. **Cleanup**: Chiama sempre `clearContextSidebar()` in `onDestroy()`
3. **Callback Naming**: Usa convenzioni chiare come `onViewChange`, `onFilterChange`, ecc.
4. **Stili Consistenti**: Mantieni lo stesso schema colori/spaziatura della sidebar globale
5. **Accessibilità**: Assicurati che i pulsanti abbiano focus visibile e siano navigabili da tastiera

## Checklist Implementazione

- [ ] Creare cartella `module/<name>/sidebar/`
- [ ] Creare `module/<name>/sidebar/Layout.svelte` con UI del menu
- [ ] Definire props necessarie (`export let ...`)
- [ ] Definire callbacks per comunicare con il Layout principale
- [ ] Importare `setContextSidebar` e `clearContextSidebar` nel Layout principale
- [ ] Registrare il componente sidebar in `onMount()`
- [ ] Aggiornare props in `$effect()`
- [ ] Rimuovere il componente in `onDestroy()`
- [ ] Testare navigazione e reattività
