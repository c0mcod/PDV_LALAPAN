package com.pdv.lalapan.exceptions;

import com.pdv.lalapan.enums.MetodoPagamento;

public class MetodoDePagamentoInvalidoException extends RuntimeException {
    private MetodoPagamento metodo;
    public MetodoDePagamentoInvalidoException(MetodoPagamento metodo) {
        super(String.format("Método de pagamento '%s' não é válido", metodo));
        this.metodo = metodo;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }
}
