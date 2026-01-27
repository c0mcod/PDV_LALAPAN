package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.*;
import com.pdv.lalapan.services.VendaService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venda")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    // ========== CICLO PRINCIPAL ==========
    @PostMapping("/abrir")
    public ResponseEntity<VendaAberturaDTO> abrirVenda() {
        VendaAberturaDTO venda = vendaService.iniciarVenda();
        return ResponseEntity.ok(venda);
    }

    @PostMapping("/{vendaId}/itens")
    public ResponseEntity<VendaAddItemResponseDTO> adicionarItens(@PathVariable Long vendaId, @RequestBody VendaAddItemRequestDTO requestDto) {
        VendaAddItemResponseDTO response = vendaService.adicionarItem(vendaId, requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{vendaId}/finalizar")
    public ResponseEntity<VendaFinalizadaResponseDTO> finalizarVenda(@PathVariable Long vendaId, @RequestBody VendaFinalizadaRequestDTO requestDTO) {
        VendaFinalizadaResponseDTO response = vendaService.fecharVenda(vendaId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // ========== ==========
    @PostMapping("/{vendaId}/remover-item")
    public ResponseEntity<CancelarItemDTO> cancelarItem(@PathVariable Long vendaId, @RequestBody RemoverItemRequest request) {
        CancelarItemDTO response = vendaService.cancelarItem(vendaId, request.itemId());
        return ResponseEntity.ok(response);
    }
}
