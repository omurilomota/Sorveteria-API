package com.sorveteria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter 
@NoArgsConstructor 
public class Produto {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String nome;
    @Column (nullable = false)
    private String descricao;
    @Column (nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
    @Column (nullable = false)
    private int estoque;
    @Column (nullable = false)
    private boolean ativo;
    
    @Enumerated(EnumType.STRING)
    private Categoria categoria;
}
