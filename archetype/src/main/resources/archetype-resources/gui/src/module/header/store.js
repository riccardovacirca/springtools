import { writable } from 'svelte/store';

// Componente header del modulo attivo
export const contextHeader = writable(null);

// Props da passare al componente header
export const contextHeaderProps = writable({});

// Setta il componente header del modulo
export function setContextHeader(component, props = {}) {
  contextHeader.set(component);
  contextHeaderProps.set(props);
}

// Rimuove il componente header
export function clearContextHeader() {
  contextHeader.set(null);
  contextHeaderProps.set({});
}
