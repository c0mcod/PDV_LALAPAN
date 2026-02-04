package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class QuantidadeInvalidaException extends RuntimeException {
    public BigDecimal quantidade;

    public QuantidadeInvalidaException(BigDecimal quantidade) {
        super(String.format("Quantidade %.2f não é válida.", quantidade));
        this.quantidade = quantidade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}
