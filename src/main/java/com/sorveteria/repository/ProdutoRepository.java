package com.sorveteria.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sorveteria.model.Categoria;
import com.sorveteria.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    List<Produto> findByAtivoTrue();

    List<Produto> findByCategoria(Categoria categoria);

}
