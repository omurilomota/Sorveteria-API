package com.sorveteria.controller;

import com.sorveteria.dto.PedidoResponse;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sorveteria.model.Pedido;
import com.sorveteria.service.PedidoService;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
	
	private final PedidoService service;
	
	public PedidoController(PedidoService service)
	{
		this.service = service;
	}

	@GetMapping
	// ResponseEntity é uma classe do Spring que representa toda a resposta HTTP enviada pelo servidor, permitindo que você controle:
	public ResponseEntity<List<Pedido>> listar() {
		List<Pedido> pedidos = service.listar();
		if (pedidos.isEmpty()) {
			return ResponseEntity.noContent().build(); // 204
		}
		return ResponseEntity.ok(pedidos); //200
	}

	@GetMapping("/{id}")
	public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscarPorId(id));
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Pedido criar(@RequestBody Pedido pedido) {
		return service.salvar(pedido);
	}
	
}
