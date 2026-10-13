package com.sorveteria.dto;

import com.sorveteria.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getCpf().replaceAll("(\\d{3})\\d{3}\\d{3}(\\d{2})", "$1.***.***-$2")
        );
    }

    public User toEntity(UserRequest req) {
        User user = new User();
        user.setNome(req.nome());
        user.setEmail(req.email());
        user.setSenhaHash(passwordEncoder.encode(req.senha()));
        user.setTelefone(req.telefone());
        user.setCpf(req.cpf());
        user.setDataNascimento(req.dataNascimento());
        return user;
    }
    
}
