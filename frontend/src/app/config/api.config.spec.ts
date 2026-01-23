import { API_BASE_URL, BENEFICIOS_ENDPOINT } from './api.config';

describe('API Config', () => {
  describe('API_BASE_URL', () => {
    it('should be defined', () => {
      expect(API_BASE_URL).toBeDefined();
    });

    it('should be a string', () => {
      expect(typeof API_BASE_URL).toBe('string');
    });

    it('should contain /api/v1', () => {
      expect(API_BASE_URL).toContain('/api/v1');
    });

    it('should start with http or https', () => {
      expect(API_BASE_URL).toMatch(/^https?:\/\//);
    });
  });

  describe('BENEFICIOS_ENDPOINT', () => {
    it('should be defined', () => {
      expect(BENEFICIOS_ENDPOINT).toBeDefined();
    });

    it('should be based on API_BASE_URL', () => {
      expect(BENEFICIOS_ENDPOINT).toContain(API_BASE_URL);
    });

    it('should end with /beneficios', () => {
      expect(BENEFICIOS_ENDPOINT).toMatch(/\/beneficios$/);
    });

    it('should have correct format', () => {
      expect(BENEFICIOS_ENDPOINT).toBe(`${API_BASE_URL}/beneficios`);
    });
  });

  describe('URL format validation', () => {
    it('API_BASE_URL should be a valid URL', () => {
      expect(() => new URL(API_BASE_URL)).not.toThrow();
    });

    it('BENEFICIOS_ENDPOINT should be a valid URL', () => {
      expect(() => new URL(BENEFICIOS_ENDPOINT)).not.toThrow();
    });
  });
});
