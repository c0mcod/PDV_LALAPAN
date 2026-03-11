package com.pdv.lalapan.services;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.pdv.lalapan.dto.impressao.ImpressaoDTO;
import com.pdv.lalapan.dto.venda.VendaItemDTO;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.entities.VendaItens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImpressoraService {
    // nome da impressora definida em application.properties
    @Value("${pdv.printer.name}")
    private String printerName;

    // ativar ou desativar o método de impressão(valor definido em application.properties)
    @Value("${pdv.printer.enabled:true}")
    private boolean printerEnabled;

    public static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    private EscPos conectar() throws Exception {
        PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);

        if (printService == null) {
            throw new RuntimeException("Impressora não encontrada: " + printerName);
        }
        return new EscPos(new PrinterOutputStream(printService));
    }

    public void imprimirCupom(ImpressaoDTO dto, String operador) throws Exception {
        if (!printerEnabled) return;
        try (EscPos escpos = conectar()) {

            Style titulo = new Style()
                    .setFontSize(Style.FontSize._1, Style.FontSize._1)
                    .setJustification(EscPosConst.Justification.Center);

            Style centralizado = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            // Cabeçalho
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dataFormatada = dto.horaFechamento().format(formatter);

            escpos.writeLF(titulo, "LALAPAN MERCEARIA/PANIFICADORA");
            escpos.writeLF(centralizado, "CNPJ: 00.000.000/0000-00");
            escpos.writeLF(centralizado, "AV TORREON, NUMERO - CAUCAIA - CE");
            escpos.writeLF("OPERADOR: " + operador.toUpperCase());
            escpos.writeLF("ABERTURA: " + dto.horaAbertura().format(formatter));
            escpos.writeLF("FECHAMENTO: " + dto.horaFechamento().format(formatter));
            escpos.writeLF("ID VENDA: #000" + dto.vendaId());

            escpos.writeLF("--------------------------------");

            for (VendaItemDTO item : dto.itens()) {
                BigDecimal subtotal = item.precoUnitario().multiply(item.quantidade());
                escpos.writeLF(removerAcentos(item.nomeProduto()));
                escpos.writeLF(item.quantidade() + " x " + item.precoUnitario() + "        " + subtotal);
            }

            // Total
            Style bold = new Style().setBold(true);
            escpos.writeLF("--------------------------------");
            escpos.writeLF(bold, "TOTAL: R$ " + dto.valorTotal());

            // Rodapé
            escpos.writeLF(centralizado, "Obrigado pela preferencia!");
            escpos.feed(3);
            escpos.cut(EscPos.CutMode.PART);
        }
    }
}