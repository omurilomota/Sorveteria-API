package com.sorveteria.dto;

import com.sorveteria.model.User;

public record UserResponse(
        Long id,
        String nome,
        String email,
        String cpf
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getCpf()
        );
    }
}