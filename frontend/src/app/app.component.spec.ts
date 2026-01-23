import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { AppComponent } from './app.component';
import { BENEFICIOS_ENDPOINT } from './config/api.config';
import { Beneficio } from './models/beneficio.model';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let httpMock: HttpTestingController;

  const mockBeneficios: Beneficio[] = [
    { id: 1, nome: 'Vale Alimentação', descricao: 'Saldo inicial', valor: 2500, ativo: true, version: 0 },
    { id: 2, nome: 'Vale Transporte', descricao: 'Transporte mensal', valor: 500, ativo: true, version: 0 },
    { id: 3, nome: 'Plano de Saúde', descricao: 'Assistência médica', valor: 1000, ativo: false, version: 0 }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent, HttpClientTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Initialization', () => {
    it('should create the component', () => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);
      expect(component).toBeTruthy();
    });

    it('should load benefícios on init', () => {
      fixture.detectChanges();
      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush(mockBeneficios);

      expect(component.beneficios.length).toBe(3);
      expect(component.loading).toBeFalse();
    });

    it('should set loading to true while fetching', () => {
      fixture.detectChanges();
      expect(component.loading).toBeTrue();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);
      expect(component.loading).toBeFalse();
    });

    it('should handle error when loading benefícios fails', () => {
      spyOn(console, 'error');
      fixture.detectChanges();

      const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
      req.flush('Error', { status: 500, statusText: 'Server Error' });

      expect(component.loading).toBeFalse();
      expect(component.feedback?.type).toBe('error');
      expect(component.feedback?.message).toContain('Não foi possível carregar');
    });
  });

  describe('Computed Properties', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);
    });

    it('should calculate totalAtivo correctly', () => {
      expect(component.totalAtivo).toBe(2); // 2 active beneficios
    });

    it('should calculate saldoTotal correctly', () => {
      expect(component.saldoTotal).toContain('4.000'); // 2500 + 500 + 1000 = 4000
    });

    it('should return false for estaEditando when not editing', () => {
      expect(component.estaEditando).toBeFalse();
    });

    it('should return true for estaEditando when editing', () => {
      component.editingId = 1;
      expect(component.estaEditando).toBeTrue();
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);
    });

    it('should have invalid form when nome is empty', () => {
      component.beneficioForm.controls.nome.setValue('');
      expect(component.beneficioForm.invalid).toBeTrue();
    });

    it('should have invalid form when nome exceeds 100 characters', () => {
      component.beneficioForm.controls.nome.setValue('a'.repeat(101));
      expect(component.beneficioForm.controls.nome.hasError('maxlength')).toBeTrue();
    });

    it('should have invalid form when valor is 0 or negative', () => {
      component.beneficioForm.controls.nome.setValue('Test');
      component.beneficioForm.controls.valor.setValue(0);
      expect(component.beneficioForm.invalid).toBeTrue();

      component.beneficioForm.controls.valor.setValue(-10);
      expect(component.beneficioForm.invalid).toBeTrue();
    });

    it('should have valid form with correct data', () => {
      component.beneficioForm.setValue({
        nome: 'Novo Benefício',
        descricao: 'Descrição teste',
        valor: 100,
        ativo: true
      });
      expect(component.beneficioForm.valid).toBeTrue();
    });

    it('should not save when form is invalid', () => {
      component.beneficioForm.controls.nome.setValue('');
      component.salvarBeneficio();

      expect(component.beneficioForm.touched).toBeTrue();
      httpMock.expectNone(`${BENEFICIOS_ENDPOINT}`);
    });
  });

  describe('CRUD Operations', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);
    });

    describe('Create', () => {
      it('should create a new benefício', () => {
        component.beneficioForm.setValue({
          nome: 'Novo Benefício',
          descricao: 'Descrição',
          valor: 150,
          ativo: true
        });

        component.salvarBeneficio();
        expect(component.saving).toBeTrue();

        const createReq = httpMock.expectOne(BENEFICIOS_ENDPOINT);
        expect(createReq.request.method).toBe('POST');
        createReq.flush({ id: 4, nome: 'Novo Benefício', descricao: 'Descrição', valor: 150, ativo: true });

        // Should reload after create
        httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);

        expect(component.saving).toBeFalse();
        expect(component.feedback?.type).toBe('success');
      });

      it('should handle error when creating fails', () => {
        spyOn(console, 'error');
        component.beneficioForm.setValue({
          nome: 'Novo Benefício',
          descricao: 'Descrição',
          valor: 150,
          ativo: true
        });

        component.salvarBeneficio();

        const req = httpMock.expectOne(BENEFICIOS_ENDPOINT);
        req.flush('Error', { status: 400, statusText: 'Bad Request' });

        expect(component.saving).toBeFalse();
        expect(component.feedback?.type).toBe('error');
      });
    });

    describe('Update', () => {
      it('should update an existing benefício', () => {
        component.iniciarEdicao(mockBeneficios[0]);
        expect(component.editingId).toBe(1);
        expect(component.beneficioForm.value.nome).toBe('Vale Alimentação');

        component.beneficioForm.controls.nome.setValue('Vale Alimentação Atualizado');
        component.salvarBeneficio();

        const updateReq = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/1`);
        expect(updateReq.request.method).toBe('PUT');
        updateReq.flush({ ...mockBeneficios[0], nome: 'Vale Alimentação Atualizado' });

        httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);

        expect(component.editingId).toBeNull();
        expect(component.feedback?.type).toBe('success');
      });

      it('should not start editing if benefício has no id', () => {
        const beneficioSemId: Beneficio = { nome: 'Test', valor: 100, ativo: true };
        component.iniciarEdicao(beneficioSemId);
        expect(component.editingId).toBeNull();
      });
    });

    describe('Delete', () => {
      it('should delete a benefício when confirmed', () => {
        spyOn(window, 'confirm').and.returnValue(true);

        component.removerBeneficio(mockBeneficios[0]);

        const deleteReq = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/1`);
        expect(deleteReq.request.method).toBe('DELETE');
        deleteReq.flush(null);

        httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios.slice(1));

        expect(component.feedback?.type).toBe('success');
      });

      it('should not delete when user cancels confirmation', () => {
        spyOn(window, 'confirm').and.returnValue(false);

        component.removerBeneficio(mockBeneficios[0]);

        httpMock.expectNone(`${BENEFICIOS_ENDPOINT}/1`);
      });

      it('should not delete if benefício has no id', () => {
        const beneficioSemId: Beneficio = { nome: 'Test', valor: 100, ativo: true };
        component.removerBeneficio(beneficioSemId);
        httpMock.expectNone(`${BENEFICIOS_ENDPOINT}/undefined`);
      });

      it('should handle error when deleting fails', () => {
        spyOn(window, 'confirm').and.returnValue(true);
        spyOn(console, 'error');

        component.removerBeneficio(mockBeneficios[0]);

        const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/1`);
        req.flush('Error', { status: 500, statusText: 'Server Error' });

        expect(component.feedback?.type).toBe('error');
      });
    });
  });

  describe('Transfer Operations', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);
    });

    it('should transfer between benefícios successfully', () => {
      component.transferForm.setValue({
        origemId: '1',
        destinoId: '2',
        valor: 100
      });

      component.transferir();
      expect(component.transferProcessing).toBeTrue();

      const transferReq = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      expect(transferReq.request.method).toBe('POST');
      expect(transferReq.request.body).toEqual({
        origemId: 1,
        destinoId: 2,
        valor: 100
      });
      transferReq.flush('Transferência realizada com sucesso');

      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);

      expect(component.transferProcessing).toBeFalse();
      expect(component.feedback?.type).toBe('success');
    });

    it('should show error when origem equals destino', () => {
      component.transferForm.setValue({
        origemId: '1',
        destinoId: '1',
        valor: 100
      });

      component.transferir();

      expect(component.feedback?.type).toBe('error');
      expect(component.feedback?.message).toContain('diferentes');
      httpMock.expectNone(`${BENEFICIOS_ENDPOINT}/transferir`);
    });

    it('should not transfer when form is invalid', () => {
      component.transferForm.setValue({
        origemId: '',
        destinoId: '2',
        valor: 100
      });

      component.transferir();

      expect(component.transferForm.touched).toBeTrue();
      httpMock.expectNone(`${BENEFICIOS_ENDPOINT}/transferir`);
    });

    it('should handle transfer error', () => {
      spyOn(console, 'error');
      component.transferForm.setValue({
        origemId: '1',
        destinoId: '2',
        valor: 10000
      });

      component.transferir();

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      req.flush({ error: 'Saldo insuficiente' }, { status: 400, statusText: 'Bad Request' });

      expect(component.transferProcessing).toBeFalse();
      expect(component.feedback?.type).toBe('error');
    });

    it('should use default message when transfer succeeds without message', () => {
      component.transferForm.setValue({
        origemId: '1',
        destinoId: '2',
        valor: 100
      });

      component.transferir();

      const req = httpMock.expectOne(`${BENEFICIOS_ENDPOINT}/transferir`);
      req.flush('');

      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);

      expect(component.feedback?.message).toBe('Transferência concluída!');
    });
  });

  describe('Edit Mode', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush(mockBeneficios);
    });

    it('should cancel editing and reset form', () => {
      component.iniciarEdicao(mockBeneficios[0]);
      expect(component.editingId).toBe(1);

      component.cancelarEdicao();

      expect(component.editingId).toBeNull();
      expect(component.beneficioForm.value.nome).toBe('');
      expect(component.beneficioForm.value.valor).toBe(0);
      expect(component.beneficioForm.value.ativo).toBeTrue();
    });

    it('should populate form when starting edit', () => {
      component.iniciarEdicao(mockBeneficios[0]);

      expect(component.beneficioForm.value).toEqual({
        nome: 'Vale Alimentação',
        descricao: 'Saldo inicial',
        valor: 2500,
        ativo: true
      });
    });

    it('should handle null descricao when editing', () => {
      const beneficioSemDescricao: Beneficio = {
        id: 5,
        nome: 'Sem Descrição',
        descricao: null,
        valor: 100,
        ativo: true
      };
      component.iniciarEdicao(beneficioSemDescricao);

      expect(component.beneficioForm.value.descricao).toBe('');
    });
  });

  describe('Utility Methods', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);
    });

    it('should format currency correctly', () => {
      expect(component.formatCurrency(1000)).toContain('1.000');
      expect(component.formatCurrency(0)).toContain('0');
      expect(component.formatCurrency(null)).toContain('0');
      expect(component.formatCurrency(undefined)).toContain('0');
    });

    it('should track by id', () => {
      const beneficio: Beneficio = { id: 5, nome: 'Test', valor: 100, ativo: true };
      expect(component.trackById(0, beneficio)).toBe(5);
    });

    it('should return undefined for trackById when no id', () => {
      const beneficio: Beneficio = { nome: 'Test', valor: 100, ativo: true };
      expect(component.trackById(0, beneficio)).toBeUndefined();
    });
  });

  describe('Feedback Timeout', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);
    });

    it('should clear feedback after timeout', fakeAsync(() => {
      component.beneficioForm.setValue({
        nome: 'Test',
        descricao: '',
        valor: 100,
        ativo: true
      });

      component.salvarBeneficio();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush({ id: 1, nome: 'Test', valor: 100, ativo: true });
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

      expect(component.feedback).not.toBeNull();

      tick(5000);

      expect(component.feedback).toBeNull();
    }));

    it('should clear previous timeout when setting new feedback', fakeAsync(() => {
      component.beneficioForm.setValue({
        nome: 'Test1',
        descricao: '',
        valor: 100,
        ativo: true
      });

      component.salvarBeneficio();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush({ id: 1, nome: 'Test1', valor: 100, ativo: true });
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

      tick(2000);

      component.beneficioForm.setValue({
        nome: 'Test2',
        descricao: '',
        valor: 200,
        ativo: true
      });

      component.salvarBeneficio();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush({ id: 2, nome: 'Test2', valor: 200, ativo: true });
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

      tick(3000);
      expect(component.feedback).not.toBeNull();

      tick(2000);
      expect(component.feedback).toBeNull();
    }));
  });

  describe('ngOnDestroy', () => {
    it('should clear timeout on destroy', fakeAsync(() => {
      fixture.detectChanges();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

      component.beneficioForm.setValue({
        nome: 'Test',
        descricao: '',
        valor: 100,
        ativo: true
      });

      component.salvarBeneficio();
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush({ id: 1, nome: 'Test', valor: 100, ativo: true });
      httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

      const clearTimeoutSpy = spyOn(window, 'clearTimeout').and.callThrough();

      component.ngOnDestroy();

      expect(clearTimeoutSpy).toHaveBeenCalled();
    }));
  });

  describe('Skeleton Loading', () => {
    it('should have skeleton rows for loading state', () => {
      expect(component.skeletonRows.length).toBe(4);
    });
  });
});
