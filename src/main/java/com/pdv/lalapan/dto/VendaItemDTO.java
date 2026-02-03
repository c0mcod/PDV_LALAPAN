package com.pdv.lalapan.dto;

import java.math.BigDecimal;

public record VendaItemDTO(Long itemId, Long produtoId, String nomeProduto, BigDecimal precoUnitario, BigDecimal quantidade) {
}
