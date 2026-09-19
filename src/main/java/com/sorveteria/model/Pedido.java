package com.sorveteria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private BigDecimal custoTotal;
    @Enumerated(EnumType.STRING)
    private StatusPedido status;
    private int itensTotal;

}
