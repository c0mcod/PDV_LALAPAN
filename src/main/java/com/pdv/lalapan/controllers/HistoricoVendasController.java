package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.historicoVendas.HistoricoStatsDTO;
import com.pdv.lalapan.dto.historicoVendas.HistoricoVendasResponseDTO;
import com.pdv.lalapan.dto.historicoVendas.VendaDetalheDTO;
import com.pdv.lalapan.services.ExcelExportService;
import com.pdv.lalapan.services.HistoricoVendasService;
import com.pdv.lalapan.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cglib.core.Local;
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
@Tag(name = "Histórico de Vendas", description = "Endpoints para consulta e exportação do histórico de vendas")
public class HistoricoVendasController {

    private final HistoricoVendasService historicoService;
    private final ExcelExportService excelService;
    private final UsuarioService userService;

    public HistoricoVendasController(HistoricoVendasService historicoService, ExcelExportService excelService, UsuarioService userService) {
        this.historicoService = historicoService;
        this.excelService = excelService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Buscar vendas", description = "Retorna as vendas paginadas filtradas por período e operador")
    public ResponseEntity<Page<HistoricoVendasResponseDTO>> buscarVendas(
            @RequestParam LocalDateTime dataInicio,
            @RequestParam LocalDateTime dataFim,
            @RequestParam(required = false) Long operadorId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(historicoService.buscarHistorico(dataInicio, dataFim, operadorId, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Buscar estatísticas", description = "Retorna estatísticas das vendas filtradas por período e operador")
    public ResponseEntity<HistoricoStatsDTO> buscarStats(
            @RequestParam LocalDateTime dataInicio,
            @RequestParam LocalDateTime dataFim,
            @RequestParam(required = false) Long operadorId) {
        return ResponseEntity.ok(historicoService.buscarStats(dataInicio, dataFim, operadorId));
    }

    @GetMapping("/detalhes/{id}")
    @Operation(summary = "Buscar detalhes da venda", description = "Retorna os detalhes de uma venda pelo ID")
    public ResponseEntity<VendaDetalheDTO> gerarDetalhes(@PathVariable Long id) {
        VendaDetalheDTO response = historicoService.buscarDetalhes(id);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/exportar/excel")
    @Operation(summary = "Exportar para Excel", description = "Exporta o histórico de vendas em formato .xlsx")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Long operadorId,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) throws IOException {

        LocalDateTime inicio = dataInicio != null
                ? LocalDateTime.parse(dataInicio)
                : LocalDateTime.now().minusDays(90);

        LocalDateTime fim = dataFim != null
                ? LocalDateTime.parse(dataFim)
                : LocalDateTime.now();

        List<HistoricoVendasResponseDTO> historico = historicoService.buscarHistoricoExport(inicio, fim, operadorId);
        LocalDateTime criadoEm = LocalDateTime.now();
        String operadorNome = operadorId != null ? userService.buscarNome(operadorId) : null;
        byte[] excelBytes = excelService.exportarHistoricoVendas(historico, inicio, fim, operadorNome, criadoEm);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=historico.xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}