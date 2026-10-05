package com.sorveteria.dto;

import com.sorveteria.model.User;

public record UserResponse(
        Long id,
        String nome,
        String email,
        String cpf
) {}