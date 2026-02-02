<script>
  import { onMount } from 'svelte'
  import { auth, checkAuth, currentModule, currentModuleName, sidebarVisible } from './store.js'
  import SidebarLayout from './module/sidebar/Layout.svelte'
  import HeaderLayout from './module/header/Layout.svelte'
  import AuthLayout from './module/auth/Layout.svelte'
  import StatusLayout from './module/status/Layout.svelte'

  // Import Bootstrap CSS
  import 'bootstrap/dist/css/bootstrap.min.css'

  // Import Bootstrap JS
  import * as bootstrap from 'bootstrap'

  // Rendi bootstrap disponibile globalmente per i componenti
  if (typeof window !== 'undefined') {
    window.bootstrap = bootstrap
  }

  let loading = true

  onMount(async () => {
    await checkAuth()
    currentModule.initFromURL()
    loading = false
  })
</script>

{#if loading}
  <div class="d-flex justify-content-center align-items-center min-vh-100">
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else if $auth.token}
  <div class="d-flex">
    <!-- Sidebar Desktop -->
    <div class="sidebar bg-dark text-white d-none d-lg-flex flex-column">
      <SidebarLayout />
    </div>

    <!-- Main Content -->
    <div class="flex-grow-1 d-flex flex-column min-vh-100">
      <!-- Header -->
      <header class="bg-white border-bottom sticky-top">
        <HeaderLayout />
      </header>

      <!-- Body -->
      <main class="flex-grow-1 p-3 bg-light">
        {#if $currentModuleName === 'status'}
          <StatusLayout />
        {:else}
          <div class="container-lg">
            <div class="card">
              <div class="card-body">
                <h1 class="card-title">Modulo {$currentModuleName}</h1>
                <p class="card-text">Modulo in fase di implementazione</p>
                <p class="text-muted small">
                  Percorso: /{$currentModule.module}{$currentModule.subPath ? '/' + $currentModule.subPath : ''}
                </p>
              </div>
            </div>
          </div>
        {/if}
      </main>
    </div>

    <!-- Sidebar Mobile (Offcanvas) -->
    <div class="offcanvas offcanvas-start bg-dark text-white" tabindex="-1" id="sidebarOffcanvas">
      <div class="offcanvas-header border-bottom border-secondary">
        <h5 class="offcanvas-title">CRM Contact Center</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Chiudi menu"></button>
      </div>
      <div class="offcanvas-body p-0">
        <SidebarLayout />
      </div>
    </div>
  </div>
{:else}
  <AuthLayout />
{/if}

<style>
  .sidebar {
    width: 256px;
    height: 100vh;
    position: sticky;
    top: 0;
  }

  header {
    height: 60px;
  }

  :global(header .container-fluid) {
    height: 100%;
    padding: 0 !important;
  }

  :global(header .d-flex) {
    height: 100%;
  }

  :global(.btn-chiama) {
    min-height: 60px;
  }
</style>
