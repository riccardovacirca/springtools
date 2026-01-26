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
    // Registra sidebar
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });

    // Registra header
    setContextHeader(StatusHeaderLayout, {
      currentView,
      onViewChange: handleViewChange
    });
  });

  $effect(() => {
    // Aggiorna props sidebar
    setContextSidebar(StatusSidebarLayout, {
      currentView,
      onViewChange: handleViewChange
    });

    // Aggiorna props header
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
  <header class="status-header">
    <h1>Spring Boot Status Monitor</h1>
    <p>Verifica lo stato dell'applicazione e visualizza i log recenti</p>
  </header>
  <main class="content">
    {#if currentView === 'health'}
      <HealthLayout />
    {:else if currentView === 'logs'}
      <LogsLayout />
    {/if}
  </main>
</div>

<style>
  .status-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .status-header {
    text-align: center;
    margin-bottom: 30px;
    padding: 20px 0;
  }

  .status-header h1 {
    font-size: 2rem;
    margin-bottom: 10px;
    color: #333;
  }

  .status-header p {
    color: #666;
    font-size: 1rem;
  }

  .content {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
</style>
