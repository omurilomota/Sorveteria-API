package com.sorveteria.service;

import com.sorveteria.exception.CpfJaCadastradoException;
import com.sorveteria.exception.EmailJaCadastradoException;
import com.sorveteria.exception.UsuarioNaoEncontradoException;
import com.sorveteria.model.User;
import com.sorveteria.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> listar() {
        return repository.findAll();
    }

    public User buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    public User salvar(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailJaCadastradoException(user.getEmail());
        }
        if (user.getCpf() != null && repository.existsByCpf(user.getCpf())) {
            throw new CpfJaCadastradoException(user.getCpf());
        }
        return repository.save(user);
    }

    public User atualizar(Long id, User novo) {
        User existente = buscarPorId(id);
        existente.setNome(novo.getNome());
        existente.setTelefone(novo.getTelefone());
        existente.setEndereco(novo.getEndereco());
        existente.setDataNascimento(novo.getDataNascimento());
        // não atualiza email, cpf nem senha por aqui
        return repository.save(existente);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new UsuarioNaoEncontradoException(id);
        }
        repository.deleteById(id);
    }
}