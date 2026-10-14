package com.sorveteria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

   @Entity
   @Getter
   @Setter
public class ItemPedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
    
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;
    
    private int quantidade;
    private BigDecimal precoUnitario;
    
    // calcula subtotal
    public BigDecimal getSubTotal(){
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
