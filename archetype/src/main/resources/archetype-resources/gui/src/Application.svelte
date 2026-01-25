<script>
  import { onMount } from 'svelte'
  import { auth, checkAuth } from './store.js'
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
    <p>Caricamento...</p>
  </div>
{:else if $auth.token}
  <!-- Blocco autenticato -->
  <div class="app-container">
    <StatusLayout />
  </div>
{:else}
  <!-- Blocco non autenticato -->
  <AuthLayout />
{/if}

<style>
  .loading {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 1rem;
  }

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #e0e0e0;
    border-top-color: #007bff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .app-container {
    min-height: 100vh;
  }
</style>
