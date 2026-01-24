export interface Beneficio {
  id?: number;
  nome: string;
  descricao?: string | null;
  valor: number;
  ativo: boolean;
  version?: number;
}

export type BeneficioPayload = Omit<Beneficio, 'id' | 'version'>;

export interface TransferenciaPayload {
  origemId: number;
  destinoId: number;
  valor: number;
}
