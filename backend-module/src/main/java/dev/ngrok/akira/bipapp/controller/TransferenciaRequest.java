package dev.ngrok.akira.bipapp.controller;

import java.math.BigDecimal;

public class TransferenciaRequest {

    private Long origemId;
    private Long destinoId;
    private BigDecimal valor;

    public TransferenciaRequest() {}

    public Long getOrigemId() {
        return origemId;
    }

    public void setOrigemId(Long origemId) {
        this.origemId = origemId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
