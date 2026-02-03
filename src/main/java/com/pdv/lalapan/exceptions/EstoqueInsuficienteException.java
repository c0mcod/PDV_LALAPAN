package com.pdv.lalapan.exceptions;

import java.math.BigDecimal;

public class EstoqueInsuficienteException extends RuntimeException{
    private String nomeProduto;
    private BigDecimal quantidade;
    private Double quantidadeEstoque;

    public EstoqueInsuficienteException(String nomeProduto, BigDecimal quantidade, Double quantidadeEstoque) {
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

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public Double getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
}
