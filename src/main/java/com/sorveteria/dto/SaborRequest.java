package com.sorveteria.dto;

import jakarta.validation.constraints.NotBlank;

public record SaborRequest (

    @NotBlank String nome

) {}
