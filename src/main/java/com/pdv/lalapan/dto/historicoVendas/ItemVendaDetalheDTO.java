package com.pdv.lalapan.dto.historicoVendas;

import java.math.BigDecimal;

public record ItemVendaDetalheDTO(
        String nome,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
}
