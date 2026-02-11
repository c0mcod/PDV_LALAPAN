package com.pdv.lalapan.dto.produto;

import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.Unidade;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String codigo,
        BigDecimal quantidadeEstoque,
        BigDecimal estoqueMinimo,
        String nome, BigDecimal preco,
        Unidade unidade,
        Categoria categoria) {

    public ProdutoResponseDTO(Produto entity) {
        this(entity.getId(), entity.getCodigo(), entity.getQuantidadeEstoque(), entity.getEstoqueMinimo(), entity.getNome(), entity.getPreco(), entity.getUnidade(), entity.getCategoria());
    }
}
