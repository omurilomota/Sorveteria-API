package com.sorveteria.service;

import com.sorveteria.model.Sabor;
import com.sorveteria.repository.SaborRepository;

import java.util.List;

public class SaborService {
    private final SaborRepository repository;

    public SaborService(SaborRepository repository) {
        this.repository = repository;
    }

    public List<Sabor> listar() {
        return repository.findAll();
    }

    public Sabor salvar(Sabor sabor) {
        return repository.save(sabor);
    }
}
