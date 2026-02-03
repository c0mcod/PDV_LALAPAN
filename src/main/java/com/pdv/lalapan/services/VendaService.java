package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.entities.VendaItens;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.exceptions.*;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    private final VendaRepository vendaRepo;
    private final ProdutoRepository prodRepo;

    public VendaService(VendaRepository vendaRepo, ProdutoRepository prodRepo) {
        this.vendaRepo = vendaRepo;
        this.prodRepo = prodRepo;
    }


    public VendaAberturaDTO iniciarVenda() {

        Optional<Venda> vendaAberta = vendaRepo.findByStatus(StatusVenda.ABERTA);

        if (vendaAberta.isPresent()) {
            return new VendaAberturaDTO(vendaAberta.get().getId());
        }

        Venda venda = new Venda();
        venda.setDataHoraAbertura(LocalDateTime.now());
        venda.setStatus(StatusVenda.ABERTA);
        venda.setValorTotal(BigDecimal.ZERO);

        Venda salva = vendaRepo.save(venda);
        return new VendaAberturaDTO(salva.getId());
    }


    @Transactional
    public VendaAddItemResponseDTO adicionarItem(Long vendaId, VendaAddItemRequestDTO dto) {
        // Verifica se venda existe
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        // Verifica se produto existe
        Produto produto = prodRepo.findById(dto.idProduto())
                .orElseThrow(() -> new ProdutoInexistenteException(dto.idProduto()));

        // Checagem de status de venda
        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new VendaNaoAbertaException(venda.getStatus(), vendaId);
        }

        VendaItens item = new VendaItens();
        item.setProduto(produto);
        item.setQuantidade(dto.quantidade());

        if (item.getQuantidade().compareTo(BigDecimal.valueOf(produto.getQuantidadeEstoque())) > 0) {
            System.out.println("AVISO: Adicionando mais itens que o estoque disponível!");
        }

        item.setPrecoUnitario(produto.getPreco());
        item.calcularSubTotal();

        venda.adicionarItem(item);

        Venda vendaSalva = vendaRepo.save(venda);
        VendaItens itemSalvo = vendaSalva.getItens().get(vendaSalva.getItens().size() - 1);

        return new VendaAddItemResponseDTO(
                vendaSalva.getId(),
                vendaSalva.getValorTotal(),
                itemSalvo.getId()
        );
    }

    @Transactional
    public VendaFinalizadaResponseDTO fecharVenda(Long vendaId, VendaFinalizadaRequestDTO dto) {
        return finalizarVenda(vendaId, dto, true);
    }

    @Transactional
    public VendaFinalizadaResponseDTO forcarVendaFechada(Long idVenda, VendaFinalizadaRequestDTO dto) {
        return finalizarVenda(idVenda, dto, false);
    }

    @Transactional
    public VendaFinalizadaResponseDTO finalizarVenda(Long vendaId, VendaFinalizadaRequestDTO dto, boolean validarEstoque) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        BigDecimal totalVenda = venda.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(item.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dto.valorRecebido().compareTo(totalVenda) < 0) {
            throw new ValorInsuficienteException(dto.valorRecebido(), totalVenda);
        }

        BigDecimal troco = dto.valorRecebido().subtract(totalVenda);

        venda.fechar(dto.metodo());

        for (VendaItens item : venda.getItens()) {
            Produto produto = item.getProduto();

            if (validarEstoque && item.getQuantidade().compareTo(BigDecimal.valueOf(produto.getQuantidadeEstoque())) > 0) {
                throw new EstoqueInsuficienteException(produto.getNome(), item.getQuantidade(), produto.getQuantidadeEstoque());
            }

            produto.setQuantidadeEstoque(
                    BigDecimal.valueOf(produto.getQuantidadeEstoque()).subtract(item.getQuantidade()).doubleValue()
            );
        }

        Venda vendaFinalizada = vendaRepo.save(venda);
        return new VendaFinalizadaResponseDTO(
                vendaFinalizada.getId(),
                troco
        );
    }


    @Transactional
    public CancelarVendaDTO cancelarVenda(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        venda.cancelar();
        Venda vendaSalva = vendaRepo.save(venda);
        return new CancelarVendaDTO(
                vendaSalva.getId(),
                vendaSalva.getStatus()
        );
    }

    @Transactional
    public CancelarItemDTO cancelarItem(Long vendaId, Long vendaItemId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        if(venda.getStatus() != StatusVenda.ABERTA) {
            throw new VendaNaoAbertaException(venda.getStatus(), vendaId);
        }

        venda.removerItem(vendaItemId);
        Venda vendaSalva = vendaRepo.save(venda);
        return new CancelarItemDTO(
                vendaSalva.getId(),
                vendaItemId
        );
    }

    public VendaDetalhadaDTO buscarVendaDetalhada(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));
        List<VendaItemDTO> itens = venda.getItens().stream()
                .map(item -> new VendaItemDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getPrecoUnitario(),
                        item.getQuantidade()

                )).toList();

        return new VendaDetalhadaDTO(
                venda.getId(),
                venda.getValorTotal(),
                itens
        );
    }
}