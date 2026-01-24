import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Beneficio, BeneficioPayload } from './models/beneficio.model';
import { BeneficioService } from './services/beneficio.service';

type FeedbackState = { type: 'success' | 'error'; message: string } | null;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  private readonly service = inject(BeneficioService);
  private readonly fb = inject(FormBuilder);

  beneficios: Beneficio[] = [];
  readonly skeletonRows = Array.from({ length: 4 });
  loading = false;
  saving = false;
  transferProcessing = false;
  editingId: number | null = null;
  feedback: FeedbackState = null;
  private feedbackTimeout?: number;

  readonly beneficioForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    descricao: [''],
    valor: [0, [Validators.required, Validators.min(0.01)]],
    ativo: [true]
  });

  readonly transferForm = this.fb.nonNullable.group({
    origemId: ['', Validators.required],
    destinoId: ['', Validators.required],
    valor: [0, [Validators.required, Validators.min(0.01)]]
  });

  private readonly currencyFormat = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  });

  ngOnInit(): void {
    this.carregarBeneficios();
  }

  ngOnDestroy(): void {
    if (typeof window !== 'undefined' && this.feedbackTimeout) {
      window.clearTimeout(this.feedbackTimeout);
    }
  }

  get estaEditando(): boolean {
    return this.editingId !== null;
  }

  get totalAtivo(): number {
    return this.beneficios.reduce((total, beneficio) => total + (beneficio.ativo ? 1 : 0), 0);
  }

  get saldoTotal(): string {
    const total = this.beneficios.reduce((acc, item) => acc + (item.valor ?? 0), 0);
    return this.currencyFormat.format(total);
  }

  carregarBeneficios(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (beneficios) => {
        this.beneficios = beneficios;
        this.loading = false;
      },
      error: (error) => {
        console.error(error);
        this.setFeedback('error', 'Não foi possível carregar os benefícios.');
        this.loading = false;
      }
    });
  }

  iniciarEdicao(beneficio: Beneficio): void {
    if (!beneficio.id) {
      return;
    }

    this.editingId = beneficio.id;
    this.beneficioForm.setValue({
      nome: beneficio.nome,
      descricao: beneficio.descricao ?? '',
      valor: beneficio.valor ?? 0,
      ativo: beneficio.ativo
    });
  }

  cancelarEdicao(): void {
    this.editingId = null;
    this.beneficioForm.reset({
      nome: '',
      descricao: '',
      valor: 0,
      ativo: true
    });
  }

  salvarBeneficio(): void {
    if (this.beneficioForm.invalid) {
      this.beneficioForm.markAllAsTouched();
      return;
    }

    const payload: BeneficioPayload = {
      nome: this.beneficioForm.value.nome!,
      descricao: this.beneficioForm.value.descricao ?? '',
      valor: Number(this.beneficioForm.value.valor ?? 0),
      ativo: this.beneficioForm.value.ativo ?? true
    };

    this.saving = true;
    const request$ = this.editingId
      ? this.service.atualizar(this.editingId, payload)
      : this.service.criar(payload);

    request$.subscribe({
      next: () => {
        this.setFeedback('success', 'Benefício salvo com sucesso!');
        this.saving = false;
        this.cancelarEdicao();
        this.carregarBeneficios();
      },
      error: (error) => {
        console.error(error);
        this.setFeedback('error', 'Não foi possível salvar o benefício.');
        this.saving = false;
      }
    });
  }

  removerBeneficio(beneficio: Beneficio): void {
    if (!beneficio.id || !confirm(`Confirma a remoção de ${beneficio.nome}?`)) {
      return;
    }

    this.service.remover(beneficio.id).subscribe({
      next: () => {
        this.setFeedback('success', `${beneficio.nome} removido.`);
        this.carregarBeneficios();
      },
      error: (error) => {
        console.error(error);
        this.setFeedback('error', 'Não foi possível remover o benefício.');
      }
    });
  }

  transferir(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    const { origemId, destinoId, valor } = this.transferForm.getRawValue();
    if (origemId === destinoId) {
      this.setFeedback('error', 'Selecione contas diferentes para transferir.');
      return;
    }

    this.transferProcessing = true;
    this.service
      .transferir({
        origemId: Number(origemId),
        destinoId: Number(destinoId),
        valor: Number(valor)
      })
      .subscribe({
        next: (message) => {
          this.setFeedback('success', message || 'Transferência concluída!');
          this.transferProcessing = false;
          this.transferForm.reset({ origemId: '', destinoId: '', valor: 0 });
          this.carregarBeneficios();
        },
        error: (error) => {
          console.error(error);
          this.setFeedback('error', error?.error || 'Não foi possível realizar a transferência.');
          this.transferProcessing = false;
        }
      });
  }

  private setFeedback(type: 'success' | 'error', message: string): void {
    this.feedback = { type, message };
    if (typeof window === 'undefined') {
      return;
    }
    if (this.feedbackTimeout) {
      window.clearTimeout(this.feedbackTimeout);
    }
    this.feedbackTimeout = window.setTimeout(() => {
      this.feedback = null;
    }, 5000);
  }

  formatCurrency(valor?: number | null): string {
    return this.currencyFormat.format(valor ?? 0);
  }

  trackById(_: number, beneficio: Beneficio): number | undefined {
    return beneficio.id;
  }
}
