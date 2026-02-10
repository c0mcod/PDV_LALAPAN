package com.pdv.lalapan.entities;

import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.Unidade;
import com.pdv.lalapan.exceptions.CodigoDeBarrasInvalidoException;
import com.pdv.lalapan.exceptions.QuantidadeInvalidaException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Produto {
    /*
    *   ATRIBUTOS DA ENTIDADE PARA CONSULTA EM ORDEM:
    *   - ID
    *   - nome
    *   - preco
    *   - codigo
    *   - estoqueMinimo
    *   - quantidadeEstoque
    *   - unidade
    *   - categoria
    * ---------------------------------------------------------------
    *   ESQUEMA RAW PARA TESTE EM POSTMAN:
    *   {
    *       "nome":,
    *       "preco":,
    *       "codigo":,
    *       "estoqueMinimo":,
    *       "quantidadeEstoque":,
    *       "unidade":,
    *       "categoria"
    * }
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private BigDecimal preco = BigDecimal.ZERO;
    private String codigo;
    private BigDecimal estoqueMinimo;
    private BigDecimal quantidadeEstoque;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 50)
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

    public BigDecimal getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(BigDecimal quantidadeEstoque) {
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
        this.codigo = formatarEAN13(codigo);
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isEstoqueBaixo() {
        return this.quantidadeEstoque.compareTo(this.estoqueMinimo) <= 0;
    }

    public void adicionarEstoque(BigDecimal quantidade) {
        if(quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new QuantidadeInvalidaException(quantidade);
        }
        this.quantidadeEstoque = this.quantidadeEstoque.add(quantidade);
    }

    private String formatarEAN13(String codigo) {
        codigo = codigo.replaceAll("\\D", "");

        if (codigo.length() == 13) {
            return codigo;
        }

        if (codigo.length() > 13) {
            throw new CodigoDeBarrasInvalidoException(codigo);
        }

        codigo = String.format("%012d", Long.parseLong(codigo));

        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }
        int digitoVerificador = (10 - (soma % 10)) % 10;

        return codigo + digitoVerificador;
    }
}
