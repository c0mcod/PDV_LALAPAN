package com.pdv.lalapan.exceptions;

public class VendaNaoEncontradaException extends RuntimeException {
    private Long idVenda;

    public VendaNaoEncontradaException(Long idVenda){
        super("Venda com id: " + idVenda + " não encontrada.");
        this.idVenda = idVenda;
    }

    public Long getIdVenda() {
        return idVenda;
    }
}
