package com.pdv.lalapan.repositories;

import com.pdv.lalapan.entities.VendaItens;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaItensRepository extends JpaRepository<VendaItens, Long> {

    // Top produtos
    @Query("SELECT p.nome, SUM(iv.quantidade) as qtd, SUM(iv.quantidade * iv.precoUnitario) as total " +
            "FROM VendaItens iv " +
            "JOIN iv.produto p " +
            "JOIN iv.venda v " +
            "WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim " +
            "GROUP BY p.id, p.nome " +
            "ORDER BY qtd DESC")
    List<Object[]> topProdutosMaisVendidos(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    // Vendas por categoria
    @Query("""
    SELECT p.categoria,
           SUM(iv.quantidade * iv.precoUnitario)
    FROM VendaItens iv
    JOIN iv.produto p
    JOIN iv.venda v
    WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim
    GROUP BY p.categoria
    ORDER BY SUM(iv.quantidade * iv.precoUnitario) DESC
""")
    List<Object[]> vendasPorCategoria(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

}
