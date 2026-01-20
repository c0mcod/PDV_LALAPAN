package com.pdv.lalapan.exceptions;

public class QuantidadeInvalidaException extends RuntimeException {
    public double quantidade;

    public QuantidadeInvalidaException(double quantidade) {
        super(String.format("Quantidade %.2f não é válida.", quantidade));
        this.quantidade = quantidade;
    }

    public double getQuantidade() {
        return quantidade;
    }
}
