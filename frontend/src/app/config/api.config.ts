/**
 * Determines API base URL based on environment.
 * In production (Docker), uses relative path proxied by nginx or same-origin backend.
 * In development, uses localhost:8080.
 */
function getApiBaseUrl(): string {
  // Check if running in browser
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname;
    // If not localhost, use the production backend URL
    if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
      // Use same origin with /api path (backend should be accessible)
      return `${window.location.protocol}//${window.location.host}/api/v1`;
    }
  }
  // Default for local development
  return 'http://localhost:8080/api/v1';
}

/**
 * Centralizes API endpoints so HTTP services use a single source of truth.
 */
export const API_BASE_URL = getApiBaseUrl();
export const BENEFICIOS_ENDPOINT = `${API_BASE_URL}/beneficios`;
