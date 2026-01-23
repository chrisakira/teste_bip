import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { BENEFICIOS_ENDPOINT } from '../config/api.config';
import { Beneficio, BeneficioPayload, TransferenciaPayload } from '../models/beneficio.model';

@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Beneficio[]> {
    return this.http
      .get<Beneficio[]>(BENEFICIOS_ENDPOINT)
      .pipe(map((beneficios) => beneficios.map((item) => this.normalize(item))));
  }

  criar(payload: BeneficioPayload): Observable<Beneficio> {
    return this.http
      .post<Beneficio>(BENEFICIOS_ENDPOINT, this.preparePayload(payload))
      .pipe(map((item) => this.normalize(item)));
  }

  atualizar(id: number, payload: BeneficioPayload): Observable<Beneficio> {
    return this.http
      .put<Beneficio>(`${BENEFICIOS_ENDPOINT}/${id}`, this.preparePayload(payload))
      .pipe(map((item) => this.normalize(item)));
  }

  remover(id: number): Observable<void> {
    return this.http.delete<void>(`${BENEFICIOS_ENDPOINT}/${id}`);
  }

  transferir(payload: TransferenciaPayload): Observable<string> {
    return this.http.post(`${BENEFICIOS_ENDPOINT}/transferir`, payload, {
      responseType: 'text' as const
    });
  }

  private preparePayload(payload: BeneficioPayload): BeneficioPayload {
    return {
      ...payload,
      valor: Number(payload.valor)
    };
  }

  private normalize(item: Beneficio): Beneficio {
    return {
      ...item,
      valor: Number(item.valor)
    };
  }
}
