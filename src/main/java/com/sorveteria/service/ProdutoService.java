package com.sorveteria.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sorveteria.dto.ProdutoRequest;
import com.sorveteria.dto.ProdutoResponse;
import com.sorveteria.exception.ProdutoNaoEncontradoException;
import com.sorveteria.model.Produto;
import com.sorveteria.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

	private final ProdutoRepository repository;

	@Transactional(readOnly = true)
	public List<ProdutoResponse> listar() {
		return repository.findAll().stream().map(ProdutoResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public ProdutoResponse buscarPorId(Long id) {
		Produto produto = repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
		return ProdutoResponse.from(produto);
	}

	@Transactional
	public ProdutoResponse criar(ProdutoRequest request) {
		Produto produto = new Produto();
		produto.setNome(request.nome());
		produto.setDescricao(request.descricao());
		produto.setPreco(request.preco());
		produto.setEstoque(request.estoque());
		return ProdutoResponse.from(repository.save(produto));
	}

	@Transactional
	public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
		Produto produto = repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
		produto.setNome(request.nome());
		produto.setDescricao(request.descricao());
		produto.setPreco(request.preco());
		produto.setEstoque(request.estoque());
		return ProdutoResponse.from(repository.save(produto));
	}

	@Transactional
	public void deletar(Long id) {
		if (!repository.existsById(id)) {
			throw new ProdutoNaoEncontradoException(id);
		}
		repository.deleteById(id);
	}
}
