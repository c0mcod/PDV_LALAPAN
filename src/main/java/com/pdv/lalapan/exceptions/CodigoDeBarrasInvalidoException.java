package com.pdv.lalapan.exceptions;

public class CodigoDeBarrasInvalidoException extends RuntimeException {
    String codigo;
    public CodigoDeBarrasInvalidoException(String codigo) {
        super(String.format("O codigo %s é invalido para o padrão EAN-13.", codigo));
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
