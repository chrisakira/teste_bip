import { routes } from './app.routes';

describe('AppRoutes', () => {
  it('should have routes defined', () => {
    expect(routes).toBeDefined();
    expect(Array.isArray(routes)).toBeTrue();
  });

  it('should be an empty array (single page application)', () => {
    expect(routes.length).toBe(0);
  });
});
