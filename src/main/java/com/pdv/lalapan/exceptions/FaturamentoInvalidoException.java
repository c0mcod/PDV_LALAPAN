package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class FaturamentoInvalidoException extends RuntimeException {
    private BigDecimal faturamento;
    public FaturamentoInvalidoException(BigDecimal faturamento) {
        super(String.format("O faturamento atual: %.2f e invalido", faturamento));
        this.faturamento = faturamento;
    }

    public BigDecimal getFaturamento() {
        return faturamento;
    }
}
