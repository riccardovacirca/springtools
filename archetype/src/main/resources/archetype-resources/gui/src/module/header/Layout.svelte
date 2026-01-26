<script>
  import { contextHeader, contextHeaderProps } from './store.js';
  import { auth, logout } from '../../store.js';

  let contextDropdownOpen = $state(false);
  let userDropdownOpen = $state(false);

  let contextButtonElement = $state(null);
  let userButtonElement = $state(null);

  let contextDropdownPosition = $derived(getDropdownPosition(contextButtonElement));
  let userDropdownPosition = $derived(getDropdownPosition(userButtonElement));

  function toggleContextDropdown() {
    contextDropdownOpen = !contextDropdownOpen;
  }

  function toggleUserDropdown() {
    userDropdownOpen = !userDropdownOpen;
  }

  function handleLogout() {
    logout();
  }

  function closeContextDropdown() {
    contextDropdownOpen = false;
  }

  function closeUserDropdown() {
    userDropdownOpen = false;
  }

  // Chiudi dropdown quando si clicca fuori
  function handleClickOutside(event) {
    if (contextDropdownOpen && !event.target.closest('.context-dropdown-container') && contextButtonElement && !contextButtonElement.contains(event.target)) {
      closeContextDropdown();
    }
    if (userDropdownOpen && !event.target.closest('.user-dropdown-container') && userButtonElement && !userButtonElement.contains(event.target)) {
      closeUserDropdown();
    }
  }

  function getDropdownPosition(buttonElement) {
    console.log('getDropdownPosition called, buttonElement:', buttonElement);
    if (!buttonElement) {
      console.log('No button element, using defaults');
      return { top: '60px', right: '20px' };
    }
    const rect = buttonElement.getBoundingClientRect();
    const pos = {
      top: `${rect.bottom + 8}px`,
      right: `${window.innerWidth - rect.right}px`
    };
    console.log('Calculated position:', pos);
    return pos;
  }
</script>

<svelte:window onclick={handleClickOutside} />

