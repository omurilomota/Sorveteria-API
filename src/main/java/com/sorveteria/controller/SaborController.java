package com.sorveteria.controller;

import com.sorveteria.dto.SaborRequest;
import com.sorveteria.dto.SaborResponse;
import com.sorveteria.model.Sabor;
import com.sorveteria.service.SaborService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/sabores")
@RequiredArgsConstructor
public class SaborController {

    private final SaborService service;

    @GetMapping
    public Page<SaborResponse> listar(Pageable pageable) {
        return service.listarTodos(pageable);
    }

    @GetMapping("/ativos")
    public List<SaborResponse> listarAtivos() {
        return service.listarAtivos();
    }

    @GetMapping("/{id}")
    public SaborResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaborResponse criar(@RequestBody @Valid SaborRequest dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public SaborResponse atualizar(@PathVariable Long id, @RequestBody @Valid SaborRequest dto) {
        return service.atualizar(id, dto);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        service.alterarStatus(id, ativo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.deletar(id);
    }

}
