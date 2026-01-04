package com.pdv.lalapan.exceptions;

import com.pdv.lalapan.enums.StatusVenda;

public class VendaNaoAbertaException extends RuntimeException{
    private StatusVenda statusAtual;
    private Long idVenda;

    public VendaNaoAbertaException(StatusVenda statusAtual, Long idVenda) {
        super(String.format("Não é possivel adicionar itens. Venda %d está com status %s.", idVenda, statusAtual));
        this.statusAtual = statusAtual;
    }

    public StatusVenda getStatusAtual() {
        return statusAtual;
    }
}
