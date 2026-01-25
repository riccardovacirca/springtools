import { writable } from 'svelte/store'

export const auth = writable({
  isAuthenticated: false,
  user: null,
  token: null
})

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

  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')
  auth.set({ isAuthenticated: false, user: null, token: null })
}
