<script>
  import { onMount } from "svelte";

  let logsData = [];
  let loading = true;
  let error = null;
  let num = 10;
  let off = 0;

  async function loadLogs() {
    loading = true;
    error = null;
    try {
      const response = await fetch(`/api/status/logs?num=${num}&off=${off}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      logsData = await response.json();
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  onMount(() => {
    loadLogs();
  });
</script>

<div class="status-section logs">
  <div class="section-header">
    <h3>📝 Recent Logs</h3>
    <button class="refresh-btn" on:click={loadLogs}>
      {loading ? "Loading..." : "Refresh"}
    </button>
  </div>

  {#if error}
    <div class="error">
      <strong>Errore:</strong> {error}
    </div>
  {:else if loading}
    <div class="loading">Caricamento log...</div>
  {:else if logsData && logsData.length > 0}
    <div class="logs-list">
      {#each logsData as log}
        <div class="log-item">
          <div class="log-header">
            <strong>ID:</strong> {log.id}
          </div>
          <div class="log-message">
            <strong>Message:</strong> {log.message}
          </div>
          <div class="log-time">
            <small>Created: {new Date(log.createdAt).toLocaleString("it-IT")}</small>
          </div>
        </div>
      {/each}
    </div>
  {:else}
    <p class="no-logs">Nessun log disponibile</p>
  {/if}
</div>

<style>
  .status-section {
    margin: 15px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #007bff;
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
    background: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: background 0.3s;
  }

  .refresh-btn:hover {
    background: #0056b3;
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

  .logs-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .log-item {
    padding: 15px;
    background: white;
    border-radius: 4px;
    border-left: 3px solid #007bff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }

  .log-header,
  .log-message {
    margin-bottom: 8px;
    color: #333;
  }

  .log-time {
    color: #666;
    font-size: 0.9rem;
  }

  .no-logs {
    padding: 20px;
    text-align: center;
    color: #666;
    font-style: italic;
  }
</style>
