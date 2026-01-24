<script>
  import HealthLayout from "./health/HealthLayout.svelte";
  import LogsLayout from "./logs/LogsLayout.svelte";
</script>

<div class="status-container">
  <header class="status-header">
    <h1>Spring Boot Status Monitor</h1>
    <p>Verifica lo stato dell'applicazione e visualizza i log recenti</p>
  </header>
  <main class="content">
    <HealthLayout />
    <LogsLayout />
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
