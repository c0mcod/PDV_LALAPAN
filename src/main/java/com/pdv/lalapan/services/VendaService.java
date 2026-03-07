package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.cancelamento.CancelarItemDTO;
import com.pdv.lalapan.dto.cancelamento.CancelarVendaDTO;
import com.pdv.lalapan.dto.venda.*;
import com.pdv.lalapan.entities.*;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.exceptions.*;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.UsuarioRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    private final VendaRepository vendaRepo;
    private final ProdutoRepository prodRepo;
    private final UsuarioRepository userRepo;

    public VendaService(VendaRepository vendaRepo, ProdutoRepository prodRepo, UsuarioRepository userRepo) {
        this.vendaRepo = vendaRepo;
        this.prodRepo = prodRepo;
        this.userRepo = userRepo;
    }


    public VendaAberturaDTO iniciarVenda(Long usuarioId) {

        Usuario operador = userRepo.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        Optional<Venda> vendaAberta = vendaRepo.findByStatusAndOperador(StatusVenda.ABERTA, operador);

        if (vendaAberta.isPresent()) {
            return new VendaAberturaDTO(vendaAberta.get().getId());
        }

        Venda venda = new Venda();
        venda.abrirVenda(operador);

        Venda salva = vendaRepo.save(venda);
        return new VendaAberturaDTO(salva.getId());
    }


    @Transactional
    public VendaAddItemResponseDTO adicionarItem(Long vendaId, VendaAddItemRequestDTO dto) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        Produto produto = prodRepo.findById(dto.idProduto())
                .orElseThrow(() -> new ProdutoInexistenteException(dto.idProduto()));

        venda.validarStatus(vendaId);
        VendaItens item = venda.registrarProduto(produto, dto.quantidade());
        Venda vendaSalva = vendaRepo.save(venda);

        return new VendaAddItemResponseDTO(
                vendaSalva.getId(),
                vendaSalva.getValorTotal(),
                item.getId()
        );
    }

    @Transactional
    public VendaFinalizadaResponseDTO fecharVenda(Long vendaId, VendaFinalizadaRequestDTO dto) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        BigDecimal totalPago = dto.pagamentos().stream()
                .map(PagamentoRequestDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        venda.validarPagamento(totalPago);
        venda.processarBaixaEstoque();

        for (PagamentoRequestDTO pagamentoDTO : dto.pagamentos()) {
            venda.registrarPagamento(
                    pagamentoDTO.metodo(),
                    pagamentoDTO.valor()
            );
        }

        venda.fechar();
        BigDecimal troco = venda.getTroco(totalPago);

        Venda vendaFinalizada = vendaRepo.save(venda);
        return new VendaFinalizadaResponseDTO(vendaFinalizada.getId(), troco);
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