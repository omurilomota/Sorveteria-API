package com.sorveteria.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest (

    @NotBlank String nome,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8) String senha,
    @Pattern(regexp = "\\d{10,11}") String telefone,
    @Pattern (regexp = "\\d{11}") String cpf,
    @Past LocalDate dataNascimento
) {}