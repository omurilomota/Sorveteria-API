package com.sorveteria.controller;

import com.sorveteria.model.Sabor;
import com.sorveteria.service.SaborService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class SaborController {
    private final SaborService service;

    public SaborController(SaborService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sabor> listar(){
        return service.listar();
    }

    @PostMapping
    public Sabor criar(@RequestBody Sabor sabor) {
        return service.salvar(sabor);
    }
}
