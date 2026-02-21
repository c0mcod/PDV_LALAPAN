package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.historicoVendas.HistoricoVendasResponseDTO;
import com.pdv.lalapan.dto.historicoVendas.VendaDetalheDTO;
import com.pdv.lalapan.services.HistoricoVendasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/historico-vendas")
public class HistoricoVendasController {

    private final HistoricoVendasService historicoService;

    public HistoricoVendasController(HistoricoVendasService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping
    public ResponseEntity<Page<HistoricoVendasResponseDTO>> buscarVendas(
            @RequestParam LocalDateTime dataInicio,
            @RequestParam LocalDateTime dataFim,
            @RequestParam(required = false) Long operadorId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(historicoService.buscarHistorico(dataInicio, dataFim, operadorId, pageable));
    }

    @GetMapping("/detalhes/{id}")
    public ResponseEntity<VendaDetalheDTO> gerarDetalhes(@PathVariable Long id) {
        VendaDetalheDTO response = historicoService.buscarDetalhes(id);
        return ResponseEntity.ok().body(response);
    }
}
