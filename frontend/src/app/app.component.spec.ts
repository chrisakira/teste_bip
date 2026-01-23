import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AppComponent } from './app.component';
import { BENEFICIOS_ENDPOINT } from './config/api.config';

describe('AppComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent, HttpClientTestingModule]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create the dashboard and load benefícios', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const request = httpMock.expectOne(BENEFICIOS_ENDPOINT);
    request.flush([
      { id: 1, nome: 'Vale alimentação', descricao: 'Saldo inicial', valor: 2500, ativo: true }
    ]);

    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.beneficios.length).toBe(1);
  });

  it('should keep form invalid when required fields are missing', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    httpMock.expectOne(BENEFICIOS_ENDPOINT).flush([]);

    const component = fixture.componentInstance;
    component.beneficioForm.controls.nome.setValue('');
    component.salvarBeneficio();

    expect(component.beneficioForm.invalid).toBeTrue();
  });
});
