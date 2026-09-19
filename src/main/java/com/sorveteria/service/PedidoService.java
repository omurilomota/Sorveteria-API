package com.sorveteria.service;


import com.sorveteria.model.Pedido;
import com.sorveteria.repository.PedidoRepository;



import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PedidoService {
	
	private final PedidoRepository repository;
	
	public PedidoService(PedidoRepository repository) {
		this.repository = repository;
	}
	
	public List<Pedido> listar() {
		return repository.findAll();
	}
	
	public Pedido salvar(Pedido pedido) {
		return repository.save(pedido);
	}
	

}
