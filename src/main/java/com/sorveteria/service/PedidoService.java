package com.sorveteria.service;


import com.sorveteria.dto.PedidoMapper;
import com.sorveteria.dto.PedidoResponse;
import com.sorveteria.exception.PedidoNaoEncontradoException;
import com.sorveteria.model.Pedido;
import com.sorveteria.repository.PedidoRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional(readOnly = true)
	public PedidoResponse buscarPorId(Long id) {
		Pedido pedido = repository.findById(id)
				.orElseThrow(() -> new PedidoNaoEncontradoException(id));
		return PedidoMapper.toResponse(pedido);
	}


}
