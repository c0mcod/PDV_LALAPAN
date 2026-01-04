package com.pdv.lalapan.exceptions;

public class ProdutoNaoEncontradoException extends RuntimeException{

    private Long idProduto;

    public ProdutoNaoEncontradoException(Long idProduto) {
        super("Produto com id: " + idProduto + " não encontrado.");
        this.idProduto = idProduto;
    }

    public Long getIdProduto() {
        return idProduto;
    }
}
