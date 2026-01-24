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
});
