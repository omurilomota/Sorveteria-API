package com.sorveteria.service;

import com.sorveteria.dto.SaborRequest;
import com.sorveteria.dto.SaborResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface SaborService {
    Page<SaborResponse> listarTodos(Pageable pageable);
    List<SaborResponse> listarAtivos();
    SaborResponse buscarPorId(Long id);

    SaborResponse criar(SaborRequest dto);
    SaborResponse atualizar(Long id, SaborRequest dto);

    void alterarStatus(Long id, boolean ativo);
    void deletar(Long id);
}
