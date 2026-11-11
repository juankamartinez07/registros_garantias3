package com.inventario.repository;

import com.inventario.model.LoteSerialInterno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteSerialInternoRepository extends JpaRepository<LoteSerialInterno, Long> {
    boolean existsByCodigo(String codigo);
}
