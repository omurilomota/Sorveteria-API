package com.sorveteria.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sorveteria.service.UserService;
import jakarta.validation.Valid;
import com.sorveteria.dto.UserMapper;
import com.sorveteria.dto.UserRequest;
import com.sorveteria.dto.UserResponse;
import com.sorveteria.model.User;

@RestController
@RequestMapping("/usuarios")
public class UserController {
	
	private final UserService service;
	
	public UserController(UserService service){
		this.service = service;
	}
	
	@GetMapping("/{id}")
	public User buscarPorId(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

    @PostMapping
    public UserResponse criar(@Valid @RequestBody UserRequest request) {
        User user = UserMapper.toEntity(request);
        User salvo = service.salvar(user);
        return UserMapper.toResponse(salvo);
    }
    
	
}
