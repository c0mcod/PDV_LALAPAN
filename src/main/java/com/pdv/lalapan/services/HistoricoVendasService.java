package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.historicoVendas.HistoricoVendasResponseDTO;
import com.pdv.lalapan.dto.historicoVendas.ItemVendaDetalheDTO;
import com.pdv.lalapan.dto.historicoVendas.VendaDetalheDTO;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.exceptions.VendaNaoAbertaException;
import com.pdv.lalapan.exceptions.VendaNaoEncontradaException;
import com.pdv.lalapan.repositories.VendaRepository;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HistoricoVendasService {

    private final VendaRepository vendaRepo;

    public HistoricoVendasService(VendaRepository vendaRepo) {
        this.vendaRepo = vendaRepo;
    }

    public Page<HistoricoVendasResponseDTO> buscarHistorico(LocalDateTime dataInicio, LocalDateTime dataFim, Long operadorId, Pageable pageable) {
        return vendaRepo.buscarHistorico(dataInicio, dataFim, operadorId, pageable)
        .map(venda -> new HistoricoVendasResponseDTO(
                venda.getId(),
                venda.getDataHoraAbertura(),
                venda.getDataHoraFechamento(),
                venda.getOperador().getNome(),
                venda.getValorTotal(),
                venda.getItens().size()
        ));
    }

    public List<HistoricoVendasResponseDTO> buscarHistoricoExport(LocalDateTime dataInicio, LocalDateTime dataFim, Long operadorId) {
        return vendaRepo.buscarHistorico(dataInicio, dataFim, operadorId, Pageable.unpaged())
                .map(venda -> new HistoricoVendasResponseDTO(
                        venda.getId(),
                        venda.getDataHoraAbertura(),
                        venda.getDataHoraFechamento(),
                        venda.getOperador().getNome(),
                        venda.getValorTotal(),
                        venda.getItens().size()
                ))
                .getContent();
    }

    public VendaDetalheDTO buscarDetalhes(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        if(venda.getStatus() != StatusVenda.FINALIZADA) {
            throw new VendaNaoAbertaException(venda.getStatus(), vendaId);
        }

        List<ItemVendaDetalheDTO> itens = venda.getItens()
                .stream()
                .map(item -> new ItemVendaDetalheDTO(
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .toList();

        return new VendaDetalheDTO(
                venda.getId(),
                venda.getOperador().getNome(),
                venda.getDataHoraAbertura(),
                venda.getDataHoraFechamento(),
                venda.getValorTotal(),
                itens
        );
    }
}
