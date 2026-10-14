package com.sorveteria.dto;

import java.util.List;

import com.sorveteria.model.ItemPedido;
import com.sorveteria.model.Pedido;

public class PedidoMapper {

	public static PedidoResponse toResponse(Pedido pedido) {
		List<ItemPedidoResponse> itens = pedido.getItens().stream().map(PedidoMapper::toItemResponse).toList();

		return new PedidoResponse(pedido.getId(), pedido.getStatus(), pedido.getItensTotal(), pedido.getCustoTotal(),
				itens);
	}

	private static ItemPedidoResponse toItemResponse(ItemPedido item) {
		return new ItemPedidoResponse(item.getId(), item.getProduto().getNome(), item.getQuantidade(),
				item.getPrecoUnitario(), item.getSubTotal()

		);
	}

}
