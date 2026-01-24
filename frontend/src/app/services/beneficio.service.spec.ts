import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { BeneficioService } from './beneficio.service';
import { BENEFICIOS_ENDPOINT } from '../config/api.config';
import { Beneficio, BeneficioPayload, TransferenciaPayload } from '../models/beneficio.model';

describe('BeneficioService', () => {
  let service: BeneficioService;
  let httpMock: HttpTestingController;

  const mockBeneficios: Beneficio[] = [
    { id: 1, nome: 'Vale Alimentação', descricao: 'Saldo inicial', valor: 2500, ativo: true, version: 0 },
    { id: 2, nome: 'Vale Transporte', descricao: 'Transporte mensal', valor: 500, ativo: true, version: 0 }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BeneficioService]
    });

    service = TestBed.inject(BeneficioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('listar', () => {
    it('should return a list of benefícios', () => {
      service.listar().subscribe((beneficios) => {
        expect(beneficios.length).toBe(2);
        expect(beneficios[0].nome).toBe('Vale Alimentação');
        expect(beneficios[0].valor).toBe(2500);
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      expect(req.request.method).toBe('GET');
      req.flush(mockBeneficios);
    });

    it('should normalize valor to number', () => {
      service.listar().subscribe((beneficios) => {
        expect(typeof beneficios[0].valor).toBe('number');
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush([{ ...mockBeneficios[0], valor: '2500.00' as unknown as number }]);
    });

    it('should handle empty list', () => {
      service.listar().subscribe((beneficios) => {
        expect(beneficios.length).toBe(0);
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush([]);
    });

    it('should handle HTTP error', () => {
      service.listar().subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('criar', () => {
    it('should create a new benefício', () => {
      const payload: BeneficioPayload = {
        nome: 'Novo Benefício',
        descricao: 'Descrição teste',
        valor: 1000,
        ativo: true
      };

      const expectedResponse: Beneficio = {
        id: 3,
        ...payload,
        version: 0
      };

      service.criar(payload).subscribe((beneficio) => {
        expect(beneficio.id).toBe(3);
        expect(beneficio.nome).toBe('Novo Benefício');
        expect(beneficio.valor).toBe(1000);
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.nome).toBe('Novo Benefício');
      expect(req.request.body.valor).toBe(1000);
      req.flush(expectedResponse);
    });

    it('should convert string valor to number in payload', () => {
      const payload: BeneficioPayload = {
        nome: 'Test',
        descricao: '',
        valor: '500' as unknown as number,
        ativo: true
      };

      service.criar(payload).subscribe();

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      expect(typeof req.request.body.valor).toBe('number');
      expect(req.request.body.valor).toBe(500);
      req.flush({ id: 1, ...payload, valor: 500 });
    });

    it('should handle creation error', () => {
      const payload: BeneficioPayload = {
        nome: '',
        descricao: '',
        valor: 100,
        ativo: true
      };

      service.criar(payload).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush('Validation Error', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('atualizar', () => {
    it('should update an existing benefício', () => {
      const payload: BeneficioPayload = {
        nome: 'Vale Alimentação Atualizado',
        descricao: 'Nova descrição',
        valor: 3000,
        ativo: true
      };

      const expectedResponse: Beneficio = {
        id: 1,
        ...payload,
        version: 1
      };

      service.atualizar(1, payload).subscribe((beneficio) => {
        expect(beneficio.id).toBe(1);
        expect(beneficio.nome).toBe('Vale Alimentação Atualizado');
        expect(beneficio.valor).toBe(3000);
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body.nome).toBe('Vale Alimentação Atualizado');
      req.flush(expectedResponse);
    });

    it('should handle update error when not found', () => {
      const payload: BeneficioPayload = {
        nome: 'Test',
        descricao: '',
        valor: 100,
        ativo: true
      };

      service.atualizar(999, payload).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/999`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('remover', () => {
    it('should delete a benefício', () => {
      service.remover(1).subscribe((result) => {
        expect(result).toBeNull();
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle delete error when not found', () => {
      service.remover(999).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/999`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('transferir', () => {
    it('should transfer value between benefícios', () => {
      const payload: TransferenciaPayload = {
        origemId: 1,
        destinoId: 2,
        valor: 500
      };

      service.transferir(payload).subscribe((message) => {
        expect(message).toBe('Transferência realizada com sucesso');
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush('Transferência realizada com sucesso');
    });

    it('should handle insufficient balance error', () => {
      const payload: TransferenciaPayload = {
        origemId: 1,
        destinoId: 2,
        valor: 100000
      };

      service.transferir(payload).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      req.flush('Saldo insuficiente', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle not found error', () => {
      const payload: TransferenciaPayload = {
        origemId: 999,
        destinoId: 2,
        valor: 100
      };

      service.transferir(payload).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      req.flush('Benefício não encontrado', { status: 404, statusText: 'Not Found' });
    });
  });
});
