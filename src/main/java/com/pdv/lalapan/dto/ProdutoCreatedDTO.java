package com.pdv.lalapan.dto;

import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.Unidade;

import java.math.BigDecimal;

public record ProdutoCreatedDTO(
        String nome,
        String codigo,
        BigDecimal estoqueMinimo,
        BigDecimal preco,
        Unidade unidade,
        Categoria categoria,
        BigDecimal quantidadeEstoque
) {

}
