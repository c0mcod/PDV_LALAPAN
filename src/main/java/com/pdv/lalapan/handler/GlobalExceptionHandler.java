package com.pdv.lalapan.handler;

import com.pdv.lalapan.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroResponse> handlerEstoqueInsuficiente(EstoqueInsuficienteException e) {
        DetalhesEstoque detalhes = new DetalhesEstoque(
                e.getNomeProduto(),
                e.getQuantidade(),
                e.getQuantidadeEstoque()
        );

        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                400,
                detalhes
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(VendaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handlerVendaNaoEncontrada(VendaNaoEncontradaException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                404,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ProdutoInexistenteException.class)
    public ResponseEntity<ErroResponse> handlerProdutoNaoEncontrado(ProdutoInexistenteException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                404,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ValorInsuficienteException.class)
    public ResponseEntity<ErroResponse> handlerValorInsuficiente(ValorInsuficienteException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                409,
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(VendaNaoAbertaException.class)
    public ResponseEntity<ErroResponse> handlerVendaNaoAberta(VendaNaoAbertaException e) {

        DetalhesVendaNaoAberta detalhes = new DetalhesVendaNaoAberta(
                e.getIdVenda(),
                e.getStatusAtual().toString()
        );

        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                422,
                detalhes
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    @ExceptionHandler(ItemNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handlerItemNaoEncontrado(ItemNaoEncontradoException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                404,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ListaDeItensVaziaException.class)
    public ResponseEntity<ErroResponse> handlerListaDeItensVazia(ListaDeItensVaziaException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                404,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MetodoDePagamentoInvalidoException.class)
    public ResponseEntity<ErroResponse> handlerMetodoDePagamentoInvalido(MetodoDePagamentoInvalidoException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                405,
                null
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
    }

    @ExceptionHandler(ValorTotalInvalidoException.class)
    public ResponseEntity<ErroResponse> handlerValorTotalInvalido(ValorTotalInvalidoException e) {
        ErroResponse erro = new ErroResponse(
                e.getMessage(),
                LocalDateTime.now(),
                406,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(erro);
    }
}
