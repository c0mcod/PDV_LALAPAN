package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.exceptions.ProdutoInexistenteException;
import com.pdv.lalapan.repositories.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository prodRepo;

    public ProdutoService(ProdutoRepository prodRepo) {
        this.prodRepo = prodRepo;
    }

    public ProdutoResponseDTO create(ProdutoCreatedDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setCodigo(dto.codigo());
        produto.setCategoria(dto.categoria());
        produto.setUnidade(dto.unidade());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());

        Produto salvo = prodRepo.save(produto);

        return new ProdutoResponseDTO(salvo);
    }

    public ProdutoAtualizadoDTO atualizarProduto(Long produtoId, ProdutoAtualizadoDTO dto) {
        Produto produto = prodRepo.findById(produtoId)
                .orElseThrow(() -> new ProdutoInexistenteException(produtoId));

        produto.setNome(dto.nome());
        produto.setCodigo(dto.codigo());
        produto.setCategoria(dto.categoria());
        produto.setUnidade(dto.unidade());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());

        Produto produtoAtualizado = prodRepo.save(produto);

        return ProdutoAtualizadoDTO.fromEntity(produtoAtualizado);
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = prodRepo.findById(id)
                .orElseThrow(() -> new ProdutoInexistenteException(id));

        return new ProdutoResponseDTO(produto);
    }

    public void delete(Long id) {
        Produto produto = prodRepo.findById(id)
                        .orElseThrow(() -> new ProdutoInexistenteException(id));
        produto.setAtivo(false);
        prodRepo.save(produto);
    }

    public List<Produto> findAll() {
        return prodRepo.findByAtivoTrue();
    }

    public List<ProdutoResponseDTO> buscarTodosProdutos() {
        return prodRepo.findByAtivoTrue()
                .stream()
                .map(ProdutoResponseDTO :: new)
                .toList();
    }

    public List<ProdutoEstoqueBaixoDTO> listarEstoqueBaixo() {
        return prodRepo.findProdutosComEstoqueBaixo()
                .stream()
                .map(ProdutoEstoqueBaixoDTO::fromEntity)
                .toList();
    }

    @Transactional
    public ProdutoResponseDTO registrarEntrada(Long produtoId, EntradaProdutoRequestDTO dto) {
        Produto produto = prodRepo.findById(produtoId)
                .orElseThrow(() -> new ProdutoInexistenteException(produtoId));

        produto.adicionarEstoque(dto.quantidade());
        Produto produtoSalvo = prodRepo.save(produto);

        return new ProdutoResponseDTO(produtoSalvo);
    }
}
