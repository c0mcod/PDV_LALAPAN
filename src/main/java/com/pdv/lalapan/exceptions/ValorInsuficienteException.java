package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class ValorInsuficienteException extends RuntimeException {
    private BigDecimal valorRecebido;
    private BigDecimal totalVenda;

    public ValorInsuficienteException(BigDecimal valorRecebido, BigDecimal totalVenda) {
        super(String.format("Valor recebido (R$%.2f) e menor que o total da venda (R$%.2f)", valorRecebido, totalVenda));
        this.totalVenda = totalVenda;
        this.valorRecebido = valorRecebido;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public BigDecimal getTotalVenda() {
        return totalVenda;
    }
}
