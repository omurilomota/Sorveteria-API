package com.sorveteria.repository;

import com.sorveteria.model.Sabor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaborRepository extends JpaRepository<Sabor, Long> {

    List<Sabor> findByAtivoTrue();

}
