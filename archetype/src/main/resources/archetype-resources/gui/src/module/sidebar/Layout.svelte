<script>
  import { contextSidebar, contextSidebarProps } from "./store.js";
</script>

<div class="sidebar-layout">
  <div class="sidebar-header">
    <h3>CRM</h3>
  </div>

  <nav class="sidebar-static">
    <button class="active">Status</button>
  </nav>

  {#if $contextSidebar}
    <div class="sidebar-dynamic">
      <svelte:component this={$contextSidebar} {...$contextSidebarProps} />
    </div>
  {/if}
</div>

<style>
  .sidebar-layout {
    height: 100%;
    display: flex;
    flex-direction: column;
    color: white;
  }

  .sidebar-header {
    padding: 1.5rem 1rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  .sidebar-header h3 {
    margin: 0;
    font-size: 1.25rem;
  }

  .sidebar-static {
    padding: 1rem 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  .sidebar-static button {
    display: block;
    width: 100%;
    padding: 0.75rem 1rem;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.7);
    text-align: left;
    cursor: pointer;
  }
  .sidebar-static button:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
  .sidebar-static button.active {
    background: rgba(255, 255, 255, 0.15);
    color: white;
    border-left: 3px solid #3498db;
  }

  .sidebar-dynamic {
    flex: 1;
    padding: 1rem 0;
  }
  .dynamic-title {
    padding: 0.5rem 1rem;
    font-size: 0.75rem;
    text-transform: uppercase;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 0.05em;
  }
  .dynamic-nav button {
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
  .dynamic-nav button:hover {
    background: rgba(255, 255, 255, 0.05);
    color: white;
  }
  .dynamic-nav button.active {
    color: #3498db;
  }
</style>
