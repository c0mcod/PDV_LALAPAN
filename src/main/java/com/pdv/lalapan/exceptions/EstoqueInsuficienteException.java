package com.pdv.lalapan.exceptions;

public class EstoqueInsuficienteException extends RuntimeException{
    private String nomeProduto;
    private Integer quantidade;
    private Double quantidadeEstoque;

    public EstoqueInsuficienteException(String nomeProduto, Integer quantidade, Double quantidadeEstoque) {
        super(String.format(
                "Estoque insuficiente para o produto '%s'. Solicitado: %d, Disponível: %.2f",
                nomeProduto, quantidade, quantidadeEstoque
        ));
        this.nomeProduto = nomeProduto;
        this.quantidadeEstoque = quantidadeEstoque;
        this.quantidade = quantidade;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }
}
