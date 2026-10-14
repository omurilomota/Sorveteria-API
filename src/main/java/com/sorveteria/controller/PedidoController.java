package com.sorveteria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sorveteria.dto.ItemPedidoRequest;
import com.sorveteria.dto.PedidoRequest;
import com.sorveteria.dto.PedidoResponse;
import com.sorveteria.dto.PedidoStatusRequest;
import com.sorveteria.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de Pedidos")
public class PedidoController {

	private final PedidoService service;

	@Operation(summary = "Listar todos os pedidos")
	@GetMapping
	// ResponseEntity é uma classe do Spring que representa toda a resposta HTTP
	// enviada pelo servidor, permitindo que você controle:
	public List<PedidoResponse> listar() {
		return service.listar();
	}

	@Operation(summary = "Buscar pedido por ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
			@ApiResponse(responseCode = "404", description = "Pedido não encontrado") })
	@GetMapping("/{id}")
	public PedidoResponse buscarPorId(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

	@Operation(summary = "Criar novo pedido", description = "Cria um pedido com status inicial PENDENTE")
	@ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PedidoResponse criar(@RequestBody @Valid PedidoRequest dto) {
		return service.criar(dto);
	}

	@Operation(summary = "Atualizar pedido", description = "Atualiza o nome do pedido")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pedido atualizado"),
			@ApiResponse(responseCode = "404", description = "Pedido não encontrado") })
	@PutMapping("/{id}")
	public PedidoResponse atualizar(@PathVariable Long id, @RequestBody @Valid PedidoRequest dto) {
		return service.atualizar(id, dto);
	}

	@Operation(summary = "Alterar status do pedido", description = "Muda o status respeitando as transições válidas: PENDENTE -> EM_PREPARO -> PRONTO -> ENTREGUE")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Status alterado"),
			@ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
			@ApiResponse(responseCode = "409", description = "Transição de status inválida") })
	@PatchMapping("/{id}/status")
	public PedidoResponse alterarStatus(@PathVariable Long id, @RequestBody @Valid PedidoStatusRequest dto) {
		return service.alterarStatus(id, dto.status());
	}

	@Operation(summary = "Excluir pedido")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Pedido excluído"),
			@ApiResponse(responseCode = "404", description = "Pedido não encontrado") })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		service.deletar(id);
	}

	// =============== ITENS DO PEDIDO =================

	@Operation(summary = "Adicionar item ao pedido", description = "Adiciona um produto ao pedido e recalcula custo total e quantidade dos itens")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Item adicionado"),
			@ApiResponse(responseCode = "404", description = "Pedido ou produto não encontrado"),
			@ApiResponse(responseCode = "409", description = "Pedido não aceita mais itens") })
	@PostMapping("/{id}/itens")
	public PedidoResponse adicionarItem(@PathVariable Long id, @RequestBody @Valid ItemPedidoRequest dto) {
		return service.adicionarItem(id, dto);
	}

	@Operation(summary = "Remover item do pedido", description = "Remove um item do pedido e recalcula custo total e quantidade de itens")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Item removido"),
			@ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
			@ApiResponse(responseCode = "409", description = "Pedido não aceita mais alterações") })
	@DeleteMapping("/{id}/itens/{itemId}")
	public PedidoResponse removerItem(@PathVariable Long id, @PathVariable Long itemId) {
		return service.removerItem(id, itemId);
	}
}
