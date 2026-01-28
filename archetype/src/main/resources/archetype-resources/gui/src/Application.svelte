<script>
  import '@coreui/coreui/dist/css/coreui.min.css'
  import { onMount } from 'svelte'
  import { auth, checkAuth } from './store.js'
  import SidebarLayout from './module/sidebar/Layout.svelte'
  import HeaderLayout from './module/header/Layout.svelte'
  import StatusLayout from './module/status/Layout.svelte'
  import AuthLayout from './module/auth/Layout.svelte'

  let loading = true

  onMount(async () => {
    await checkAuth()
    loading = false
  })
</script>

{#if loading}
  <div class="loading">
    <div class="spinner"></div>
  </div>
{:else if $auth.token}
  <!-- Blocco autenticato -->
  <div class="app">
    <aside class="sidebar">
      <SidebarLayout />
    </aside>
    <main class="content">
      <header class="header">
        <HeaderLayout />
      </header>
      <section class="context">
        <StatusLayout />
      </section>
    </main>
  </div>
{:else}
  <!-- Blocco non autenticato -->
  <AuthLayout />
{/if}

<style>
  .loading {
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #e0e0e0;
    border-top-color: #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .app {
    min-height: 100vh;
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 220px;
    height: 100vh;
    background: #2c3e50;
    overflow-y: auto;
  }

  .content {
    margin-left: 220px;
  }

  .header {
    height: 60px;
    background: white;
    border-bottom: 1px solid #ddd;
    overflow: visible;
    position: relative;
    z-index: 100;
    display: flex;
    align-items: center;
  }

  .context {
    background: #f5f6fa;
    min-height: calc(100vh - 60px);
  }
</style>
