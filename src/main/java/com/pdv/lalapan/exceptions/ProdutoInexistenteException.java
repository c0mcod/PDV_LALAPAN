package com.pdv.lalapan.exceptions;

public class ProdutoInexistenteException extends RuntimeException{
    private Long idProduto;

    public ProdutoInexistenteException(Long idProduto) {
        super(String.format("Produto com id: %d não encontrado.", idProduto));
        this.idProduto = idProduto;
    }

    public Long getIdProduto() {
        return idProduto;
    }
}
