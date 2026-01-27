MODULE_STYLE hierarchical_layout_architecture

# ARCHITECTURE

PRINCIPLE: Separazione tra layout (routing/visibilità) e componenti (logica applicativa)

HIERARCHY:
  - Levels: module -> entity -> subentity
  - Max depth: 3 livelli
  - Rationale: Profondità limitata per garantire gestibilità

IMPORT STYLE:
  - Type: static imports only
  - Required: import Component from './path/Component.svelte'
  - Forbidden: dynamic import ({#await import(...)})
  - Rationale: Import statici garantiscono stabilità e prevedibilità

# DIRECTORY STRUCTURE

```
module/<module_name>/
├── Layout.svelte                    # Entry point (importato da src/Application.svelte)
├── sidebar/                         # OPZIONALE: UI sidebar contestuale
│   └── Layout.svelte                # Componente sidebar del modulo
├── header/                          # OPZIONALE: UI header dropdown contestuale
│   └── Layout.svelte                # Componente header del modulo
├── <EntityName>Layout.svelte        # Entity layout (1 per entità)
├── store.js                         # Store unico centralizzato
├── <entity_name>/                   # Cartella entità (snake_case)
│   ├── <EntityName>Layout.svelte    # OBBLIGATORIO: organizza i componenti
│   ├── <ComponentName>Component.svelte
│   └── ... (altri componenti)
│   oppure (se complesso con sub-entità):
│   ├── <EntityName>Layout.svelte
│   ├── <SubentityName>Layout.svelte
│   └── <subentity_name>/            # Cartella sub-entità
│       └── <ComponentName>Component.svelte
```

CONTEXTUAL UI (sidebar/header):
  - Pattern: Component-based registration
  - Location: module/<name>/sidebar/ e module/<name>/header/
  - Purpose: Registrazione UI contestuale nella sidebar/header globale
  - Docs: Vedi docs/architecture/sidebar_context_pattern.md e header_dropdown_pattern.md
  - Note: Opzionali, usare solo se il modulo richiede navigazione contestuale

# NAMING CONVENTIONS

LAYOUTS:
  - Pattern: <Name>Layout.svelte
  - Case: PascalCase
  - Suffix: Layout
  - Examples:
    - Layout.svelte (entry point modulo)
    - ContattiLayout.svelte (entity layout)
    - DettaglioLayout.svelte (subentity layout)

COMPONENTS:
  - Pattern: <Name>Component.svelte
  - Case: PascalCase
  - Suffix: Component
  - Examples: ListaComponent.svelte, FormComponent.svelte, TabellaComponent.svelte

FOLDERS:
  - Case: snake_case
  - Examples: contatti, liste, campagne, dettaglio_contatto

STORE:
  - Name: store.js
  - Location: module root only (mai nelle sottocartelle)
  - Required: true

TEST ENTRY (opzionale):
  - Name: index.html
  - Purpose: Entry point di test per visualizzare il modulo isolato
  - Note: Senza contesto applicativo e senza autenticazione

# LAYOUT RULES

RESPONSIBILITY:
  Allowed:
    - Import statici di componenti e sub-layout
    - Routing via {#if}/{:else if} basato su stato
    - Passaggio props ai componenti figli
    - Navigazione e visibilità

  Forbidden:
    - Logica applicativa
    - Chiamate API dirette
    - Gestione stato complesso
    - Manipolazione dati
    - Business logic

STATE MANAGEMENT:
  - Navigation state: gestito nel layout (currentView, activeTab)
  - Application state: gestito nello store centralizzato
  - Local state: gestito nei componenti

PROP PASSING:
  - Pattern: export let propName
  - Direction: top-down (layout -> component)
  - Example: <ContattiLayout {currentView} />

# CONTEXTUAL UI PATTERNS

SIDEBAR PATTERN:
  - Purpose: Registrare menu contestuale nella sidebar globale
  - File: module/<name>/sidebar/Layout.svelte
  - Props richieste:
    - currentView: stato attuale navigazione
    - onViewChange: callback per cambio vista
  - Registrazione: onMount(() => setContextSidebar(SidebarLayout, props))
  - Aggiornamento: $effect(() => setContextSidebar(SidebarLayout, props))
  - Cleanup: onDestroy(() => clearContextSidebar())
  - Docs: docs/architecture/sidebar_context_pattern.md

HEADER PATTERN:
  - Purpose: Registrare dropdown contestuale nell'header globale
  - File: module/<name>/header/Layout.svelte
  - Props richieste:
    - currentView: stato attuale navigazione
    - onViewChange: callback per cambio vista
    - isDropdown: boolean per doppia modalità rendering
    - onClose: callback per chiudere dropdown
  - Doppia modalità:
    - isDropdown=false: mostra solo titolo (nel trigger)
    - isDropdown=true: mostra voci menu (nel dropdown)
  - Registrazione: onMount(() => setContextHeader(HeaderLayout, props))
  - Aggiornamento: $effect(() => setContextHeader(HeaderLayout, props))
  - Cleanup: onDestroy(() => clearContextHeader())
  - Docs: docs/architecture/header_dropdown_pattern.md

QUANDO USARE:
  - Moduli con multiple viste/sezioni (es. Status: Health, Logs)
  - Moduli con navigazione interna complessa
  - Moduli che richiedono accesso rapido da qualsiasi punto dell'app
  - NON usare per moduli con singola vista semplice

ESEMPIO REGISTRAZIONE:
```svelte
<script>
  import { onMount, onDestroy } from 'svelte';
  import { setContextSidebar, clearContextSidebar } from '../sidebar/store.js';
  import { setContextHeader, clearContextHeader } from '../header/store.js';
  import StatusSidebarLayout from './sidebar/Layout.svelte';
  import StatusHeaderLayout from './header/Layout.svelte';

  let currentView = $state('health');

  function handleViewChange(view) {
    currentView = view;
  }

  onMount(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  $effect(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  onDestroy(() => {
    clearContextSidebar();
    clearContextHeader();
  });
</script>
```

# COMPONENT RULES

RESPONSIBILITY:
  Required:
    - Logica applicativa completa
    - Chiamate API
    - Gestione stato locale
    - Interazioni utente
    - Rendering UI
    - Event handling

  Forbidden:
    - Decisione sulla propria visibilità (compete al layout)
    - Import di altri layout
    - Gestione navigazione globale

VISIBILITY RULE:
  - Principle: Il componente si occupa solo del rendering
  - Decision: Sempre delegata al layout parent
  - Pattern: Layout decide {#if show}<Component />{/if}
  - Component: renderizza sempre, non decide se mostrarsi

STORE ACCESS:
  - Import: import { storeName } from '../store.js'
  - Usage: lettura e scrittura stato condiviso

# STORE RULES

LOCATION: module/<module_name>/store.js
SCOPE: Unico per modulo
CENTRALIZATION: Obbligatoria (no store multipli)

CONTENT:
  - Stato condiviso del modulo
  - Derived state
  - Actions/mutations

IMPORT PATTERN:
  - From entity: import { state } from '../store.js'
  - From subentity: import { state } from '../../store.js'

# IMPORT PATTERNS

MODULE LAYOUT imports:
  - Entity layouts
  - Example: import ContattiLayout from './ContattiLayout.svelte'

ENTITY LAYOUT imports:
  - Components from entity folder OR subentity layouts
  - Simple: import ListaComponent from './contatti/ListaComponent.svelte'
  - Complex: import DettaglioLayout from './contatti/DettaglioLayout.svelte'

SUBENTITY LAYOUT imports:
  - Components from subentity folder only
  - Example: import FormComponent from './dettaglio/FormComponent.svelte'

FORBIDDEN:
  - Dynamic imports: {#await import('./Component.svelte')}
  - Cross-entity component imports
  - Store imports in layouts (solo stato navigazione locale)

# DATA FLOW

NAVIGATION:
  Flow: Layout.svelte -> EntityLayout -> Component
  State: currentView passed as prop or managed in layout

APPLICATION DATA:
  Flow: store.js <-> Components
  Pattern: Components read/write to centralized store

API CALLS:
  Location: Components only
  Forbidden in: Layouts

# ANTI-PATTERNS

FORBIDDEN:
  ✗ Dynamic imports
    Example: {#await import('./Component.svelte')}
    Reason: Meno stabile, comportamento imprevedibile

  ✗ Logic in layouts
    Example: API calls, data manipulation in Layout.svelte
    Reason: Layouts devono essere puri routing/visibility

  ✗ Self visibility decision
    Example: Component che decide {#if shouldShow} internamente
    Reason: Visibilità compete al layout parent

  ✗ Multiple stores
    Example: store.js in ogni sottocartella
    Reason: Store deve essere unico e centralizzato

  ✗ Deep nesting
    Example: subentity/sub-subentity/sub-sub-subentity
    Reason: Max 3 livelli: module -> entity -> subentity

  ✗ Cross-entity imports
    Example: contatti/ importing from liste/
    Reason: Entità devono essere autonome

  ✗ Wrong naming
    Examples: contatti.svelte, contattiComponent.svelte, Contatti/
    Reason: Naming conventions garantiscono consistenza

# EXAMPLES

## Module Layout (Entry Point)

File: module/risorse/Layout.svelte
```svelte
<script>
  import ContattiLayout from './ContattiLayout.svelte';
  import ListeLayout from './ListeLayout.svelte';
  import CampagneLayout from './CampagneLayout.svelte';

  let currentView = $state('contatti');
</script>

<nav>
  <button onclick={() => currentView = 'contatti'}>Contatti</button>
  <button onclick={() => currentView = 'liste'}>Liste</button>
  <button onclick={() => currentView = 'campagne'}>Campagne</button>
</nav>

<main>
  {#if currentView === 'contatti'}
    <ContattiLayout />
  {:else if currentView === 'liste'}
    <ListeLayout />
  {:else if currentView === 'campagne'}
    <CampagneLayout />
  {/if}
</main>
```

## Entity Layout (Simple)

File: module/risorse/ContattiLayout.svelte
```svelte
<script>
  import ListaComponent from './contatti/ListaComponent.svelte';
  import FormComponent from './contatti/FormComponent.svelte';

  let showForm = $state(false);
</script>

{#if showForm}
  <FormComponent onClose={() => showForm = false} />
{:else}
  <ListaComponent onAdd={() => showForm = true} />
{/if}
```

## Entity Layout (Complex with Subentity)

File: module/risorse/ContattiLayout.svelte
```svelte
<script>
  import ListaComponent from './contatti/ListaComponent.svelte';
  import DettaglioLayout from './contatti/DettaglioLayout.svelte';

  let selectedId = $state(null);
</script>

{#if selectedId}
  <DettaglioLayout id={selectedId} onBack={() => selectedId = null} />
{:else}
  <ListaComponent onSelect={(id) => selectedId = id} />
{/if}
```

## Subentity Layout

File: module/risorse/contatti/DettaglioLayout.svelte
```svelte
<script>
  import InfoComponent from './dettaglio/InfoComponent.svelte';
  import StoriaComponent from './dettaglio/StoriaComponent.svelte';
  import AzioniComponent from './dettaglio/AzioniComponent.svelte';

  export let id;
  export let onBack;

  let activeTab = $state('info');
</script>

<button onclick={onBack}>Indietro</button>

<nav>
  <button onclick={() => activeTab = 'info'}>Info</button>
  <button onclick={() => activeTab = 'storia'}>Storia</button>
  <button onclick={() => activeTab = 'azioni'}>Azioni</button>
</nav>

{#if activeTab === 'info'}
  <InfoComponent {id} />
{:else if activeTab === 'storia'}
  <StoriaComponent {id} />
{:else if activeTab === 'azioni'}
  <AzioniComponent {id} />
{/if}
```

## Component

File: module/risorse/contatti/ListaComponent.svelte
```svelte
<script>
  import { contatti } from '../store.js';

  export let onSelect;
  export let onAdd;

  async function loadContatti() {
    const response = await fetch('/api/risorse/contatti');
    $contatti = await response.json();
  }
</script>

<button onclick={onAdd}>Nuovo Contatto</button>

{#each $contatti as contatto}
  <div onclick={() => onSelect(contatto.id)}>
    {contatto.nome}
  </div>
{/each}
```

## Store

File: module/risorse/store.js
```javascript
import { writable } from 'svelte/store';

export const contatti = writable([]);
export const liste = writable([]);
export const campagne = writable([]);
export const loading = writable(false);
export const error = writable(null);
```

# SIMPLE MODULE EXAMPLE

```
module/risorse/
├── Layout.svelte              # Entry point
├── ContattiLayout.svelte      # Entity layout
├── ListeLayout.svelte         # Entity layout
├── CampagneLayout.svelte      # Entity layout
├── store.js                   # Centralized store
├── contatti/
│   ├── ListaComponent.svelte
│   └── FormComponent.svelte
├── liste/
│   ├── ListaComponent.svelte
│   └── EditorComponent.svelte
└── campagne/
    ├── DashboardComponent.svelte
    └── ConfigComponent.svelte
```

# COMPLEX MODULE EXAMPLE

```
module/risorse/
├── Layout.svelte
├── ContattiLayout.svelte
├── store.js
└── contatti/
    ├── ListaComponent.svelte
    ├── DettaglioLayout.svelte     # Subentity layout
    └── dettaglio/                  # Subentity folder
        ├── InfoComponent.svelte
        ├── StoriaComponent.svelte
        └── AzioniComponent.svelte
```

# MODULE WITH CONTEXTUAL UI EXAMPLE

```
module/status/
├── Layout.svelte                    # Entry point con registrazione sidebar/header
├── sidebar/
│   └── Layout.svelte                # Menu sidebar con Health/Logs
├── header/
│   └── Layout.svelte                # Dropdown header con Health/Logs
├── store.js
├── health/
│   ├── HealthLayout.svelte
│   └── HealthComponent.svelte
└── logs/
    ├── LogsLayout.svelte
    └── LogsComponent.svelte
```

Layout.svelte:
```svelte
<script>
  import { onMount, onDestroy } from 'svelte';
  import { setContextSidebar, clearContextSidebar } from '../sidebar/store.js';
  import { setContextHeader, clearContextHeader } from '../header/store.js';
  import StatusSidebarLayout from './sidebar/Layout.svelte';
  import StatusHeaderLayout from './header/Layout.svelte';
  import HealthLayout from "./health/HealthLayout.svelte";
  import LogsLayout from "./logs/LogsLayout.svelte";

  let currentView = $state('health');

  function handleViewChange(view) {
    currentView = view;
  }

  onMount(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  $effect(() => {
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  onDestroy(() => {
    clearContextSidebar();
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

sidebar/Layout.svelte:
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
```

header/Layout.svelte:
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
  <!-- Modalità dropdown: mostra voci -->
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
```

# CHECKLIST: MODULO CON SIDEBAR E HEADER

Layout Principale:
- [ ] Import setContextSidebar, clearContextSidebar da '../sidebar/store.js'
- [ ] Import setContextHeader, clearContextHeader da '../header/store.js'
- [ ] Import componenti sidebar/Layout.svelte e header/Layout.svelte
- [ ] Definire stato navigazione (let currentView = $state('default'))
- [ ] Definire handler cambio vista (function handleViewChange(view))
- [ ] Registrare sidebar e header in onMount()
- [ ] Aggiornare props in $effect()
- [ ] Cleanup in onDestroy()
- [ ] Routing condizionale basato su currentView

Sidebar Layout (sidebar/Layout.svelte):
- [ ] Definire props: export let currentView, export let onViewChange
- [ ] Implementare UI navigazione con pulsanti
- [ ] Applicare classe .active basata su currentView
- [ ] Chiamare onViewChange al click
- [ ] Stili: padding, colori, hover, active state

Header Layout (header/Layout.svelte):
- [ ] Definire props: currentView, onViewChange, isDropdown, onClose
- [ ] Implementare doppia modalità con {#if isDropdown}
- [ ] Modalità trigger (isDropdown=false): solo titolo
- [ ] Modalità dropdown (isDropdown=true): voci menu
- [ ] Funzione handleItemClick che chiama onViewChange + onClose
- [ ] Stili: dropdown layout, item hover, active state
- [ ] Opzionale: icone SVG per migliorare UX

# IMPORT CHAIN

```
src/Application.svelte
  └── import Layout from './module/risorse/Layout.svelte'
        └── import ContattiLayout from './ContattiLayout.svelte'
              └── import ListaComponent from './contatti/ListaComponent.svelte'
              └── import DettaglioLayout from './contatti/DettaglioLayout.svelte'
                    └── import InfoComponent from './dettaglio/InfoComponent.svelte'
```
