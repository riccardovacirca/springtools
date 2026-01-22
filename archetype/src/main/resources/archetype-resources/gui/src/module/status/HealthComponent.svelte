<script>
  import { onMount } from "svelte";

  let healthData = null;
  let loading = true;
  let error = null;

  async function loadHealth() {
    loading = true;
    error = null;
    try {
      const response = await fetch("/api/status/health");
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      healthData = await response.json();
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  onMount(() => {
    loadHealth();
  });
</script>

<div class="status-section health">
  <div class="section-header">
    <h3>🏥 Health Status</h3>
    <button class="refresh-btn" on:click={loadHealth}>
      {loading ? "Loading..." : "Refresh"}
    </button>
  </div>

  {#if error}
    <div class="error">
      <strong>Errore:</strong> {error}
    </div>
  {:else if loading}
    <div class="loading">Caricamento...</div>
  {:else if healthData}
    <div class="health-content">
      <span class="label">Status:</span>
      <span class="value status-badge">{healthData.status}</span>
    </div>
  {/if}
</div>

<style>
  .status-section {
    margin: 15px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #28a745;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .section-header h3 {
    margin: 0;
    font-size: 1.3rem;
    color: #333;
  }

  .refresh-btn {
    padding: 8px 16px;
    background: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: background 0.3s;
  }

  .refresh-btn:hover {
    background: #218838;
  }

  .loading,
  .error {
    padding: 15px;
    border-radius: 4px;
    text-align: center;
  }

  .loading {
    background: #e7f3ff;
    color: #666;
  }

  .error {
    background: #fee;
    color: #c33;
  }

  .health-content {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
  }

  .health-content .label {
    font-weight: 600;
    font-size: 1.1rem;
    color: #555;
  }

  .status-badge {
    padding: 8px 20px;
    background: #28a745;
    color: white;
    border-radius: 6px;
    font-weight: 600;
    font-size: 1.1rem;
    text-transform: uppercase;
  }
</style>
