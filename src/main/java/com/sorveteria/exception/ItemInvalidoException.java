package com.sorveteria.exception;

public class ItemInvalidoException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public ItemInvalidoException(String mensagem) {
		super(mensagem);
	}

}
