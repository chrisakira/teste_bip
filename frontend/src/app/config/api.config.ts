const DEFAULT_API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Centralizes API endpoints so HTTP services use a single source of truth.
 * Adjust this constant if the backend base URL changes.
 */
export const API_BASE_URL = DEFAULT_API_BASE_URL;
export const BENEFICIOS_ENDPOINT = `${API_BASE_URL}/beneficios`;
