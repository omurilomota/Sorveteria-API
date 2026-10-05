package com.sorveteria.service;

import com.sorveteria.dto.SaborRequest;
import com.sorveteria.model.Sabor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.sorveteria.repository.SaborRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.sorveteria.dto.SaborResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaborServiceImpl implements SaborService {

    private final SaborRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<SaborResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable)
        .map(SaborResponse::from);

    }

    @Override
    @Transactional(readOnly = true)
    public List<SaborResponse> listarAtivos() {
            return repository.findByAtivoTrue()
                    .stream()
                    .map(SaborResponse::from)
                    .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaborResponse buscarPorId(Long id) {
        return SaborResponse.from(buscarEntidade(id));
    }

    @Override
    @Transactional
    public SaborResponse criar(SaborRequest dto) {
        Sabor sabor = new Sabor();
        sabor.setNome(dto.nome());
        sabor.setAtivo(true);
        return SaborResponse.from(repository.save(sabor));
    }

    @Override
    @Transactional
    public SaborResponse atualizar(Long id, SaborRequest dto) {
        Sabor sabor = buscarEntidade(id);
        sabor.setNome(dto.nome());
        return SaborResponse.from(repository.save(sabor));
    }

    @Override
    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Sabor sabor = buscarEntidade(id);
        sabor.setAtivo(ativo);
        repository.save(sabor);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        repository.deleteById(id);
    }

    private Sabor buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sabor não encontrado" + id));
    }

}