<div class="header-layout">
  <!-- Menu contestuale dropdown del modulo -->
  {#if $contextHeader}
    {@const ContextComponent = $contextHeader}
    <div class="context-dropdown-container">
      <button
        bind:this={contextButtonElement}
        class="context-dropdown-trigger"
        onclick={toggleContextDropdown}
      >
        <ContextComponent {...$contextHeaderProps} />
        <svg class="dropdown-icon" class:open={contextDropdownOpen} width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M2 4L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  {/if}

  <!-- Menu user dropdown (estrema destra) -->
  {#if $auth.user}
    <div class="user-dropdown-container">
      <button
        bind:this={userButtonElement}
        class="user-dropdown-trigger"
        onclick={toggleUserDropdown}
      >
        <div class="user-avatar">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="2"/>
            <circle cx="10" cy="8" r="3" fill="currentColor"/>
            <path d="M4 17c0-3 2.5-5 6-5s6 2 6 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="user-name">{$auth.user.username}</span>
        <svg class="dropdown-icon" class:open={userDropdownOpen} width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M2 4L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  {/if}

  <!-- Dropdown menus fuori dai container -->
  {#if contextDropdownOpen && $contextHeader}
    {@const ContextComponent = $contextHeader}
    <div style="position: fixed; top: {contextDropdownPosition.top}; right: {contextDropdownPosition.right}; z-index: 10000; min-width: 200px; background: white; border: 1px solid #ddd; border-radius: 6px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); overflow: hidden;">
      <ContextComponent
        {...$contextHeaderProps}
        isDropdown={true}
        onClose={closeContextDropdown}
      />
    </div>
  {/if}

  {#if userDropdownOpen}
    <div style="position: fixed; top: {userDropdownPosition.top}; right: {userDropdownPosition.right}; z-index: 10000; min-width: 240px; background: white; border: 1px solid #ddd; border-radius: 6px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); overflow: hidden;">
      <div style="padding: 1rem; background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <div style="font-weight: 600; color: #2c3e50; font-size: 0.95rem;">{$auth.user.username}</div>
        <div style="color: #6c757d; font-size: 0.8rem; margin-top: 0.25rem;">{$auth.user.ruolo}</div>
      </div>
      <div style="height: 1px; background: #e9ecef; margin: 0.25rem 0;"></div>
      <button style="display: flex; align-items: center; gap: 0.75rem; width: 100%; padding: 0.75rem 1rem; background: none; border: none; text-align: left; cursor: pointer; font-size: 0.9rem; color: #495057;" onclick={() => { closeUserDropdown(); }}>
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
          <circle cx="8" cy="7" r="2" fill="currentColor"/>
          <path d="M4 13c0-2 1.5-3 4-3s4 1 4 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        Profilo
      </button>
      <button style="display: flex; align-items: center; gap: 0.75rem; width: 100%; padding: 0.75rem 1rem; background: none; border: none; text-align: left; cursor: pointer; font-size: 0.9rem; color: #495057;" onclick={() => { closeUserDropdown(); }}>
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
          <path d="M8 5v3l2 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        Impostazioni
      </button>
      <div style="height: 1px; background: #e9ecef; margin: 0.25rem 0;"></div>
      <button style="display: flex; align-items: center; gap: 0.75rem; width: 100%; padding: 0.75rem 1rem; background: none; border: none; text-align: left; cursor: pointer; font-size: 0.9rem; color: #dc3545;" onclick={() => { closeUserDropdown(); handleLogout(); }}>
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="color: #dc3545;">
          <path d="M6 14H3a1 1 0 01-1-1V3a1 1 0 011-1h3M11 11l3-3-3-3M14 8H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Logout
      </button>
    </div>
  {/if}
</div>

<style>
  .header-layout {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 1rem;
    padding: 0 1.5rem;
    box-sizing: border-box;
  }

  /* Context Dropdown (Menu modulo) */
  .context-dropdown-container {
    position: relative;
  }

  .context-dropdown-trigger {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    background: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.2s;
  }

  .context-dropdown-trigger:hover {
    background: #f8f9fa;
    border-color: #bbb;
  }

  /* User Dropdown */
  .user-dropdown-container {
    position: relative;
  }

  .user-dropdown-trigger {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.4rem 0.75rem;
    background: white;
    border: 1px solid #ddd;
    border-radius: 20px;
    cursor: pointer;
    font-size: 0.875rem;
    transition: all 0.2s;
  }

  .user-dropdown-trigger:hover {
    background: #f8f9fa;
    border-color: #bbb;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #e3f2fd;
    color: #1976d2;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-name {
    font-weight: 500;
    color: #2c3e50;
  }

  .dropdown-icon {
    transition: transform 0.2s;
    color: #6c757d;
  }

  .dropdown-icon.open {
    transform: rotate(180deg);
  }

  /* Dropdown Menu (comune) */
  .dropdown-menu {
    position: absolute;
    top: calc(100% + 0.5rem);
    right: 0;
    min-width: 200px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    overflow: hidden;
  }

  /* User Menu Specifico */
  .user-menu {
    min-width: 240px;
  }

  .user-menu-header {
    padding: 1rem;
    background: #f8f9fa;
    border-bottom: 1px solid #e9ecef;
  }

  .user-menu-name {
    font-weight: 600;
    color: #2c3e50;
    font-size: 0.95rem;
  }

  .user-menu-role {
    color: #6c757d;
    font-size: 0.8rem;
    margin-top: 0.25rem;
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    width: 100%;
    padding: 0.75rem 1rem;
    background: none;
    border: none;
    text-align: left;
    cursor: pointer;
    font-size: 0.9rem;
    color: #495057;
    transition: background 0.15s;
  }

  .dropdown-item:hover {
    background: #f8f9fa;
  }

  .dropdown-item svg {
    flex-shrink: 0;
    opacity: 0.7;
  }

  .dropdown-item:hover svg {
    opacity: 1;
  }

  .logout-item {
    color: #dc3545;
  }

  .logout-item:hover {
    background: #fff5f5;
  }

  .logout-item svg {
    color: #dc3545;
  }

  .dropdown-divider {
    height: 1px;
    background: #e9ecef;
    margin: 0.25rem 0;
  }
</style>
