package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.historicoVendas.HistoricoVendasResponseDTO;
import com.pdv.lalapan.dto.historicoVendas.VendaDetalheDTO;
import com.pdv.lalapan.services.ExcelExportService;
import com.pdv.lalapan.services.HistoricoVendasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/historico-vendas")
public class HistoricoVendasController {

    private final HistoricoVendasService historicoService;
    private final ExcelExportService excelService;

    public HistoricoVendasController(HistoricoVendasService historicoService, ExcelExportService excelService) {
        this.historicoService = historicoService;
        this.excelService = excelService;
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

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) Long operadorId) throws IOException {

        LocalDateTime dataFim = LocalDateTime.now();
        LocalDateTime dataInicio = dataFim.minusDays(90);

        List<HistoricoVendasResponseDTO> historico = historicoService.buscarHistoricoExport(dataInicio, dataFim, operadorId);
        byte[] excelBytes = excelService.exportarHistoricoVendas(historico);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=historico.xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
