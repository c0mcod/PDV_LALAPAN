package com.pdv.lalapan.dto.historicoVendas;

import com.pdv.lalapan.enums.MetodoPagamento;
import java.math.BigDecimal;

public record PagamentoDetalheDTO(MetodoPagamento metodo, BigDecimal valor) {}