package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.VendaAberturaDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class VendaServiceTest {

    @Autowired
    private VendaService vendaService;

    @Test
    void deveCriarVendaComSucesso() {
        VendaAberturaDTO venda = vendaService.iniciarVenda();

        assertNotNull(venda);
        assertNotNull(venda.vendaId());
    }
}
