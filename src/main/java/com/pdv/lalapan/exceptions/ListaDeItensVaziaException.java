package com.pdv.lalapan.exceptions;

public class ListaDeItensVaziaException extends RuntimeException {
    private Long vendaId;
    public ListaDeItensVaziaException(Long vendaId) {
        super(String.format("Não há itens na venda com id %d para finalizar.", vendaId));
        this.vendaId = vendaId;
    }

    public Long getVendaId() {
        return vendaId;
    }
}
