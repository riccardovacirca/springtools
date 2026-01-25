// src/modules/auth/store.js
import { writable } from 'svelte/store';

export const user = writable(null);
export const token = writable(null);
export const loading = writable(false);
export const error = writable(null);

// Inizializza da localStorage
if (typeof localStorage !== 'undefined') {
    const savedToken = localStorage.getItem('auth_token');
    const savedUser = localStorage.getItem('auth_user');
    if (savedToken && savedUser) {
        token.set(savedToken);
        user.set(JSON.parse(savedUser));
    }
}

export async function login(username, password) {
    loading.set(true);
    error.set(null);

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('Credenziali non valide');
        }

        const data = await response.json();

        token.set(data.token);
        user.set({
            id: data.userId,
            username: data.username,
            ruolo: data.ruolo
        });

        // Salva in localStorage
        localStorage.setItem('auth_token', data.token);
        localStorage.setItem('auth_user', JSON.stringify({
            id: data.userId,
            username: data.username,
            ruolo: data.ruolo
        }));

        return data;
    } catch (err) {
        error.set(err.message);
        throw err;
    } finally {
        loading.set(false);
    }
}

export async function logout() {
    loading.set(true);

    try {
        const currentToken = localStorage.getItem('auth_token');
        if (currentToken) {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${currentToken}` }
            });
        }
    } catch (err) {
        console.error('Errore logout:', err);
    } finally {
        token.set(null);
        user.set(null);
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
        loading.set(false);
    }
}

export async function checkSession() {
    const currentToken = localStorage.getItem('auth_token');
    if (!currentToken) {
        return false;
    }

    try {
        const response = await fetch('/api/auth/session', {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        if (!response.ok) {
            await logout();
            return false;
        }

        return true;
    } catch (err) {
        await logout();
        return false;
    }
}

export function isAuthenticated() {
    return localStorage.getItem('auth_token') !== null;
}

export function isAdmin() {
    const savedUser = localStorage.getItem('auth_user');
    if (!savedUser) return false;
    const u = JSON.parse(savedUser);
    return u.ruolo === 'ADMIN';
}
