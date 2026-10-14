package com.sorveteria.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sorveteria.dto.ItemPedidoRequest;
import com.sorveteria.dto.PedidoMapper;
import com.sorveteria.dto.PedidoRequest;
import com.sorveteria.dto.PedidoResponse;
import com.sorveteria.exception.ItemInvalidoException;
import com.sorveteria.exception.PedidoNaoEncontradoException;
import com.sorveteria.exception.ProdutoNaoEncontradoException;
import com.sorveteria.exception.TransicaoStatusInvalidaException;
import com.sorveteria.model.ItemPedido;
import com.sorveteria.model.Pedido;
import com.sorveteria.model.Produto;
import com.sorveteria.model.StatusPedido;
import com.sorveteria.repository.PedidoRepository;
import com.sorveteria.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

	private final ProdutoRepository produtorepository;
	private final PedidoRepository repository;

	// Regras de transição de status

	private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = Map.of(StatusPedido.PENDENTE,
			Set.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO), StatusPedido.EM_PREPARO,
			Set.of(StatusPedido.PRONTO, StatusPedido.CANCELADO), StatusPedido.PRONTO,
			Set.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO), StatusPedido.ENTREGUE, Set.of(),
			StatusPedido.CANCELADO, Set.of());

	@Transactional(readOnly = true)
	public List<PedidoResponse> listar() {
		return repository.findAll().stream().map(PedidoMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public PedidoResponse buscarPorId(Long id) {
		return PedidoMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public PedidoResponse criar(PedidoRequest dto) {
		Pedido pedido = new Pedido();
		pedido.setNome(dto.nome());
		pedido.setStatus(StatusPedido.PENDENTE);
		pedido.setCustoTotal(BigDecimal.ZERO);
		pedido.setItensTotal(0);
		return PedidoMapper.toResponse(repository.save(pedido));
	}

	@Transactional
	public PedidoResponse atualizar(Long id, PedidoRequest dto) {
		Pedido pedido = buscarEntidade(id);
		pedido.setNome(dto.nome());
		return PedidoMapper.toResponse(repository.save(pedido));
	}

	@Transactional
	public PedidoResponse alterarStatus(Long id, StatusPedido novoStatus) {
		Pedido pedido = buscarEntidade(id);
		validarTransicao(pedido.getStatus(), novoStatus);
		pedido.setStatus(novoStatus);
		return PedidoMapper.toResponse(repository.save(pedido));
	}

	@Transactional
	public void deletar(Long id) {
		Pedido pedido = buscarEntidade(id);
		repository.delete(pedido);
	}

	// ================ ITENS DO PEDIDO ===================

	@Transactional
	public PedidoResponse adicionarItem(Long pedidoId, ItemPedidoRequest dto) {
		Pedido pedido = buscarEntidade(pedidoId);
		validarPedidoAceitaItens(pedido);

		Produto produto = produtorepository.findById(dto.produtoId())
				.orElseThrow(() -> new ProdutoNaoEncontradoException(dto.produtoId()));

		ItemPedido item = new ItemPedido();
		item.setPedido(pedido);
		item.setProduto(produto);
		item.setQuantidade(dto.quantidade());
		item.setPrecoUnitario(produto.getPreco());

		pedido.getItens().add(item);
		recalcularTotais(pedido);

		return PedidoMapper.toResponse(repository.save(pedido));
	}

	@Transactional
	public PedidoResponse removerItem(Long pedidoId, Long itemId) {
		Pedido pedido = buscarEntidade(pedidoId);
		validarPedidoAceitaItens(pedido);

		boolean removido = pedido.getItens().removeIf(i -> i.getId().equals(itemId));
		if (!removido) {
			throw new ItemInvalidoException("Item não encontrado no pedido: " + itemId);
		}
		recalcularTotais(pedido);
		return PedidoMapper.toResponse(repository.save(pedido));
	}

	private void validarPedidoAceitaItens(Pedido pedido) {
		if (pedido.getStatus() == StatusPedido.ENTREGUE || pedido.getStatus() == StatusPedido.CANCELADO) {
			throw new ItemInvalidoException(
					"Não é possível alterar itens de um pedido com status " + pedido.getStatus());
		}
	}

	private void recalcularTotais(Pedido pedido) {
		BigDecimal total = pedido.getItens().stream().map(ItemPedido::getSubTotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		pedido.setCustoTotal(total);
		pedido.setItensTotal(pedido.getItens().size());
	}

	// ================ HELPERS =====================

	private Pedido buscarEntidade(Long id) {
		return repository.findById(id).orElseThrow(() -> new PedidoNaoEncontradoException(id));
	}

	private void validarTransicao(StatusPedido atual, StatusPedido novo) {
		Set<StatusPedido> permitidos = TRANSICOES_VALIDAS.getOrDefault(atual, Set.of());
		if (!permitidos.contains(novo)) {
			throw new TransicaoStatusInvalidaException(atual, novo);
		}
	}

}
