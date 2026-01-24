/**
 * Determines API base URL based on environment.
 * In production (Docker), uses relative path proxied by nginx.
 * In development with ng serve, uses localhost:8080.
 */
function getApiBaseUrl(): string {
  // Check if running in browser
  if (typeof window !== 'undefined') {
    // Check if running via ng serve (default port 4200)
    if (window.location.port === '4200') {
      // Local development without Docker - direct backend access
      return 'http://localhost:8080/api/v1';
    }
    // All other cases (Docker/production) - use relative path for nginx proxy
    return '/api/v1';
  }
  // SSR/server-side fallback
  return '/api/v1';
}

/**
 * Centralizes API endpoints so HTTP services use a single source of truth.
 */
export const API_BASE_URL = getApiBaseUrl();
export const BENEFICIOS_ENDPOINT = `${API_BASE_URL}/beneficios`;
