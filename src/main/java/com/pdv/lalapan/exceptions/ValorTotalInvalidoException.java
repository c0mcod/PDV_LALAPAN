package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class ValorTotalInvalidoException extends RuntimeException {
    private BigDecimal valorTotal;

    public ValorTotalInvalidoException(BigDecimal valorTotal) {
        super(String.format("Valor total da venda R$%.2f é inválido.", valorTotal));
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
