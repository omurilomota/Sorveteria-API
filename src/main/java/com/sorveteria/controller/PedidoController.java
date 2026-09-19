package com.sorveteria.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public List<Pedido> listar() {
		return service.listar();
	}
	
	@PostMapping
	public Pedido criar(@RequestBody Pedido pedido) {
		return service.salvar(pedido);
	}
	
}
