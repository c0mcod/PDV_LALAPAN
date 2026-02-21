package com.pdv.lalapan.dto.historicoVendas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaDetalheDTO(
        Long id,
        String operador,
        LocalDateTime dataHoraAbertura,
        LocalDateTime dataHoraFechamento,
        BigDecimal valorTotal,
        List<ItemVendaDetalheDTO> itens
        ) {
}
