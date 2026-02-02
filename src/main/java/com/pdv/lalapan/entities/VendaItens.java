package com.pdv.lalapan.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class VendaItens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    * TODO: CORRIGIR O ATRIBUTO "quantidade".
    *  1°: substituir toda e qualquer lógica que envolva calculo de subtotal, pois atualmente
    *      os calculos estão sendo feitos com o atributo "quantidade" em Integer, se uma quantidade
    *      for lida em decimal (gramas ou kg decimal) o calculo não trabalha precisamente, convertendo
    *      para um numero inteiro.
    *
    * TODO: INSPECIONAR FUNCIONAMENTO.
    *  2°: inspecionar o funcionamento de cada etapa do ciclo de venda para ter certeza de que o calculo
     *     está sendo feito da maneira correta.
     * 3°: Iniciar implementação do ciclo de calculo nos testes de integração e unitários
     */

    private Integer quantidade;

    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // Construtor vazio
    public VendaItens() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public void calcularSubTotal() {
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
