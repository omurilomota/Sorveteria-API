package com.sorveteria.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ProdutoNaoEncontradoException(Long id) {
		super("Produto não encontrado: "+ id);
	}
	
}
