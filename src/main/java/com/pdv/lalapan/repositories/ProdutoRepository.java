package com.pdv.lalapan.repositories;

import com.pdv.lalapan.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque <= p.estoqueMinimo AND p.estoqueMinimo IS NOT NULL")
    List<Produto> findProdutosComEstoqueBaixo();
}
