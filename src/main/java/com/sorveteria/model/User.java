package com.sorveteria.model;

// Validation and persistence annotations
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

// Lombok annotations for generating getters and setters
import lombok.Getter;
import lombok.Setter;

// Java time package for handling date of birth
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode ser vazio")
    private String nome;

    @Email(message = "O email deve ser válido")
    @NotBlank(message = "O email não pode ser vazio")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "A senha não pode ser vazia")
    @Column(nullable = false)
    private String senhaHash;

    @Embedded
    private Endereco endereco;

    @Embeddable
    public static class Endereco {
        private String rua;
        private String numero;
        private String cidade;
        private String estado;
        private String cep;

    public Endereco() {}
    
}

    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos")
    private String telefone;

    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 dígitos")
    private String cpf;

    @Past(message = "A data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    private boolean ativo = true;

    private LocalDateTime ultimoLogin;

    @OneToMany(mappedBy = "user")
    private List<Pedido> pedidos = new ArrayList<>();

}
