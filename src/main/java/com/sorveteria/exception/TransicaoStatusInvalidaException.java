package com.sorveteria.exception;

import com.sorveteria.model.StatusPedido;

public class TransicaoStatusInvalidaException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	public TransicaoStatusInvalidaException(StatusPedido atual, StatusPedido novo) {
		super("Não é possível mudar o status de "+ atual + " para " + novo);
	}

}
