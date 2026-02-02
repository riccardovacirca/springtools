import { writable, derived } from 'svelte/store'

export const auth = writable({
  isAuthenticated: false,
  user: null,
  token: null
})

// Navigazione globale con supporto percorsi annidati
function createModuleStore() {
  const { subscribe, set } = writable({ module: 'status', subPath: '', params: {} })

  // Inizializza dal path URL
  function initFromURL() {
    if (typeof window === 'undefined') return { module: 'status', subPath: '', params: {} }

    const path = window.location.pathname
    const parts = path.replace(/^\//, '').split('/').filter(Boolean)

    // Primo segmento è il modulo
    const module = parts[0] || 'status'

    // Valida che sia un modulo valido
    const validModules = ['status', 'chiamate', 'campagne', 'operatori', 'agenti', 'sedi', 'liste']
    const currentModule = validModules.includes(module) ? module : 'status'

    // Resto del path è il subPath
    const subPath = parts.slice(1).join('/')

    const state = { module: currentModule, subPath, params: {} }
    set(state)
    return state
  }

  // Naviga a un modulo/percorso
  function navigate(module, subPath = '', params = {}) {
    if (typeof window === 'undefined') return

    const state = { module, subPath, params }
    set(state)

    // Costruisci URL
    let newUrl = `/${module}`
    if (subPath) {
      newUrl += `/${subPath}`
    }

    // Aggiorna l'URL senza ricaricare la pagina
    if (window.location.pathname !== newUrl) {
      window.history.pushState(state, '', newUrl)
    }
  }

  // Gestisce il pulsante back/forward del browser
  if (typeof window !== 'undefined') {
    window.addEventListener('popstate', (event) => {
      if (event.state) {
        set(event.state)
      } else {
        initFromURL()
      }
    })
  }

  return {
    subscribe,
    navigate,
    initFromURL,
    set: (module) => navigate(module, '', {}) // Compatibilità
  }
}

export const currentModule = createModuleStore()

// Store derivato per il modulo corrente (primo livello)
export const currentModuleName = derived(
  currentModule,
  $currentModule => $currentModule.module
)

// Store derivato per il subPath
export const currentSubPath = derived(
  currentModule,
  $currentModule => $currentModule.subPath
)

// Sidebar toggle state for responsive design
export const sidebarVisible = writable(true)

// Check authentication status from localStorage
export async function checkAuth() {
  if (typeof localStorage === 'undefined') {
    auth.set({ isAuthenticated: false, user: null, token: null })
    return false
  }

  const savedToken = localStorage.getItem('auth_token')
  const savedUser = localStorage.getItem('auth_user')

  if (savedToken && savedUser) {
    try {
      const user = JSON.parse(savedUser)
      auth.set({
        isAuthenticated: true,
        user,
        token: savedToken
      })
      return true
    } catch (error) {
      console.error('Auth check error:', error)
    }
  }

  auth.set({ isAuthenticated: false, user: null, token: null })
  return false
}

// Logout function
export async function logout() {
  const currentToken = localStorage.getItem('auth_token')

  try {
    if (currentToken) {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${currentToken}` }
      })
    }
  } catch (error) {
    console.error('Logout error:', error)
  }

  // Pulisci localStorage
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')

  // Pulisci store globale
  auth.set({ isAuthenticated: false, user: null, token: null })

  // Naviga al modulo di autenticazione per mostrare il login
  currentModule.navigate('status', '')
}
