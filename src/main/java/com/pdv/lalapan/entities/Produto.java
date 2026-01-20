package com.pdv.lalapan.entities;

import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.Unidade;
import com.pdv.lalapan.exceptions.QuantidadeInvalidaException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private BigDecimal preco = BigDecimal.ZERO;
    private String codigo;
    private double estoqueMinimo;
    private double quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @OneToMany(mappedBy = "produto")
    private List<VendaItens> vendaItens;

    // Construtor Vazio
    public Produto () {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public double getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(double quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<VendaItens> getVendaItens() {
        return vendaItens;
    }

    public void setVendaItens(List<VendaItens> vendaItens) {
        this.vendaItens = vendaItens;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(double estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isEstoqueBaixo() {
        return this.quantidadeEstoque <= this.estoqueMinimo;
    }

    public void adicionarEstoque(double quantidade) {
        if(quantidade <= 0) {
            throw new QuantidadeInvalidaException(quantidade);
        }
        this.quantidadeEstoque += quantidade;
    }
}
