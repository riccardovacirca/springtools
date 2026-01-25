<script>
    import { user, token, logout, loading } from './store.js';

    let sessions = $state([]);

    $effect(() => {
        loadSessions();
    });

    async function loadSessions() {
        try {
            const response = await fetch('/api/auth/sessions');
            if (response.ok) {
                sessions = await response.json();
            }
        } catch (err) {
            console.error('Errore caricamento sessioni:', err);
        }
    }

    async function handleLogout() {
        await logout();
    }

    function formatDateTime(dateStr) {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleString('it-IT');
    }
</script>

<div class="session-container">
    <div class="user-info">
        <h2>Benvenuto, {$user?.username}</h2>
        <p>Ruolo: <strong>{$user?.ruolo}</strong></p>
        <p>ID Utente: {$user?.id}</p>

        <button onclick={handleLogout} disabled={$loading}>
            {$loading ? 'Uscita...' : 'Logout'}
        </button>
    </div>

    <div class="sessions-section">
        <h3>Sessioni Attive</h3>
        <button class="refresh" onclick={loadSessions}>Aggiorna</button>

        {#if sessions.length === 0}
            <p>Nessuna sessione attiva.</p>
        {:else}
            <table>
                <thead>
                    <tr>
                        <th>Utente</th>
                        <th>Ruolo</th>
                        <th>Data Login</th>
                        <th>Token</th>
                    </tr>
                </thead>
                <tbody>
                    {#each sessions as session}
                        <tr class:current={session.token === $token}>
                            <td>{session.username}</td>
                            <td>{session.ruolo}</td>
                            <td>{formatDateTime(session.createdAt)}</td>
                            <td class="token">{session.token?.substring(0, 8)}...</td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        {/if}
    </div>
</div>

<style>
    .session-container { padding: 1rem; }
    .user-info {
        background: #f5f5f5;
        padding: 1.5rem;
        margin-bottom: 2rem;
    }
    .user-info h2 { margin: 0 0 0.5rem 0; }
    .user-info p { margin: 0.25rem 0; }
    .user-info button {
        margin-top: 1rem;
        padding: 0.5rem 1rem;
        background: #dc3545;
        color: white;
        border: none;
        cursor: pointer;
    }
    .user-info button:disabled {
        background: #ccc;
    }
    .sessions-section h3 { margin: 0 0 1rem 0; }
    .refresh {
        margin-bottom: 1rem;
        padding: 0.5rem 1rem;
        cursor: pointer;
    }
    table {
        width: 100%;
        border-collapse: collapse;
    }
    th, td {
        padding: 0.5rem;
        text-align: left;
        border-bottom: 1px solid #ddd;
    }
    th { background: #f5f5f5; }
    tr.current { background: #e3f2fd; }
    .token {
        font-family: monospace;
        font-size: 0.85rem;
        color: #666;
    }
</style>
