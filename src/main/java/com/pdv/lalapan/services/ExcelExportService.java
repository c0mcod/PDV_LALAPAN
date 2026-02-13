package com.pdv.lalapan.services;

import com.pdv.lalapan.entities.Produto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportarProdutos(List<Produto> produtos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Produtos");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("CODIGO");
        headerRow.createCell(1).setCellValue("NOME");
        headerRow.createCell(2).setCellValue("CATEGORIA");
        headerRow.createCell(3).setCellValue("PREÇO DE CUSTO");
        headerRow.createCell(4).setCellValue("QUANTIDADE");
        headerRow.createCell(5).setCellValue("UNIDADE");
        headerRow.createCell(6).setCellValue("QTD. MIN");
        headerRow.createCell(7).setCellValue("VALOR");
        headerRow.createCell(8).setCellValue("STATUS");

        int rowNum = 1;
        for(Produto produto : produtos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(produto.getCodigo());
            row.createCell(1).setCellValue(produto.getNome());
            row.createCell(2).setCellValue(produto.getCategoria().toString());
            row.createCell(3).setCellValue(produto.getPrecoCusto().doubleValue());
            row.createCell(4).setCellValue(produto.getQuantidadeEstoque().doubleValue());
            row.createCell(5).setCellValue(produto.getUnidade().toString());
            row.createCell(6).setCellValue(produto.getEstoqueMinimo().doubleValue());
            row.createCell(7).setCellValue(produto.getPreco().doubleValue());
            row.createCell(8).setCellValue(produto.getAtivo() ? "Ativo" : "Inativo");
        }

        for(int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
