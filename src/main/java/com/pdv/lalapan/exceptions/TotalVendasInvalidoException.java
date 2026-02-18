package com.pdv.lalapan.exceptions;

public class TotalVendasInvalidoException extends RuntimeException {
    Integer totalVendas;

    public TotalVendasInvalidoException(Integer totalVendas) {
        super(String.format("o numero total de vendas retornado: %d e invalido", totalVendas));
        this.totalVendas = totalVendas;
    }

    public Integer getTotalVendas() {
        return totalVendas;
    }
}
