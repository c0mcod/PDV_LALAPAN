package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.ProdutoCreatedDTO;
import com.pdv.lalapan.dto.ProdutoResponseDTO;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = prodRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        return new ProdutoResponseDTO(produto);
    }

    public void delete(Long id) {
        if (!prodRepo.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado.");
        }
        prodRepo.deleteById(id);
    }

    public List<ProdutoResponseDTO> buscarTodosProdutos() {
        return prodRepo.findAll()
                .stream()
                .map(ProdutoResponseDTO :: new)
                .toList();
    }

}
