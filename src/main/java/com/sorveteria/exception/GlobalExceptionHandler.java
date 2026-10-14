package com.sorveteria.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<Map<String, Object>> handleEmailDuplicado(EmailJaCadastradoException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "erro", "Conflito", "mensagem",
				e.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(CpfJaCadastradoException.class)
	public ResponseEntity<Map<String, Object>> handleCpfDuplicado(CpfJaCadastradoException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "erro", "Conflito", "mensagem",
				e.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException e) {
		List<String> erros = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.toList());

		return ResponseEntity.badRequest()
				.body(Map.of("status", 400, "erro", "Validação", "mensagens", erros, "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(PedidoNaoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> handlePedidoNaoEncontrado(PedidoNaoEncontradoException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "erro", "Não encontrado",
				"mensagem", ex.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", 400, "erro", "Requisição inválida",
				"mensagem", e.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(TransicaoStatusInvalidaException.class)
	public ResponseEntity<Map<String, Object>> handleTransicaoInvalida(TransicaoStatusInvalidaException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "erro", "Transição inválida",
				"mensagem", e.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(ProdutoNaoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> handleProdutoNaoEncontrado(ProdutoNaoEncontradoException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "erro", "Não encontrado",
				"mensagem", e.getMessage(), "timestamp", LocalDateTime.now()));
	}

	@ExceptionHandler(ItemInvalidoException.class)
	public ResponseEntity<Map<String, Object>> handleItemInvalido(ItemInvalidoException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "erro", "Operação inválida",
				"mensagem", e.getMessage(), "timesstamp", LocalDateTime.now()));

	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleGenerico(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 500, "erro",
				"Erro interno", "mensagem", e.getMessage(), "timestamp", LocalDateTime.now()));
	}

}