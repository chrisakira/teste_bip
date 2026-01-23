import { Beneficio, BeneficioPayload, TransferenciaPayload } from './beneficio.model';

describe('Beneficio Model', () => {
  describe('Beneficio interface', () => {
    it('should create a valid Beneficio object', () => {
      const beneficio: Beneficio = {
        id: 1,
        nome: 'Vale Alimentação',
        descricao: 'Descrição do benefício',
        valor: 1000,
        ativo: true,
        version: 0
      };

      expect(beneficio.id).toBe(1);
      expect(beneficio.nome).toBe('Vale Alimentação');
      expect(beneficio.descricao).toBe('Descrição do benefício');
      expect(beneficio.valor).toBe(1000);
      expect(beneficio.ativo).toBeTrue();
      expect(beneficio.version).toBe(0);
    });

    it('should allow optional fields to be undefined', () => {
      const beneficio: Beneficio = {
        nome: 'Vale Transporte',
        valor: 500,
        ativo: true
      };

      expect(beneficio.id).toBeUndefined();
      expect(beneficio.descricao).toBeUndefined();
      expect(beneficio.version).toBeUndefined();
    });

    it('should allow null descricao', () => {
      const beneficio: Beneficio = {
        id: 2,
        nome: 'Plano de Saúde',
        descricao: null,
        valor: 2000,
        ativo: false
      };

      expect(beneficio.descricao).toBeNull();
    });
  });

  describe('BeneficioPayload type', () => {
    it('should create a valid BeneficioPayload object', () => {
      const payload: BeneficioPayload = {
        nome: 'Novo Benefício',
        descricao: 'Nova descrição',
        valor: 750,
        ativo: true
      };

      expect(payload.nome).toBe('Novo Benefício');
      expect(payload.descricao).toBe('Nova descrição');
      expect(payload.valor).toBe(750);
      expect(payload.ativo).toBeTrue();

      // Should not have id or version (they are omitted)
      expect((payload as Beneficio).id).toBeUndefined();
      expect((payload as Beneficio).version).toBeUndefined();
    });

    it('should allow optional descricao', () => {
      const payload: BeneficioPayload = {
        nome: 'Benefício Simples',
        valor: 100,
        ativo: true
      };

      expect(payload.descricao).toBeUndefined();
    });
  });

  describe('TransferenciaPayload interface', () => {
    it('should create a valid TransferenciaPayload object', () => {
      const payload: TransferenciaPayload = {
        origemId: 1,
        destinoId: 2,
        valor: 500
      };

      expect(payload.origemId).toBe(1);
      expect(payload.destinoId).toBe(2);
      expect(payload.valor).toBe(500);
    });

    it('should require all fields', () => {
      const payload: TransferenciaPayload = {
        origemId: 10,
        destinoId: 20,
        valor: 1000.50
      };

      expect(payload.origemId).toBeDefined();
      expect(payload.destinoId).toBeDefined();
      expect(payload.valor).toBeDefined();
    });
  });
});
