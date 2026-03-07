package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class ValorNegativoException extends RuntimeException {
    BigDecimal valor;
    public ValorNegativoException(BigDecimal valor) {
        super(String.format("O valor %.2f nao e um valor valido.", valor));
        this.valor = valor;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
