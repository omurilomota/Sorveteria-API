package com.sorveteria.dto;

import com.sorveteria.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getTelefone()
        );
    }

    public static User toEntity(UserRequest req) {
        User user = new User();
        user.setNome(req.nome());
        user.setEmail(req.email());
        user.setSenhaHash(req.senha());
        user.setTelefone(req.telefone());
        user.setCpf(req.cpf());
        user.setDataNascimento(req.dataNascimento());
        return user;
    }
    
}
