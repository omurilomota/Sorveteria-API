package com.sorveteria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sorveteria.dto.ProdutoRequest;
import com.sorveteria.dto.ProdutoResponse;
import com.sorveteria.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de Produtos")
public class ProdutoController {

	private final ProdutoService service;

	@Operation(summary = "Listar todos os produtos")
	@GetMapping
	public List<ProdutoResponse> listar() {
		return service.listar();
	}

	@Operation(summary = "Buscar produto por ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Produto encontrado"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado") })

	@GetMapping("/{id}")
	public ProdutoResponse buscarPorId(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

	@Operation(summary = "Criar novo produto")
	@ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProdutoResponse criar(@RequestBody @Valid ProdutoRequest dto) {
		return service.criar(dto);
	}

	@Operation(summary = "Atualizar produto")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Produto atualizado"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado") })
	@PutMapping("/{id}")
	public ProdutoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoRequest dto) {
		return service.atualizar(id, dto);
	}

	@Operation(summary = "Excluir produto")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Produto excluído"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado") })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {

		service.deletar(id);
	}

}
